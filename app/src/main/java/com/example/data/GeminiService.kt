package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
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

// --- Gemini API Request/Response Models ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Retrofit Client Implementation ---

object RetrofitClient {
    private const val TAG = "GeminiRetrofitClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Executes content generation against the Gemini API using the injected key.
     */
    suspend fun askGemini(
        prompt: String,
        history: List<Content>,
        systemPrompt: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is empty or placeholder! Please configure it in the Secrets panel.")
            return "يرجى ضبط مفتاح GEMINI_API_KEY في لوحة الأسرار (Secrets) لبدء المحادثة مع التوأم الذكي لنسيم!"
        }

        // Prepare full conversation contents
        val fullContents = mutableListOf<Content>()
        // Add chat history
        fullContents.addAll(history)
        // Add current prompt
        fullContents.add(Content(parts = listOf(Part(text = prompt)), role = "user"))

        val request = GeminiRequest(
            contents = fullContents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "لم أتمكن من استلام إجابة في الوقت الحالي، يرجى المحاولة مرة أخرى."
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ", e)
            "حدث خطأ في الاتصال: ${e.localizedMessage ?: "يرجى التحقق من الاتصال بالإنترنت وإعادة المحاولة"}."
        }
    }
}
