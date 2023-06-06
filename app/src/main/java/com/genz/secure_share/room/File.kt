package com.genz.secure_share.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// refers to the table name for sqlite database
@Entity(tableName = "FilesInfo")

/**
 * File Entity Saved in DB
 */
class File(
    @ColumnInfo("name") val name : String,
    @ColumnInfo("pass") val pass : String,
    @ColumnInfo("SearchKey") val searchKey : String,
    @ColumnInfo("Expiry") val expiry : String,
    @ColumnInfo("Algorithm") val algo: String
) {
    // primary key, starting with 0
    @PrimaryKey(autoGenerate = true) var id = 0
}