package com.example.video_share_app.repostory

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.video_share_app.model.FileResponse
import com.example.video_share_app.retrofit.GetFileService
import com.example.video_share_app.retrofit.RetrofitService

class ViewRepository {

    private val getFileService = RetrofitService.getInstance().create(GetFileService::class.java)

    private val fileLiveData = MutableLiveData<FileResponse?>()
    val file : LiveData<FileResponse?>
    get() = fileLiveData

    suspend fun getFile(searchKey : String) {
        Log.e("Key : ", searchKey)
        try {
            val result = getFileService.getFile(searchKey)
            if (result.body() != null) {
                Log.i("Result : ", result.body().toString())
                fileLiveData.postValue(result.body())
            } else {
                fileLiveData.postValue(null)
            }
        }catch(e:Exception) {
            e.printStackTrace()
            fileLiveData.postValue(null)
        }
    }
}