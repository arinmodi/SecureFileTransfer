package com.genz.secure_share.repostory

import androidx.lifecycle.LiveData
import com.genz.secure_share.room.File
import com.genz.secure_share.retrofit.FileService
import com.genz.secure_share.retrofit.RetrofitService
import com.genz.secure_share.room.FileDao
import com.genz.secure_share.utils.Resource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

/**
 * Repository for, UploadFile to Remote Source
 * Insert, Delete, Get from SQLite(Room) DB
 */
class FileRepository (private val fileDao : FileDao) {

    private val fileService : FileService = RetrofitService
        .getInstance().create(FileService::class.java)

    val allFiles : LiveData<List<File>> = fileDao.getAllFiles(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString())

    /**
     * Upload file to firebase and store mapping in fire-store
     * @file : Encrypted File
     * @expiry : Expiry of the file
     *
     * @return Resource, Success or Error
     */
    suspend fun uploadFile(file:MultipartBody.Part,expiry:RequestBody, iv : RequestBody) : Resource<Any> {
        return try {
            val response = fileService.uploadImage(file,expiry, iv)
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

    /**
     * Delete file from fire store and storage
     * searchKey : File(Doc) to be deleted from fire store
     *
     * @return Resource, Success or Error
     */
    suspend fun deleteFile(searchKey : String) : Resource<Any> {
        return try {
            val response = fileService.deleteFile(searchKey)
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
    suspend fun delete(file : File) : Resource<Any> {
        return try {
            fileDao.delete(file)
            Resource.Success("Success")
        } catch(e : Exception) {
            Resource.Error(e.message.toString())
        }
    }

}