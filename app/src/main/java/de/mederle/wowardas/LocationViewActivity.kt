package de.mederle.wowardas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.mederle.wowardas.MainActivity.Companion.storageSQL
import java.nio.charset.Charset


class ExportedLocationsBucket private constructor() {
    var locationsToExport = mutableListOf<Entry>()

    companion object {
        val instance = ExportedLocationsBucket()
    }
}

class LocationViewActivity : AppCompatActivity() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PreviousLocationsAdapter.CREATE_FILE && resultCode == RESULT_OK) {
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
                    Toast.makeText(this, getString(R.string.gpx_export_success), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(this, getString(R.string.gpx_export_fail), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        if (requestCode == PreviousLocationsAdapter.UPDATE_COMMENT && resultCode == RESULT_OK) {
            viewLocations()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_location_view_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_db) {
            Log.d("WoWarDas", "Clear database requested.")
            val alertDialog: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.delete_warning_title))
                    .setMessage(getString(R.string.delete_warning_text))
                    .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                        storageSQL.deleteAllEntries(this)
                        viewLocations()
                    }
                    .setNegativeButton(getString(R.string.no)) { _, _ -> }
            alertDialog.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.loc_view_toolbar)
        setSupportActionBar(toolbar)
        title = getString(R.string.previos_locations_title)
        storageSQL = StorageSQL(this, null, null, 1)

        viewLocations()

    }

    private fun viewLocations() {
        val locationsList = storageSQL.getAllEntries(this)
        val locAdapter = PreviousLocationsAdapter(this, locationsList)
        val recyclerView: RecyclerView = findViewById(R.id.loc_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = locAdapter
    }


}
