package com.example.video_share_app.model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("url")
    val url : String?,
    @SerializedName("searchKey")
    val searchKey : String?,
    @SerializedName("deletionKey")
    val deletionKey : String?
)
