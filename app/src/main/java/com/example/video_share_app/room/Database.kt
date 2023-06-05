package com.example.video_share_app.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

@androidx.room.Database(entities = [File::class], version = 2, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun getFileDao(): FileDao

    companion object {
        // Singleton prevents multiple
        // instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: Database? = null

        fun getDatabase(context: Context): Database {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Database::class.java,
                    "file_database"
                ).addCallback(FileDatabaseCallBack(context)).build()
                INSTANCE = instance
                instance
            }
        }
    }
}