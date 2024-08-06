package com.fahomid.securityapp

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log

// ContentProvider for managing device data
class DeviceProvider : ContentProvider() {

    private lateinit var dbHelper: DeviceDbHelper

    // Initialize the ContentProvider
    override fun onCreate(): Boolean {
        dbHelper = DeviceDbHelper(context!!)
        return true
    }

    // Query the database
    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val database = dbHelper.readableDatabase
        val cursor: Cursor = when (uriMatcher.match(uri)) {
            DEVICES -> database.query(
                DeviceContract.DeviceEntry.TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder
            )
            DEVICE_ID -> {
                val selection = "${BaseColumns._ID}=?"
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                database.query(
                    DeviceContract.DeviceEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    // Insert a new device into the database
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val database = dbHelper.writableDatabase
        val id: Long = when (uriMatcher.match(uri)) {
            DEVICES -> database.insert(DeviceContract.DeviceEntry.TABLE_NAME, null, values)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
        if (id == -1L) {
            Log.e("DeviceProvider", "Failed to insert row for $uri")
            return null
        }
        context?.contentResolver?.notifyChange(uri, null)
        return ContentUris.withAppendedId(uri, id)
    }

    // Update an existing device in the database
    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        if (values == null) {
            throw IllegalArgumentException("Cannot update with null values")
        }
        val database = dbHelper.writableDatabase
        val rowsUpdated: Int = when (uriMatcher.match(uri)) {
            DEVICES -> database.update(
                DeviceContract.DeviceEntry.TABLE_NAME, values, selection, selectionArgs
            )
            DEVICE_ID -> {
                val selection = "${BaseColumns._ID}=?"
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                database.update(
                    DeviceContract.DeviceEntry.TABLE_NAME, values, selection, selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Update is not supported for $uri")
        }
        if (rowsUpdated != 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsUpdated
    }

    // Delete a device from the database
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val database = dbHelper.writableDatabase
        val rowsDeleted: Int = when (uriMatcher.match(uri)) {
            DEVICES -> database.delete(
                DeviceContract.DeviceEntry.TABLE_NAME, selection, selectionArgs
            )
            DEVICE_ID -> {
                val selection = "${BaseColumns._ID}=?"
                val selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                database.delete(
                    DeviceContract.DeviceEntry.TABLE_NAME, selection, selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Deletion is not supported for $uri")
        }
        if (rowsDeleted != 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rowsDeleted
    }

    // Return the MIME type of the data at the given URI
    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            DEVICES -> DeviceContract.DeviceEntry.CONTENT_LIST_TYPE
            DEVICE_ID -> DeviceContract.DeviceEntry.CONTENT_ITEM_TYPE
            else -> throw IllegalStateException("Unknown URI $uri with match ${uriMatcher.match(uri)}")
        }
    }

    companion object {
        private const val DEVICES = 100  // URI code for the devices table
        private const val DEVICE_ID = 101  // URI code for a specific device

        // UriMatcher to match content URIs to corresponding operations
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(DeviceContract.CONTENT_AUTHORITY, DeviceContract.PATH_DEVICES, DEVICES)
            addURI(DeviceContract.CONTENT_AUTHORITY, "${DeviceContract.PATH_DEVICES}/#", DEVICE_ID)
        }
    }
}
