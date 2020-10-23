package de.mederle.wowardas

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.mederle.wowardas.StorageSQL.Companion.COLUMN_ID
import de.mederle.wowardas.StorageSQL.Companion.DTT
import de.mederle.wowardas.StorageSQL.Companion.LAT
import de.mederle.wowardas.StorageSQL.Companion.LOT
import java.time.Instant
import java.time.ZoneId


class PreviousLocationsAdapter(private val cursor: Cursor, private val itemClickListener: AdapterView.OnItemClickListener) :
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
        holder.idView.append(dbId.toString())
        holder.latView.append(cursor.getFloat(cursor.getColumnIndex(LAT)).toString())
        holder.lotView.append(cursor.getFloat(cursor.getColumnIndex(LOT)).toString())
        holder.dttView.append(
                Instant.ofEpochSecond(cursor.getLong(cursor.getColumnIndex(DTT))).atZone(ZoneId.systemDefault())
                        .toLocalDateTime().toString()
        )
        holder.itemView.tag = dbId
        holder.optsView
        holder.bind(holder.itemView, itemClickListener)

    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    inner class LocationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val idView: TextView = itemView.findViewById(R.id.tv_id)
        val latView: TextView = itemView.findViewById(R.id.tv_lat)
        val lotView: TextView = itemView.findViewById(R.id.tv_lot)
        val dttView: TextView = itemView.findViewById(R.id.tv_dtt)
        val optsView: TextView = itemView.findViewById(R.id.tv_opts)

        override fun onClick(v: View?) {
            Log.d("WoWarDas", "Item clicked!")
            val context = v?.context
            val popup = PopupMenu(context, v)
            popup.inflate(R.menu.menu_location_view)
            popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener(
                    fun(item: MenuItem): Boolean {
                        var result: Boolean
                        when(item.itemId) {
                            R.id.multi_select_gpx -> {
                                Log.d("WoWarDas", "MultiSelect GPX selected")
                                result = true
                            }
                            R.id.copy_clipboard -> result = true
                            R.id.export_gpx -> result = true
                            else -> result = false
                        }
                        return result
                    }
            ))
            popup.show()
        }
        fun bind(holder: LocationHolder, itemClickListener: AdapterView.OnItemClickListener) {
            itemView.setOnClickListener {
                itemClickListener.onItemClick(holder.itemView)
            }
        }
    }


}


//(private val context: Context, private val cursor: Cursor): BaseAdapter() {
//    override fun getCount(): Int {
//        return cursor.count
//    }
/*

    override fun getItem(position: Int): Entry {
        cursor.moveToPosition(position)
        return  Entry(cursor.getInt(0), cursor.getFloat(1), cursor.getFloat(2), cursor.getLong(3))
    }

    override fun getItemId(position: Int): Long {
        return cursor.getInt(0).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.row_view, parent, false)
        cursor.moveToPosition(position)
        val tvId: TextView = convertView!!.findViewById(R.id.tv_id)
        val tvLat: TextView = convertView.findViewById(R.id.tv_lat)
        val tvLot: TextView = convertView.findViewById(R.id.tv_lot)
        val tvDtt: TextView = convertView.findViewById(R.id.tv_dtt)
        tvId.text = cursor.getInt(0).toString()
        tvLat.text = cursor.getFloat(1).toString()
        tvLot.text = cursor.getFloat(2).toString()
        tvDtt.text = cursor.getLong(3).toString()
        return convertView
    }
}*/
