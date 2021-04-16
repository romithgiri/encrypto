package com.rohit.encrypto.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rohit.encrypto.R
import com.rohit.encrypto.database.NoteDB
import com.rohit.encrypto.database.NoteEntity
import com.rohit.encrypto.recycler_view.CardAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var noteDB: NoteDB
    private var noteList: MutableList<NoteEntity> = ArrayList()

    private var recyclerView: RecyclerView? = null
    private lateinit var adapter: CardAdapter
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private lateinit var btnCreate: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteDB = Room.databaseBuilder(this, NoteDB::class.java, "NoteDB").build()
        btnCreate = findViewById(R.id.btnCrete)
        recyclerView = findViewById(R.id.mRecyclerView)
        recyclerViewLayoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = recyclerViewLayoutManager

        btnCreate.setOnClickListener {
            var intent = Intent(this, CreateNote::class.java)
            intent.putExtra("action", "save")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        setDataInRecyclerView()
    }

    private fun setDataInRecyclerView() {
        println("================== noteList 0:")
        GlobalScope.launch {
            noteList = noteDB.noteDAO().getAllNotes()
            runOnUiThread {
                try {
                    println("================== noteList 1:")
                    println("================== noteList 2:")
                    for (i in noteList) {
                        println("================== noteList 3: ${i.noteTitle}")
                    }
                    adapter = CardAdapter(
                        this@MainActivity,
                        noteList
                    )
                    recyclerView!!.adapter = adapter

                    adapter.deleteTrustedUserClickListener = object : CardAdapter.DeleteClickListener {
                        override fun onBtnClick(noteEntity: NoteEntity) {
                            var currentData = noteEntity
                            GlobalScope.launch {
                                noteDB.noteDAO().deleteNote(noteEntity)
                                runOnUiThread {
                                    setDataInRecyclerView()
                                }
                            }
                        }
                    }

                    adapter.editTrustedUserInfoClickListener = object : CardAdapter.EditClickListener{
                        override fun onBtnClick(noteEntity: NoteEntity) {
                            var intent = Intent(this@MainActivity, CreateNote::class.java)
                            intent.putExtra("action", "edit")
                            intent.putExtra("pk", noteEntity.pkId)
                            intent.putExtra("title", noteEntity.noteTitle)
                            intent.putExtra("description", noteEntity.noteDescription)
                            intent.putExtra("date", noteEntity.noteDate)
                            startActivity(intent)
                        }
                    }
                } catch (e: Exception) {
                    println("================== noteList 4: $e")
                }
            }
        }
    }
}