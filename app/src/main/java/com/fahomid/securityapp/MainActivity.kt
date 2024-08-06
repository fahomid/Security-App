package com.fahomid.securityapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import android.provider.BaseColumns
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeviceAdapter
    private lateinit var noDevicesTextView: TextView
    private var explanationShown = false
    private var permissionRequested = false

    // Register for the result of requesting notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
            explanationShown = false // Reset the flag if permission is granted
        } else {
            Log.d("MainActivity", "Notification permission denied")
            if (!explanationShown) {
                showPermissionExplanation()
            }
        }
        permissionRequested = false // Reset the request flag after handling the result
    }

    // Register for the result of adding a device
    private val addDeviceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val deviceAdded = result.data?.getBooleanExtra("DEVICE_ADDED", false) ?: false
            if (deviceAdded) {
                Snackbar.make(findViewById(R.id.recycler_view), "Device added successfully", Snackbar.LENGTH_SHORT)
                    .setAction("OK") {
                        // Handle click
                    }
                    .show()
                loadDevices()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create notification channel
        createNotificationChannel()

        // Check and request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        // Get the unique device ID
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        // Get FCM token and send it to the server
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("MainActivity", "FCM Token: $token")
            Log.d("MainActivity", "Device ID: $deviceId")

            // Send token and device ID to your server
            sendTokenToServer(deviceId, token)
        }

        // Initialize UI elements
        recyclerView = findViewById(R.id.recycler_view)
        val addDeviceBtn: FloatingActionButton = findViewById(R.id.add_device_btn)
        noDevicesTextView = findViewById(R.id.no_devices_text_view)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DeviceAdapter(this, mutableListOf(), ::deleteDevice)
        recyclerView.adapter = adapter

        // Set click listener for the add device button
        addDeviceBtn.setOnClickListener {
            val intent = Intent(this, AddDeviceActivity::class.java)
            addDeviceLauncher.launch(intent)
        }

        // Load devices from the database and update the adapter
        loadDevices()
    }

    override fun onResume() {
        super.onResume()

        // Check notification permission again when the app resumes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }

        // Reload devices when returning to the activity
        loadDevices()
    }

    // Check and request notification permission
    private fun checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, you can proceed with showing notifications
            Log.d("MainActivity", "Notification permission already granted")
            explanationShown = false // Reset the flag if permission is granted
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            // Show an explanation to the user if not already shown
            if (!explanationShown) {
                showPermissionExplanation()
            }
        } else {
            // Directly request for required permissions, without explanation
            if (!permissionRequested) {
                permissionRequested = true
                requestPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    // Show explanation dialog for notification permission
    private fun showPermissionExplanation() {
        explanationShown = true // Set the flag to avoid repeated dialogs
        AlertDialog.Builder(this)
            .setTitle("Notification Permission Needed")
            .setMessage("This app needs the Notification permission to alert you about important updates. Please grant the permission in the app settings.")
            .setPositiveButton("OK") { _, _ ->
                // Open the app's notification settings
                openAppNotificationSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Open app notification settings
    private fun openAppNotificationSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }

    // Create a notification channel for Android O and higher
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "This is the default notification channel."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("default_channel_id", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Send FCM token and device ID to the server
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

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Failed to send token", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "Token sent successfully")
                } else {
                    Log.e("MainActivity", "Failed to send token: ${response.code()}")
                }
            }
        })
    }

    // Load devices from the database and update the adapter
    private fun loadDevices() {
        val cursor: Cursor? = contentResolver.query(
            DeviceContract.DeviceEntry.CONTENT_URI,
            null, null, null, null
        )

        cursor?.let {
            val devices = mutableListOf<Device>()
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(BaseColumns._ID))
                val name = it.getString(it.getColumnIndexOrThrow(DeviceContract.DeviceEntry.COLUMN_NAME))
                val ip = it.getString(it.getColumnIndexOrThrow(DeviceContract.DeviceEntry.COLUMN_IP))
                val port = it.getString(it.getColumnIndexOrThrow(DeviceContract.DeviceEntry.COLUMN_PORT))
                val path = it.getString(it.getColumnIndexOrThrow(DeviceContract.DeviceEntry.COLUMN_PATH))
                val clipsPath = it.getString(it.getColumnIndexOrThrow(DeviceContract.DeviceEntry.COLUMN_CLIPS_PATH))
                val lastAccess = it.getString(it.getColumnIndexOrThrow(DeviceContract.DeviceEntry.COLUMN_LAST_ACCESS))
                val device = Device(name, ip, port, path, clipsPath, R.drawable.ic_camera, lastAccess, id)
                devices.add(device)
                // Log the device details
                Log.d("MainActivity", "Device loaded: $device")
            }
            adapter.updateDevices(devices)
            it.close()

            // Update UI based on whether devices are present
            if (devices.isEmpty()) {
                recyclerView.visibility = View.GONE
                noDevicesTextView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                noDevicesTextView.visibility = View.GONE
            }
        }
    }

    // Delete a device from the database
    private fun deleteDevice(deviceId: Long) {
        val uri = ContentUris.withAppendedId(DeviceContract.DeviceEntry.CONTENT_URI, deviceId)
        val rowsDeleted = contentResolver.delete(uri, null, null)
        Log.d("MainActivity", "Deleted $rowsDeleted rows.")
        if (rowsDeleted > 0) {
            Snackbar.make(findViewById(R.id.recycler_view), "Device deleted successfully", Snackbar.LENGTH_SHORT)
                .setAction("OK") {
                    // Handle click
                }
                .show()
            loadDevices()
        }
    }
}
