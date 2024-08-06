package com.fahomid.securityapp

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

// Adapter for managing and displaying a list of devices in a RecyclerView
class DeviceAdapter(
    private val context: Context,                           // Context for inflating views and starting activities
    private val devices: MutableList<Device>,               // List of devices to be displayed
    private val deleteDeviceCallback: (Long) -> Unit        // Callback for handling device deletion
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    // Called when RecyclerView needs a new ViewHolder of the given type to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(itemView)
    }

    // Called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        holder.bind(device)
    }

    // Returns the total number of items in the data set held by the adapter
    override fun getItemCount(): Int {
        return devices.size
    }

    // Update the list of devices and notify the adapter to refresh the RecyclerView
    fun updateDevices(newDevices: List<Device>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged()
    }

    // ViewHolder class for managing the views of each item in the RecyclerView
    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.device_name)                // TextView for the device name
        private val deviceStreamFeed: TextView = itemView.findViewById(R.id.device_stream_feed)  // TextView for the device stream feed URL
        private val deviceClips: TextView = itemView.findViewById(R.id.device_clips)             // TextView for the device clips URL
        private val deviceLastAccess: TextView = itemView.findViewById(R.id.device_last_access)  // TextView for the device last access time
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_button)     // Button for deleting the device

        // Bind the device data to the views
        fun bind(device: Device) {
            deviceName.text = device.name
            val liveFeedUrl = "http://${device.ip}:${device.port}${device.path}"
            deviceStreamFeed.text = "Stream Feed: $liveFeedUrl"
            val clipsUrl = "http://${device.ip}:${device.port}${device.clipsPath}"
            deviceClips.text = "Clips: $clipsUrl"
            deviceLastAccess.text = "Last accessed: ${device.lastAccess}"

            // Set click listener for the item to open device details
            itemView.setOnClickListener {
                val intent = Intent(context, DeviceDetailActivity::class.java).apply {
                    putExtra("DEVICE_NAME", device.name)
                    putExtra("LIVE_FEED_URL", liveFeedUrl)
                    putExtra("CLIPS_URL", clipsUrl)
                    putExtra("LAST_ACCESS", device.lastAccess)
                }
                context.startActivity(intent)
            }

            // Set click listener for the delete button to show a confirmation dialog
            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(device.id)
            }
        }

        // Show a confirmation dialog for deleting a device
        private fun showDeleteConfirmationDialog(deviceId: Long) {
            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Delete Device")
                .setMessage("Are you sure you want to delete this device? This action cannot be undone.")
                .setPositiveButton("Yes") { _, _ ->
                    deleteDeviceCallback(deviceId)  // Call the delete callback if confirmed
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}
