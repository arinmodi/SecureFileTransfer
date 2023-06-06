package com.genz.secure_share.room

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Dao class for operating with
 * SQLite DB(room)
 */
@Dao
interface FileDao {

    // adding a new entry to our database.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(file : File)

    // delete entry from database
    @Delete
    suspend fun delete(file : File)

    // get all data from local DB(SQLite)
    @Query("Select * from FilesInfo where Expiry > (:Expiry) order by id DESC")
    fun getAllFiles(Expiry : String): LiveData<List<File>>

}