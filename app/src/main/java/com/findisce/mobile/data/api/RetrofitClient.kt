package com.findisce.mobile.data.api

import android.content.Context
import com.findisce.mobile.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://herself-bacterial-browsers-ancient.trycloudflare.com/api/" // Maps to finone-server on host when running in android emulator

    var sessionManager: SessionManager? = null

    var token: String?
        get() = sessionManager?.fetchToken()
        set(value) {
            sessionManager?.saveSession(value)
        }

    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            val authToken = token
            if (!authToken.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $authToken")
            }
            chain.proceed(requestBuilder.build())
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
