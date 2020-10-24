package de.mederle.wowardas

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
import androidx.recyclerview.widget.RecyclerView
import de.mederle.wowardas.StorageSQL.Companion.COLUMN_ID
import de.mederle.wowardas.StorageSQL.Companion.DTT
import de.mederle.wowardas.StorageSQL.Companion.LAT
import de.mederle.wowardas.StorageSQL.Companion.LOT
import java.time.Instant
import java.time.ZoneId


class PreviousLocationsAdapter(private val cursor: Cursor) :
        RecyclerView.Adapter<PreviousLocationsAdapter.LocationHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.row_view, parent, false)
        return LocationHolder(contactView)
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        cursor.moveToPosition(position)
        val dbId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
        holder.idView.text = "ID: $dbId"
        holder.latView.text = "Breitengrad: ${cursor.getFloat(cursor.getColumnIndex(LAT))}"
        holder.lotView.text = "Längengrad:  ${cursor.getFloat(cursor.getColumnIndex(LOT))}"
        holder.dttView.text = "Datum: ${
            Instant.ofEpochSecond(cursor.getLong(cursor.getColumnIndex(DTT)))
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()
        }"
        holder.itemView.tag = dbId
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    inner class LocationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {
        val idView: TextView = itemView.findViewById(R.id.tv_id)
        val latView: TextView = itemView.findViewById(R.id.tv_lat)
        val lotView: TextView = itemView.findViewById(R.id.tv_lot)
        val dttView: TextView = itemView.findViewById(R.id.tv_dtt)

        init {
            v.setOnClickListener(this)
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
                            R.id.multi_select_gpx -> {
                                Log.d("WoWarDas", "MultiSelect GPX selected")
                                true
                            }
                            R.id.copy_clipboard -> {
                                val locationText = "Breitengrad: ${cursor.getFloat(cursor.getColumnIndex(LAT))}\nLängengrad: ${cursor.getFloat(cursor.getColumnIndex(LOT))}"
                                val clipboard: ClipboardManager = v?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip: ClipData = ClipData.newPlainText("simple Text", locationText)
                                clipboard.setPrimaryClip(clip)
                                true
                            }
                            R.id.export_gpx -> {


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
