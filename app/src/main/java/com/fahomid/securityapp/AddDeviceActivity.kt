package com.fahomid.securityapp

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class AddDeviceActivity : AppCompatActivity() {

    // UI elements for device input fields and save button
    private lateinit var deviceNameInput: TextInputEditText
    private lateinit var deviceIpInput: TextInputEditText
    private lateinit var devicePortInput: TextInputEditText
    private lateinit var devicePathInput: TextInputEditText
    private lateinit var deviceClipsPathInput: TextInputEditText
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_device)

        // Initialize UI elements
        deviceNameInput = findViewById(R.id.device_name_input)
        deviceIpInput = findViewById(R.id.device_ip_input)
        devicePortInput = findViewById(R.id.device_port_input)
        devicePathInput = findViewById(R.id.device_path_input)
        deviceClipsPathInput = findViewById(R.id.device_clips_path_input)
        saveButton = findViewById(R.id.save_button)

        // Set click listener for the save button
        saveButton.setOnClickListener {
            hideKeyboard()  // Hide the keyboard when the save button is clicked
            saveDevice()    // Attempt to save the device information
        }
    }

    // Save the device information to the database
    private fun saveDevice() {
        val deviceName = deviceNameInput.text.toString()
        val deviceIp = deviceIpInput.text.toString()
        val devicePort = devicePortInput.text.toString()
        val devicePath = devicePathInput.text.toString()
        val deviceClipsPath = deviceClipsPathInput.text.toString()

        // Check if all fields are filled
        if (deviceName.isNotEmpty() && deviceIp.isNotEmpty() && devicePort.isNotEmpty() && devicePath.isNotEmpty() && deviceClipsPath.isNotEmpty()) {
            // Validate IP address and port number
            if (!isValidIp(deviceIp)) {
                showSnackbar("Invalid IP address", "OK")
                return
            }
            if (!isValidPort(devicePort)) {
                showSnackbar("Invalid port number", "OK")
                return
            }

            // Prepare content values to insert into the database
            val values = ContentValues().apply {
                put(DeviceContract.DeviceEntry.COLUMN_NAME, deviceName)
                put(DeviceContract.DeviceEntry.COLUMN_IP, deviceIp)
                put(DeviceContract.DeviceEntry.COLUMN_PORT, devicePort)
                put(DeviceContract.DeviceEntry.COLUMN_PATH, devicePath)
                put(DeviceContract.DeviceEntry.COLUMN_CLIPS_PATH, deviceClipsPath)
                put(DeviceContract.DeviceEntry.COLUMN_ICON, R.drawable.ic_camera)  // Assuming a default icon
                put(DeviceContract.DeviceEntry.COLUMN_LAST_ACCESS, "Never")
            }

            // Insert the device information into the database
            contentResolver.insert(DeviceContract.DeviceEntry.CONTENT_URI, values)

            // Return result to indicate the device was added successfully
            val intent = Intent().apply {
                putExtra("DEVICE_ADDED", true)
            }
            setResult(RESULT_OK, intent)
            finish()  // Close the activity
        } else {
            showSnackbar("Please fill all the fields", "OK")
        }
    }

    // Validate the IP address format
    private fun isValidIp(ip: String): Boolean {
        val ipPattern = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        return ipPattern.matches(ip)
    }

    // Validate the port number
    private fun isValidPort(port: String): Boolean {
        return try {
            val portNum = port.toInt()
            portNum in 1..65535
        } catch (e: NumberFormatException) {
            false
        }
    }

    // Show a snackbar with a message and an optional action
    private fun showSnackbar(message: String, action: String) {
        Snackbar.make(findViewById(R.id.save_button), message, Snackbar.LENGTH_SHORT)
            .setAction(action) {
                // Handle action click (if needed)
            }
            .show()
    }

    // Hide the keyboard
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
