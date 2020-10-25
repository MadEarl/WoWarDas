package de.mederle.wowardas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.nio.charset.Charset


class ExportedLocationsBucket private constructor() {
    var locationsToExport = mutableListOf<Entry>()

    companion object {
        val instance = ExportedLocationsBucket()
    }
}

class LocationViewActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private val CREATE_FILE = 148

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                val uri = data.data
                Log.d("WoWarDas", "Got file URI in LocationViewActivity: $uri")
                val exportedLocationsBucket: ExportedLocationsBucket =
                    ExportedLocationsBucket.instance
                val resultGpx =
                    GpxFileWriter(exportedLocationsBucket.locationsToExport).getContents()
                val fileWriter = contentResolver.openOutputStream(uri!!, "w")
                if (fileWriter != null) {
                    fileWriter.write(resultGpx.toByteArray(Charset.defaultCharset()))
                    fileWriter.close()
                    Toast.makeText(this, "Export in GPX-Datei erfolgreich!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, "Konnte Datei nicht schreiben!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
    }

}
