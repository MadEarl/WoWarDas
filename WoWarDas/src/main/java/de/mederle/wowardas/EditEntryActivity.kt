package de.mederle.wowardas

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.ZoneId

class EditEntryActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_entry)
        val entryID: Int = intent.extras!!.getInt("ENTRY_ID")
        val entry: Entry = MainActivity.storageSQL.getEntryByID(entryID)
        val idView = findViewById<TextView>(R.id.edit_id)
        val latView = findViewById<TextView>(R.id.edit_latitude)
        val lotView = findViewById<TextView>(R.id.edit_longitude)
        val dateView = findViewById<TextView>(R.id.edit_date)
        val cmtEditView = findViewById<TextInputEditText>(R.id.loc_info_edit)
        val cancelButton = findViewById<Button>(R.id.btn_cancel_edit)
        val saveButton = findViewById<Button>(R.id.btn_save)
        idView.text = "${entry.id}"
        latView.text = "${entry.latitude}"
        lotView.text = "${entry.longitude}"
        dateView.text = "${Instant.ofEpochSecond(entry.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()}"
        //cmtEditView.hint = getString(R.string.loc_info_edit_hint)
        cmtEditView.setText(entry.comment, TextView.BufferType.EDITABLE)
        cancelButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
        }
        saveButton.setOnClickListener {
            entry.comment = cmtEditView.text.toString()
            MainActivity.storageSQL.updateEntry(this, entry)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}