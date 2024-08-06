package com.fahomid.securityapp

// Data class representing a device with various properties
data class Device(
    val name: String,         // Name of the device
    val ip: String,           // IP address of the device
    val port: String,         // Port number for connecting to the device
    val path: String,         // Path for the live video feed
    val clipsPath: String,    // Path for accessing recorded clips
    val icon: Int,            // Resource ID for the device icon
    val lastAccess: String,   // Last access time of the device
    val id: Long              // Unique ID of the device
)
