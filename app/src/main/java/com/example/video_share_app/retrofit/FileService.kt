package com.example.video_share_app.retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {

    /**
     * Upload File API
     * @file : file to be uploaded
     * @expiry : Expiry of the file
     */
    @Multipart
    @POST(value = "/upload/file")
    suspend fun uploadImage(@Part file: MultipartBody.Part,
                            @Part("expiry") expiry : RequestBody): Response<Any>
}