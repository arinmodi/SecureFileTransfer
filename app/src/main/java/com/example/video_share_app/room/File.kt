package com.example.video_share_app.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// refers to the table name for sqlite database
@Entity(tableName = "FilesInfo")

class File(
    @ColumnInfo("pass") val pass : String,
    @ColumnInfo("SearchKey") val searchKey : String,
    @ColumnInfo("Expiry") val expiry : String,
    @ColumnInfo("Algorithm") val algo: String
) {
    // primary key, starting with 0
    @PrimaryKey(autoGenerate = true) var id = 0
}