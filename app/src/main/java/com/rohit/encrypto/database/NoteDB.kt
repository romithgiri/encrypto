package com.rohit.encrypto.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [(NoteEntity::class)],version = 1)
abstract class NoteDB: RoomDatabase() {
    abstract fun noteDAO(): NoteDAO
}