package com.findisce.mobile.data.api

import android.content.Context
import com.findisce.mobile.data.local.SessionManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val existingHeader = response.request.header("Authorization")
        val existingToken = existingHeader?.replace("Bearer ", "")
        val currentToken = RetrofitClient.token

        // If another thread already refreshed the token, retry with updated token
        if (existingToken != null && existingToken != currentToken && !currentToken.isNullOrBlank()) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        // Try refreshing Firebase ID token
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            try {
                val task = firebaseUser.getIdToken(true)
                val result = Tasks.await(task, 10, TimeUnit.SECONDS)
                val newToken = result.token

                if (!newToken.isNullOrBlank()) {
                    RetrofitClient.sessionManager?.saveSession(
                        token = newToken,
                        email = firebaseUser.email,
                        name = firebaseUser.displayName,
                        userId = firebaseUser.uid
                    )

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                }
            } catch (e: Exception) {
                // Token refresh failed
            }
        }

        // Token cannot be refreshed -> clear session
        RetrofitClient.sessionManager?.clearSession()
        return null
    }
}

object RetrofitClient {

    private const val BASE_URL = "https://herself-bacterial-browsers-ancient.trycloudflare.com/api/"

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
        .authenticator(TokenAuthenticator())
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
