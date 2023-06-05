package com.example.video_share_app.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class FileResponse(
    @SerializedName("url")
    @Expose
    var url : String,
    @SerializedName("name")
    @Expose
    var name : String,
    @SerializedName("path")
    @Expose
    var path : String,
    @SerializedName("expiry")
    @Expose
    var expiry : String,
    @SerializedName("iv")
    @Expose
    var iv : String
)
