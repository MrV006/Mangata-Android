package com.example.network

import retrofit2.http.GET

interface MangaApiService {
    // This connects to the custom WordPress REST API endpoint defined in our Mangata theme
    @GET("wp-json/mangata/v1/mangas")
    suspend fun getMangas(): List<com.example.data.MangaEntity>
}
