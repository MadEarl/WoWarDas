package de.mederle.wowardas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast

// wanted REAL for date, but values.put did not like it
class Entry {
    var id: Int? = null
    var latitude: Float
    var longitude: Float
    var time: Long
    var comment: String? = ""

    constructor(id: Int, latitude: Float, longitude: Float, time: Long, comment: String) {
        this.id = id
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
        this.comment = comment
    }

    constructor(id: Int, latitude: Float, longitude: Float, time: Long) {
        this.id = id
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
        this.comment = ""
    }

    constructor(latitude: Float, longitude: Float, time: Long) {
        this.id = null
        this.latitude = latitude
        this.longitude = longitude
        this.time = time
        this.comment = ""
    }

    constructor(latitude: Float, longitude: Float) {
        this.id = null
        this.latitude = latitude
        this.longitude = longitude
        this.time = System.currentTimeMillis() / 1000
        this.comment = ""
    }

    constructor() {
        this.id = null
        this.latitude = 0f
        this.longitude = 0f
        this.time = 0
        this.comment = ""
    }
}

class StorageSQL(
    context: Context,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_LOCATION_TABLE = ("CREATE TABLE " + TABLE_NAME + " " +
                "(" + COLUMN_ID + " INTEGER PRIMARY KEY, " +
                LAT + " FLOAT, " + LOT + " FLOAT, "
                + DTT + " INT, " + CMT + " TEXT)")
        db?.execSQL(CREATE_LOCATION_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            val ADD_COMMENT_COLUMN = "ALTER TABLE $TABLE_NAME ADD COLUMN $CMT TEXT"
            try {
                db?.execSQL(ADD_COMMENT_COLUMN)
            } catch (e: java.lang.Exception) {
                Log.d("WoWarDas", "Could not alter table: $e")
            }
        }
    }

    fun addEntry(context: Context, entry: Entry) {
        val values = ContentValues()
        values.put(LAT, entry.latitude)
        values.put(LOT, entry.longitude)
        values.put(DTT, entry.time)
        values.put(CMT, entry.comment)
        val db = this.writableDatabase
        try {
            db.insert(TABLE_NAME, null, values)
            Toast.makeText(context, context.getString(R.string.location_added), Toast.LENGTH_SHORT)
            db.close()
        } catch (e: Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun updateEntry(context: Context, entry: Entry) {
        val values = ContentValues()
        values.put(LAT, entry.latitude)
        values.put(LOT, entry.longitude)
        values.put(DTT, entry.time)
        values.put(CMT, entry.comment)
        val db = this.writableDatabase
        try {
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(entry.id.toString()))
            Toast.makeText(context, context.getString(R.string.comment_added), Toast.LENGTH_SHORT)
            db.close()
        } catch (e: java.lang.Exception) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    fun getAllEntries(context: Context): ArrayList<Entry> {
        val resultList = ArrayList<Entry>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM location ORDER BY _id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val entry = Entry(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getFloat(cursor.getColumnIndex(LAT)),
                    cursor.getFloat(cursor.getColumnIndex(LOT)),
                    cursor.getLong(cursor.getColumnIndex(DTT)),
                    cursor.getString(cursor.getColumnIndex(CMT))
                )
                resultList.add(entry)
            } while (cursor.moveToNext())
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_records_found),
                Toast.LENGTH_SHORT
            ).show()
        }
        cursor.close()
        db.close()
        return resultList
    }

    fun deleteEntryByID(id: Int): Boolean {
        var result = false
        val db = this.writableDatabase
        try {
            //db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_ID=$id")
            db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
            Log.d("WoWarDas", "Item $id deleted from database.")
            result = true
        } catch (e: Exception) {
            Log.d("WoWarDas", "Could not delete item $id from database: $e")
        }
        db.close()
        return result
    }

    fun getEntryByID(id: Int): Entry {
        val entry = Entry()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM location WHERE _id = ${id}", null)
        if (cursor.moveToFirst()) {
            entry.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            entry.latitude = cursor.getFloat(cursor.getColumnIndex(LAT))
            entry.longitude = cursor.getFloat(cursor.getColumnIndex(LOT))
            entry.time = cursor.getLong(cursor.getColumnIndex(DTT))
            entry.comment = cursor.getString(cursor.getColumnIndex(CMT))
        }
        return entry
    }

    fun deleteAllEntries(context: Context) {
        val db = this.writableDatabase
        try {
            db.delete(TABLE_NAME, null, null)
            Toast.makeText(context, "All locations deleted.", Toast.LENGTH_LONG).show()
        } catch (e: java.lang.Exception) {
            Toast.makeText(context, "Error deleting all locations: $e", Toast.LENGTH_LONG).show()
        }
        db.close()
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "locations.db"
        const val TABLE_NAME = "location"
        const val COLUMN_ID = "_id"
        const val LAT = "latitude"
        const val LOT = "longitude"
        const val DTT = "datetime"
        const val CMT = "comment"
    }
}