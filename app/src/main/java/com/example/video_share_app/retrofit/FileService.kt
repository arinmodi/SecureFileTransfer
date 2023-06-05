package com.example.video_share_app.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface FileService {

    /**
     * Upload File API
     * @file : file to be uploaded
     * @expiry : Expiry of the file
     * @iv : IV used for Encryption for AES(CBC)
     */
    @Multipart
    @POST(value = "/upload/file")
    suspend fun uploadImage(@Part file: MultipartBody.Part,
                            @Part("expiry") expiry : RequestBody,
                            @Part("iv") iv : RequestBody): Response<Any>

    /**
     * Delete File API
     * @searchKey : refers to search key of file (id of doc in firebase)
     */
    @FormUrlEncoded
    @POST("/delete/file")
    suspend fun deleteFile(@Field("searchKey") searchKey : String) : Response<Any>

}