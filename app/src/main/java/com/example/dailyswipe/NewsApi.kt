package com.example.dailyswipe

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface NewsApi {
    @GET("v1/news")
    suspend fun getNews(@QueryMap params: Map<String, String>): ApiResponse
}

fun createNewsApi(): NewsApi {
    return Retrofit.Builder()
        .baseUrl("http://api.mediastack.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NewsApi::class.java)
}