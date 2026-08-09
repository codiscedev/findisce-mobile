package com.findisce.mobile.data.repository

import android.util.Log
import com.findisce.mobile.data.api.RetrofitClient
import com.findisce.mobile.data.model.*

class UserRepository {

    private val TAG = "GoogleSignInDebug"
    private val apiService = RetrofitClient.apiService

    suspend fun login(email: String, password: String): AuthResponse {
        Log.d(TAG, "UserRepository.login calling POST /auth/login for email: $email")
        val request = mapOf("email" to email, "password" to password)
        val response = apiService.login(request)
        Log.d(TAG, "UserRepository.login response: success=${response.success}, message=${response.message}")
        if (response.success && response.data != null) {
            RetrofitClient.token = response.data.token
            return response.data
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    suspend fun loginWithGoogle(email: String, name: String): AuthResponse {
        Log.d(TAG, "UserRepository.loginWithGoogle calling POST /auth/login with mock token for email: $email")
        val request = mapOf("idToken" to "mock:$email:$name")
        val response = apiService.login(request)
        Log.d(TAG, "UserRepository.loginWithGoogle response: success=${response.success}, message=${response.message}")
        if (response.success && response.data != null) {
            RetrofitClient.token = response.data.token
            return response.data
        } else {
            throw Exception(response.message ?: "Google Sign In failed")
        }
    }

    suspend fun loginWithFirebaseIdToken(idToken: String): AuthResponse {
        Log.d(TAG, "UserRepository.loginWithFirebaseIdToken calling POST /auth/login with idToken length: ${idToken.length}")
        val request = mapOf("idToken" to idToken)
        val response = apiService.login(request)
        Log.d(TAG, "UserRepository.loginWithFirebaseIdToken response: success=${response.success}, message=${response.message}")
        if (response.success && response.data != null) {
            RetrofitClient.token = response.data.token ?: idToken
            return response.data
        } else {
            throw Exception(response.message ?: "Firebase Token Auth failed")
        }
    }

    suspend fun signup(name: String, email: String, password: String): AuthResponse {
        Log.d(TAG, "UserRepository.signup calling POST /auth/signup for email: $email")
        val request = mapOf("name" to name, "email" to email, "password" to password)
        val response = apiService.signup(request)
        Log.d(TAG, "UserRepository.signup response: success=${response.success}, message=${response.message}")
        if (response.success && response.data != null) {
            RetrofitClient.token = response.data.token
            return response.data
        } else {
            throw Exception(response.message ?: "Signup failed")
        }
    }
}
