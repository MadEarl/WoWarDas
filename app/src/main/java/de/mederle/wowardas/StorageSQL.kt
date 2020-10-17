package de.mederle.wowardas

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Entry(var latitude: Float, var longitude: Float, var time: Long)

class StorageSQL(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_LOCATION_TABLE = ("CREATE TABLE " + TABLE_NAME + " " +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY, " +
                "latitude FLOAT, longitude FLOAT, " +
                "datetime INT)")
        db!!.execSQL(CREATE_LOCATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet needed.")
    }

    fun addEntry(entry: Entry) {
        val values = ContentValues()
        values.put("latitude", entry.latitude)
        values.put("longitude", entry.longitude)
        values.put("datetime", entry.time)
        val db = this.writableDatabase
        db.insert("location", null, values)
        db.close()
    }

    fun getAllEntries(): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM location", null)
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "locations.db"
        val TABLE_NAME = "location"
        val COLUMN_ID = "_id"
    }
}