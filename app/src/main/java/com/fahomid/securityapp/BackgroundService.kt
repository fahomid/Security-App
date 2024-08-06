package com.fahomid.securityapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class BackgroundService : FirebaseMessagingService() {

    // Called when a message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            val title = it.title ?: "Event Detected"
            val message = it.body ?: "An event has been detected."
            showNotification(title, message)
        }

        // Check if the message contains a data payload (optional, if you are using data payload)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Event Detected"
            val message = remoteMessage.data["message"] ?: "An event has been detected."
            showNotification(title, message)
        }
    }

    // Show a notification with the specified title and message
    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "event_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Event Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    // Called when a new token for FCM is generated
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("BackgroundService", "Refreshed token: $token")

        // Get the unique device ID
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Send token and device ID to your server
        sendTokenToServer(deviceId, token)
    }

    // Send the token and device ID to the server
    private fun sendTokenToServer(deviceId: String, token: String) {
        val client = OkHttpClient()
        val url = "http://10.0.0.2:8080/register_token"

        val json = JSONObject()
        json.put("device_id", deviceId)
        json.put("token", token)

        val body = RequestBody.create(
            MediaType.parse("application/json; charset=utf-8"),
            json.toString()
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        // Make the network request to send the token to the server
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("BackgroundService", "Failed to send token", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("BackgroundService", "Token sent successfully")
                } else {
                    Log.e("BackgroundService", "Failed to send token: ${response.code()}")
                }
            }
        })
    }
}
