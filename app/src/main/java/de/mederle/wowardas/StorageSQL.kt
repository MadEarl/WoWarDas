package de.mederle.wowardas

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
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
                LAT + " FLOAT, " + LOT + " FLOAT, "
                + DTT + " INT)")
        db!!.execSQL(CREATE_LOCATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet needed.")
    }

    fun addEntry(entry: Entry) {
        val values = ContentValues()
        values.put(LAT, entry.latitude)
        values.put(LOT, entry.longitude)
        values.put(DTT, entry.time)
        val db = this.writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllEntries(context: Context): ArrayList<Entry> {
        val resultList = ArrayList<Entry>()
        val dbHelper = StorageSQL(context, null)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM location ORDER BY _id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val entry = Entry(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getFloat(cursor.getColumnIndex(LAT)),
                    cursor.getFloat(cursor.getColumnIndex(LOT)),
                    cursor.getLong(cursor.getColumnIndex(DTT))
                )
                resultList.add(entry)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return resultList
    }

    fun getAllEntriesCursor(context: Context): Cursor {
        val dbHelper = StorageSQL(context, null)
        val db = dbHelper.readableDatabase
        return db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC", null)
    }

    fun getEntryByID(context: Context, id: Int): Entry {
        val entry = Entry()
        val dbHelper = StorageSQL(context, null)
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME where $COLUMN_ID = $id", null)
        if (cursor.moveToFirst()) {
            entry.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            entry.latitude = cursor.getFloat(cursor.getColumnIndex(LAT))
            entry.longitude = cursor.getFloat(cursor.getColumnIndex(LOT))
            entry.time = cursor.getLong(cursor.getColumnIndex(DTT))
        }
        cursor.close()
        return entry
    }

    fun deleteEntryByID(context: Context, id: Int) {
        val dbHelper = StorageSQL(context, null)
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("DELETE * FROM $TABLE_NAME WHERE $COLUMN_ID=$id", null)
        cursor.close()
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "locations.db"
        const val TABLE_NAME = "location"
        const val COLUMN_ID = "_id"
        const val LAT = "latitude"
        const val LOT = "longitude"
        const val DTT = "datetime"
    }
}