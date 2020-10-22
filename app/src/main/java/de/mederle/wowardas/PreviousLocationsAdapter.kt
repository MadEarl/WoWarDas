package de.mederle.wowardas

import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
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
        holder.idView.append(cursor.getInt(0).toString())
        holder.latView.append(cursor.getFloat(1).toString())
        holder.lotView.append(cursor.getFloat(2).toString())
        holder.dttView.append(
            Instant.ofEpochSecond(cursor.getLong(3)).atZone(ZoneId.systemDefault())
                .toLocalDateTime().toString()
        )
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    inner class LocationHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        val idView: TextView = itemView.findViewById(R.id.tv_id)
        val latView: TextView = itemView.findViewById(R.id.tv_lat)
        val lotView: TextView = itemView.findViewById(R.id.tv_lot)
        val dttView: TextView = itemView.findViewById(R.id.tv_dtt)

        override fun onClick(v: View?) {
            TODO("Not yet implemented")
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
