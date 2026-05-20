package com.example.api

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    /**
     * Call Gemini to analyze the movie reviews
     */
    suspend fun getSummary(reviewsJson: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "پیکربندی کلید API جمینای متصل نشده است. لطفا کلید را در بخش مدیریت امنیتی تنظیم کنید."
        }

        val prompt = """
            تو یک دستیار هوش مصنوعی متخصص تحلیل فیلم و سریال برای کاربران فارسی زبان هستی.
            داده‌های زیر نظرات واقعی کاربران برای یک فیلم است. لطفاً بر اساس این اطلاعات یک تحلیل کوتاه، خلاقانه و جمع‌وجور در ۳ خط از وضعیت بازخوردها به زبان فارسی بنویس (مثلا چه درصدی راضی بودند، چه نقاط قوت دارند، و چه نقاط ضعفی).
            پاسخ را در قالب یک پاراگراف روان فارسی بدون کاراکترهای اضافه یا بخش انگلیسی بنویس.
            
            داده نظرات:
            $reviewsJson
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(GeminiPart(text = prompt))
                )
            )
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "تحلیلی برای نظرات این فیلم یافت نشد."
        } catch (e: Exception) {
            "دریافت تحلیل هوشمند نظرات با خطا همراه شد: ${e.localizedMessage}"
        }
    }
}
