package de.mederle.wowardas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// wanted REAL for date, but values.put did not like it
class Entry {
    var id: Int? = null
    var latitude: Float
    var longitude: Float
    var time: Long

    constructor(id: Int, latitude: Float, longitude: Float, time: Long) {
        this.id = id
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
    }

    constructor(latitude: Float, longitude: Float, time: Long) {
        this.id = null
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
    }

    constructor(latitude: Float, longitude: Float) {
        this.id = null
        this.latitude = latitude
        this.longitude = longitude
        this.time = System.currentTimeMillis() / 1000
    }

    constructor() {
        this.id = null
        this.latitude = 0f
        this.longitude = 0f
        this.time = 0
    }
}

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

    fun getAllEntries(context: Context): ArrayList<Entry> {
        val resultList = ArrayList<Entry>()
        val dbHelper = StorageSQL(context, null)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM location ORDER BY _id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val entry = Entry(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getFloat(cursor.getColumnIndex("latitude")),
                        cursor.getFloat(cursor.getColumnIndex("longitude")),
                        cursor.getLong(cursor.getColumnIndex("datetime")))
                resultList.add(entry)
            } while (cursor.moveToNext())
        }
        return resultList
    }

    fun getEntryByID(context: Context, id: Int): Entry {
        val entry = Entry()
        val dbHelper = StorageSQL(context, null)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM location where " + COLUMN_ID + " = " + id, null)
        if (cursor.moveToFirst()) {
            entry.id = cursor.getInt(cursor.getColumnIndex("_id"))
            entry.latitude = cursor.getFloat(cursor.getColumnIndex("latitude"))
            entry.longitude = cursor.getFloat(cursor.getColumnIndex("longitude"))
            entry.time = cursor.getLong(cursor.getColumnIndex("datetime"))
        }
        return entry
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "locations.db"
        val TABLE_NAME = "location"
        val COLUMN_ID = "_id"
    }
}