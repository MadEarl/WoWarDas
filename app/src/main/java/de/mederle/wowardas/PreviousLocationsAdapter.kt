package de.mederle.wowardas

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import de.mederle.wowardas.StorageSQL.Companion.COLUMN_ID
import de.mederle.wowardas.StorageSQL.Companion.DTT
import de.mederle.wowardas.StorageSQL.Companion.LAT
import de.mederle.wowardas.StorageSQL.Companion.LOT
import java.time.Instant
import java.time.ZoneId
import java.util.*

class PreviousLocationsAdapter(private val cursor: Cursor) :
        RecyclerView.Adapter<PreviousLocationsAdapter.LocationHolder>() {

    private val CREATE_FILE = 148
    val selectedItems = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.row_view, parent, false)
        return LocationHolder(contactView)
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        cursor.moveToPosition(position)
        val entry = Entry(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getFloat(cursor.getColumnIndex(LAT)),
                cursor.getFloat(cursor.getColumnIndex(LOT)),
                cursor.getLong(cursor.getColumnIndex(DTT)))
        holder.idView.text = "ID: ${entry.id}"
        holder.latView.text = "Breitengrad: ${entry.latitude}"
        holder.lotView.text = "Längengrad:  ${entry.longitude}"
        holder.dttView.text = "Datum: ${
            Instant.ofEpochSecond(entry.time)
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()
        }"
        holder.itemView.tag = position
        if (selectedItems.contains(position)) {
            holder.itemView.alpha = 0.3f
        } else {
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    override fun getItemId(position: Int): Long {
        return cursor.position.toLong()
    }

    private fun selectItem(holder: LocationHolder, id: Int) {
        if (selectedItems.contains(id)) {
            selectedItems.remove(id)
            Log.d("WoWarDas", "Unselecting id $id = holder ${holder.itemView.tag}")
            holder.itemView.alpha = 1.0f
        } else {
            selectedItems.add(id)
            Log.d("WoWarDas", "Selecting id $id = holder ${holder.itemView.tag}")
            holder.itemView.alpha = 0.3f
        }
    }

    private fun getEntryByID(cursor: Cursor, id: Int): Entry {
        val entry = Entry()
        if (cursor.moveToPosition(id)) {
            entry.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
            entry.latitude = cursor.getFloat(cursor.getColumnIndex(LAT))
            entry.longitude = cursor.getFloat(cursor.getColumnIndex(LOT))
            entry.time = cursor.getLong(cursor.getColumnIndex(DTT))
        }
        return entry
    }

    private fun makeClip(selectedIds: ArrayList<Int>): String {
        val clipBuilder = StringBuilder()
        for (id in selectedIds) {
            val entry = getEntryByID(cursor, id)
            clipBuilder.append("ID: ${entry.id}\nBreitengrad: ${entry.latitude}\n")
            clipBuilder.append("Längengrad: ${entry.longitude}\n")
            clipBuilder.append(
                "Datum: ${
                    Instant.ofEpochSecond(entry.time)
                        .atZone(ZoneId.systemDefault()).toLocalDateTime()
                }\n"
            )
            clipBuilder.append("-------\n")
        }
        return clipBuilder.toString()
    }

    private fun makeSelectedEntryList(selectedIds: ArrayList<Int>): MutableList<Entry> {
        val resultList = mutableListOf<Entry>()
        for (id in selectedIds) {
            val entry = getEntryByID(cursor, id)
            resultList.add(entry)
        }
        return resultList
    }

    fun getSelectedItems(itemList: ArrayList<Entry>) {
        for (item in selectedItems) {
            itemList.add(getEntryByID(cursor, item))
        }
    }

    inner class LocationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener,
        View.OnLongClickListener {
        val idView: TextView = itemView.findViewById(R.id.tv_id)
        val latView: TextView = itemView.findViewById(R.id.tv_lat)
        val lotView: TextView = itemView.findViewById(R.id.tv_lot)
        val dttView: TextView = itemView.findViewById(R.id.tv_dtt)

        init {
            v.setOnClickListener(this)
            // setHasStableIds(true)
        }

        override fun onClick(v: View?) {
            Log.d("WoWarDas", "Item clicked!")
            val context = v?.context
            val popup = PopupMenu(context, v)
            popup.inflate(R.menu.menu_location_view)
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener(
                fun(item: MenuItem): Boolean {
                    Log.d("WoWarDas", "Clickable item clicked")
                    return when (item.itemId) {
                        R.id.select_unselect -> {
                            selectItem(this, this.itemView.tag as Int)
                            Log.d("WoWarDas", "Selected: ${selectedItems.joinToString()}")
                            true
                        }
                        R.id.copy_clipboard -> {
                            val locationText = makeClip(selectedItems)
                            val clipboard: ClipboardManager =
                                v?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip: ClipData = ClipData.newPlainText("simple Text", locationText)
                            clipboard.setPrimaryClip(clip)
                            true
                        }
                        R.id.export_gpx -> {
                            if (selectedItems.size < 1) {
                                Log.d("WoWarDas", "No item selected for export.")
                                Toast.makeText(
                                    v?.context,
                                    "Kein Eintrag gewählt!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // need to store in global container as onActivityResult is out of this scope
                                val exportedLocationsBucket: ExportedLocationsBucket =
                                    ExportedLocationsBucket.instance
                                exportedLocationsBucket.locationsToExport =
                                    makeSelectedEntryList(selectedItems)
                                val fileUriIntent = ActivityResultContracts.CreateDocument()
                                    .createIntent(v!!.context, "WoWarDas.gpx")
                                startActivityForResult(
                                    v.context as Activity,
                                    fileUriIntent,
                                    CREATE_FILE,
                                    null
                                )
                                // further processing in LocationViewActivity.onActivityResult()
                            }

                            true
                        }
                        else -> false
                    }
                }
            ))
            popup.show()
        }

        override fun onLongClick(v: View?): Boolean {
            TODO("Not yet implemented")
        }
    }
}
