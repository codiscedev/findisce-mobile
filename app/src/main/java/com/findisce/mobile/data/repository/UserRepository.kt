package com.findisce.mobile.data.repository

import com.findisce.mobile.data.api.RetrofitClient
import com.findisce.mobile.data.model.AuthResponse

class UserRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun login(email: String, name: String): AuthResponse {
        val request = mapOf("email" to email, "password" to "securePassword123") // standard mock authentication wrapper
        return apiService.login(request)
    }

    suspend fun signup(name: String, email: String): AuthResponse {
        val request = mapOf("name" to name, "email" to email, "password" to "securePassword123")
        return apiService.signup(request)
    }
}
