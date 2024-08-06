package com.fahomid.securityapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

// Helper class to manage database creation and version management
class DeviceDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Called when the database is created for the first time
    override fun onCreate(db: SQLiteDatabase) {
        // SQL statement to create the devices table
        val SQL_CREATE_DEVICES_TABLE = ("CREATE TABLE " + DeviceContract.DeviceEntry.TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DeviceContract.DeviceEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + DeviceContract.DeviceEntry.COLUMN_IP + " TEXT NOT NULL, "
                + DeviceContract.DeviceEntry.COLUMN_PORT + " TEXT NOT NULL, "
                + DeviceContract.DeviceEntry.COLUMN_PATH + " TEXT NOT NULL, "
                + DeviceContract.DeviceEntry.COLUMN_CLIPS_PATH + " TEXT NOT NULL, "
                + DeviceContract.DeviceEntry.COLUMN_ICON + " INTEGER NOT NULL, "
                + DeviceContract.DeviceEntry.COLUMN_LAST_ACCESS + " TEXT);")
        db.execSQL(SQL_CREATE_DEVICES_TABLE)
    }

    // Called when the database needs to be upgraded
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + DeviceContract.DeviceEntry.TABLE_NAME)
        // Create the table again
        onCreate(db)
    }

    companion object {
        // Name of the database file
        const val DATABASE_NAME = "devices.db"
        // Database version. If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
    }
}
