package com.genz.secure_share.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {

    /**
     * Base API Url
     */
    private const val BASE_URL = "https://dark-puce-donkey-boot.cyclic.app"

    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
