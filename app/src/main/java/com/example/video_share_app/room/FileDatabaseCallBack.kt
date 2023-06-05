package com.example.video_share_app.room

import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Just For Testing, Insert the the data with expiry list
 */
class FileDatabaseCallBack(private val context: Context) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val dao = Database.getDatabase(context).getFileDao()
            try {
                dao.insert(File("demo", "123456789", "89454154",
                    "2023-06-04", "AES-128"))
            }catch(e : Exception) {
                Log.e("Error : ", "Inserting Expire Data")
                e.printStackTrace()
            }
        }
    }
}