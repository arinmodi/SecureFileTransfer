package com.example.video_share_app.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FileDao {

    // adding a new entry to our database.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(file : File)

    // delete entry from database
    @Delete
    suspend fun delete(file : File)

    @Query("Select * from FilesInfo order by id DESC")
    fun getAllFiles(): LiveData<List<File>>

}