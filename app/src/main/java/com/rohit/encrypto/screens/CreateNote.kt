package com.rohit.encrypto.screens

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.rohit.encrypto.R
import com.rohit.encrypto.database.NoteDB
import com.rohit.encrypto.database.NoteEntity
import com.rohit.encrypto.encryption_decryption.EncAndDecUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CreateNote : AppCompatActivity() {
    private lateinit var noteDB: NoteDB
    private lateinit var action: String
    private lateinit var btnSave: FloatingActionButton
    private lateinit var editTitle: EditText
    private lateinit var editDescription: EditText
    private lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        noteDB = Room.databaseBuilder(this, NoteDB::class.java, "NoteDB").build()
        btnSave = findViewById(R.id.btnSave)
        editTitle = findViewById(R.id.editTitle)
        editDescription = findViewById(R.id.editDescription)
        backBtn = findViewById(R.id.btnCreateBack)

        var extract = intent.extras
        action = extract!!.getString("action").toString()
        if (action == "edit") {
            editTitle.setText(extract.getString("title").toString())
            val obj: EncAndDecUtil.SecuredData = Gson().fromJson(extract.getString("description"), EncAndDecUtil.SecuredData::class.java)
            var decryptedDataStr = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                EncAndDecUtil().decryptString(
                    obj.value,
                    obj.encryptedValue
                )
            } else {
                null
            }
            println("======================== val 1: $decryptedDataStr")
            editDescription.setText(decryptedDataStr)
        }

        backBtn.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            if (editTitle.text.isNullOrBlank() || editDescription.text.isNullOrBlank()) {
                if (editTitle.text.isNullOrBlank()) {
                    Toast.makeText(this, "Please Enter Title...", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please Enter Note...", Toast.LENGTH_SHORT).show()
                }
            } else {
                GlobalScope.launch {
                    try {
                        var noteEntity = NoteEntity()
                        if (action == "edit") {
                            noteEntity.pkId = extract.getLong("pk")
                            noteEntity.noteDate = extract.getString("date")
                        } else {
                            val pattern = "dd-MMM-yyyy"
                            val simpleDateFormat = SimpleDateFormat(pattern)
                            val date: String = simpleDateFormat.format(Date())
                            noteEntity.pkId = Date().time
                            noteEntity.noteDate = date
                        }
                        noteEntity.noteTitle = editTitle.text.toString()
                        noteEntity.noteDescription = Gson().toJson(EncAndDecUtil().encryptString(editDescription.text.toString()))
                        noteDB.noteDAO().updateAndSaveNote(noteEntity)
                        finish()
                    } catch (e: Exception) {
                        println("+++++++++++++++++++++++++++++++: Error: $e")
                    }
                }
            }
        }
    }
}