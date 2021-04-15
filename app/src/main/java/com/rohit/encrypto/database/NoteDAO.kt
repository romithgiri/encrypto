package com.rohit.encrypto.database

import androidx.room.*

@Dao
interface NoteDAO {
    @Insert
    fun saveNoteUserInDB(ruleItem: NoteEntity)

    @Query(value = "Select * from NoteEntity")
    fun getAllNotes() : MutableList<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun updateAndSaveNote(note: NoteEntity)

    @Delete
    fun deleteNote(note: NoteEntity)
}