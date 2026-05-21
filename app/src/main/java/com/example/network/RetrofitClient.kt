package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null

    fun getClient(baseUrl: String): MangaApiService {
        // Ensure trailing slash for Retrofit base URL
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        if (retrofit == null || retrofit?.baseUrl().toString() != formattedUrl) {
            retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
        }

        return retrofit!!.create(MangaApiService::class.java)
    }
}
