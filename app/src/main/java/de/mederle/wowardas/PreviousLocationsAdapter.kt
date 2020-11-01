package de.mederle.wowardas

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.Instant
import java.time.ZoneId

class PreviousLocationsAdapter(val context: Context, val locations: ArrayList<Entry>) :
    RecyclerView.Adapter<PreviousLocationsAdapter.LocationHolder>() {

    companion object {
        const val CREATE_FILE = 148
        const val UPDATE_COMMENT = 149
    }

    val selectedItems = arrayListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val contactView = inflater.inflate(R.layout.row_view, parent, false)
        return LocationHolder(contactView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        val entry: Entry = locations[position]
        holder.idView.text = holder.itemView.context.getString(R.string.id) + entry.id
        holder.latView.text = holder.itemView.context.getString(R.string.latitude) + entry.latitude
        holder.lotView.text =
                holder.itemView.context.getString(R.string.longitude) + entry.longitude
        holder.dttView.text = holder.itemView.context.getString(R.string.timestamp) +
                Instant.ofEpochSecond(entry.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
        if (entry.comment != "") holder.cmtView.text =
                context.getString(R.string.comment_loc) + entry.comment
        holder.itemView.tag = position
        if (selectedItems.contains(entry.id)) {
            holder.cardWrapper.setBackgroundResource(R.color.purple_200)
        } else {
            holder.cardWrapper.setBackgroundResource(R.color.transparent)
        }
        holder.deleteButton.setOnClickListener {
            val alertDialog: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(context)
                    .setTitle(context.getString(R.string.delete_warning_title))
                    .setMessage(context.getString(R.string.warning_delete_entry_text) + "${entry.id}")
                    .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                        if (MainActivity.storageSQL.deleteEntryByID(entry.id!!)) {
                            locations.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, locations.size)
                        }
                    }
                    .setNegativeButton(context.getString(R.string.no)) { _, _ -> }
            alertDialog.show()
        }
        holder.editButton.setOnClickListener {
            val intent = Intent(context, EditEntryActivity::class.java)
            intent.putExtra("ENTRY_ID", entry.id)
            startActivityForResult(context as Activity, intent, 149, null)
        }
    }

    override fun getItemCount(): Int {
        return locations.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun selectItem(v: View, position: Int) {
        val entry = locations[position]
        if (selectedItems.contains(entry.id)) {
            selectedItems.remove(entry.id)
            Log.d("WoWarDas", "Deselecting entry at $position = ${entry.id}")
            //v.alpha = 1.0f
            notifyItemChanged(position)
        } else {
            selectedItems.add(entry.id!!)
            Log.d("WoWarDas", "Selecting entry at $position = entry ${entry.id}}")
            //v.alpha = 0.6f
            notifyItemChanged(position)
        }
    }

    private fun makeClip(selectedIds: ArrayList<Int>): String {
        val clipBuilder = StringBuilder()
        for (id in selectedIds) {
            val entry = MainActivity.storageSQL.getEntryByID(id)
            val commentString = if (entry.comment != "") entry.comment.toString() else "ID: " + entry.id.toString()
            clipBuilder.append(
                    commentString + "\n" + context.getString(
                            R.string.latitude
                    ) + entry.latitude + "\n"
            )
            clipBuilder.append(context.getString(R.string.longitude) + entry.longitude + "\n")
            clipBuilder.append(
                context.getString(R.string.timestamp) +
                        Instant.ofEpochSecond(entry.time).atZone(ZoneId.systemDefault())
                            .toLocalDateTime() + "\n"
            )
            clipBuilder.append("-------\n")
        }
        return clipBuilder.toString()
    }

    private fun makeSelectedEntryList(selectedIds: ArrayList<Int>): MutableList<Entry> {
        val resultList = mutableListOf<Entry>()
        for (id in selectedIds) {
            val entry = MainActivity.storageSQL.getEntryByID(id)
            resultList.add(entry)
        }
        return resultList
    }

    fun getSelectedItems(itemList: ArrayList<Entry>) {
        for (item in selectedItems) {
            itemList.add(MainActivity.storageSQL.getEntryByID(item))
        }
    }

    inner class LocationHolder(v: View) : RecyclerView.ViewHolder(v),
        View.OnClickListener,
        View.OnLongClickListener {
        val idView: TextView = v.findViewById(R.id.tv_id)
        val latView: TextView = v.findViewById(R.id.tv_lat)
        val lotView: TextView = v.findViewById(R.id.tv_lot)
        val dttView: TextView = v.findViewById(R.id.tv_dtt)
        val cmtView: TextView = v.findViewById(R.id.tv_cmt)
        val deleteButton: Button = v.findViewById(R.id.btn_delete_location)
        val editButton: Button = v.findViewById(R.id.btn_edit_cmt)
        val cardWrapper: View = v.findViewById(R.id.card_wrapper)

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
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
                            R.id.select_deselect -> {
                                selectItem(v!!, adapterPosition)
                                Log.d("WoWarDas", "Selected: ${selectedItems.joinToString()}")
                                true
                            }
                            R.id.copy_clipboard -> {
                                if (selectedItems.size < 1) {
                                    Log.d("WoWarDas", "No item selected for export.")
                                    Toast.makeText(
                                            v?.context,
                                            context?.getString(R.string.no_entry_selected),
                                            Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val locationText = makeClip(selectedItems)
                                    val clipboard: ClipboardManager =
                                            v?.context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip: ClipData =
                                            ClipData.newPlainText("simple Text", locationText)
                                    clipboard.setPrimaryClip(clip)
                                }
                                true
                            }
                            R.id.export_gpx -> {
                                if (selectedItems.size < 1) {
                                    Log.d("WoWarDas", "No item selected for export.")
                                    Toast.makeText(
                                            v?.context,
                                            context?.getString(R.string.no_entry_selected),
                                            Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // need to store in global container as onActivityResult is out of this scope
                                    val exportedLocationsBucket: ExportedLocationsBucket =
                                            ExportedLocationsBucket.instance
                                    exportedLocationsBucket.locationsToExport =
                                            makeSelectedEntryList(selectedItems)
                                    val fileUriIntent = ActivityResultContracts.CreateDocument()
                                            .createIntent(v!!.context, context!!.getString(R.string.wowardas_gpx))
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

        override fun onLongClick(v: View): Boolean {
            selectItem(v, adapterPosition)
            Log.d("WoWarDas", "Selected: ${selectedItems.joinToString()}")

            return true

        }
    }
}
