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
        setSupportActionBar(findViewById(R.id.loc_view_toolbar))
        title = "WoWarDas Ortssammlung"
        linearLayoutManager = LinearLayoutManager(this)
        val recyclerView: RecyclerView = findViewById(R.id.loc_view)
        recyclerView.layoutManager = this.linearLayoutManager
        val locAdapter = PreviousLocationsAdapter(StorageSQL(this, null).getAllEntriesCursor(this))
        Log.d("WoWarDas", "locAdapter.count = " + locAdapter.itemCount)
        recyclerView.adapter = locAdapter
        //locAdapter.LocationHolder(recyclerView).onClick(recyclerView)
    }
}
