package com.example.video_share_app.repostory

import androidx.lifecycle.LiveData
import com.example.video_share_app.room.File
import com.example.video_share_app.retrofit.FileService
import com.example.video_share_app.retrofit.RetrofitService
import com.example.video_share_app.room.FileDao
import com.example.video_share_app.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody

class FileRepository (private val fileDao : FileDao) {

    private val fileService : FileService = RetrofitService
        .getInstance().create(FileService::class.java)

    val allFiles : LiveData<List<File>> = fileDao.getAllFiles()

    /**
     * Upload file to firebase and store mapping in fire-store
     * @file : Encrypted File
     * @expiry : Expiry of the file
     *
     * @return Resource, Success or Error
     */
    suspend fun uploadFile(file:MultipartBody.Part,expiry:RequestBody) : Resource<Any> {
        return try {
            val response = fileService.uploadImage(file,expiry)
            val result = response.body()

            if (response.isSuccessful) {
                Resource.Success(result!!)
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    // insert entry in sqlite database
    suspend fun insert(file : File) : Resource<Any> {
        return try {
            fileDao.insert(file)
            Resource.Success("Success")
        } catch(e : Exception) {
            Resource.Error(e.message.toString())
        }
    }

    // delete entry in sqlite database
    suspend fun delete(file : File) {
        fileDao.delete(file)
    }

}