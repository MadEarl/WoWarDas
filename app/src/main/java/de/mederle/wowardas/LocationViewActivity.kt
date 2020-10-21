package de.mederle.wowardas

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.time.Instant
import java.time.ZoneId

class LocationViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_view)
        val listView = findViewById<ListView>(R.id.prv_loc_list)
        val locAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, getPreviousLocationsArray())
        listView.adapter = locAdapter
    }

    private fun getPreviousLocationsArray(): MutableList<String> {
        val locationEntries = StorageSQL(this, null).getAllEntries(this)
        val returnList = mutableListOf<String>()
        val iterator = locationEntries.iterator()
        iterator.forEach {
            var strung = "\n"
            strung += "ID: " + it.id.toString() + "\n"
            strung += "Datum: " + Instant.ofEpochSecond(it.time).atZone(ZoneId.systemDefault())
                .toLocalDateTime().toString() + "\n"
            strung += "Breitengrad: " + it.latitude.toString() + "\n"
            strung += "Längengrad: " + it.longitude.toString() + "\n"
            //Log.d("Location string: ", strung)
            returnList.add(strung)
        }
        return returnList
    }


}


