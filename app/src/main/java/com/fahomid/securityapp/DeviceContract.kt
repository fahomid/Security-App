package com.fahomid.securityapp

import android.content.ContentResolver
import android.net.Uri
import android.provider.BaseColumns

// Object to define the contract for the device database schema and URIs
object DeviceContract {

    // The content authority for the content provider
    const val CONTENT_AUTHORITY = "com.fahomid.securityapp.provider"

    // The base content URI for the content provider
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

    // The path for the "devices" directory
    const val PATH_DEVICES = "devices"

    // Inner object to define the table contents
    object DeviceEntry : BaseColumns {

        // Table name
        const val TABLE_NAME = "devices"

        // Column names
        const val COLUMN_NAME = "name"
        const val COLUMN_IP = "ip"
        const val COLUMN_PORT = "port"
        const val COLUMN_PATH = "path"
        const val COLUMN_CLIPS_PATH = "clips_path"
        const val COLUMN_ICON = "icon"
        const val COLUMN_LAST_ACCESS = "last_access"

        // The content URI to access the device data in the provider
        val CONTENT_URI: Uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DEVICES)

        // MIME type for a list of devices
        const val CONTENT_LIST_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/$CONTENT_AUTHORITY/$PATH_DEVICES"

        // MIME type for a single device
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/$CONTENT_AUTHORITY/$PATH_DEVICES"
    }
}
