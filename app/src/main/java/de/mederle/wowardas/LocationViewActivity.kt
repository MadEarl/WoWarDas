package de.mederle.wowardas

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LocationViewActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_view)
        linearLayoutManager = LinearLayoutManager(this)
        val recyclerView: RecyclerView = findViewById(R.id.loc_view)
        recyclerView.layoutManager = this.linearLayoutManager

        //val listView = findViewById<LinearLayout>(R.id.loc_layout)
        val locAdapter = PreviousLocationsAdapter(StorageSQL(this, null).getAllEntriesCursor(this))
        Log.d("WoWarDas", "locAdapter.count = " + locAdapter.itemCount)
        // ArrayAdapter(this, android.R.layout.simple_list_item_1, getPreviousLocationsArray())
        recyclerView.adapter = locAdapter
        //val convertView = layoutInflater.inflate(R.layout.row_view, listView, false)
        //for (i in 0 until (locAdapter.itemCount - 1)) {
        //    locAdapter.getView(i, convertView, findViewById(R.id.loc_layout))
        //}
    }

    /*private fun getPreviousLocationsArray(): MutableList<String> {
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
    } */


}


