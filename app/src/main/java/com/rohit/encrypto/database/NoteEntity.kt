package com.rohit.encrypto.database

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
class NoteEntity {
    @PrimaryKey
    var pkId: Long = 0

    @ColumnInfo(name = "NoteDate")
    var noteDate: String ?= ""

    @ColumnInfo(name = "NoteTitle")
    var noteTitle: String ?= ""

    @ColumnInfo(name = "NoteDescription")
    var noteDescription: String? = ""
}