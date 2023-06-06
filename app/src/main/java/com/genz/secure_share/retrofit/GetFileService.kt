package com.genz.secure_share.retrofit

import com.genz.secure_share.model.FileResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GetFileService {

    /**
     * Get File API
     * @searchKey : refers to search key of file (id of doc in firebase)
     */
    @GET("/get/file")
    suspend fun getFile(@Query("searchKey") searchKey : String) : Response<FileResponse>

}