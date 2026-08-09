package com.findisce.mobile.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.findisce.mobile.data.model.AuthResponse
import com.findisce.mobile.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val TAG = "GoogleSignInDebug"
    private val userRepository = UserRepository()

    private val _authState = MutableLiveData<Result<AuthResponse>>()
    val authState: LiveData<Result<AuthResponse>> = _authState

    fun login(email: String, password: String) {
        Log.d(TAG, "AuthViewModel.login called for email: $email")
        viewModelScope.launch {
            try {
                val response = userRepository.login(email, password)
                Log.d(TAG, "AuthViewModel.login success for email: $email")
                _authState.value = Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG, "AuthViewModel.login error: ${e.message}", e)
                _authState.value = Result.failure(e)
            }
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        Log.d(TAG, "AuthViewModel.loginWithGoogle called for email: $email")
        viewModelScope.launch {
            try {
                val response = userRepository.loginWithGoogle(email, name)
                Log.d(TAG, "AuthViewModel.loginWithGoogle success for email: $email")
                _authState.value = Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG, "AuthViewModel.loginWithGoogle error: ${e.message}", e)
                _authState.value = Result.failure(e)
            }
        }
    }

    fun loginWithFirebaseIdToken(idToken: String) {
        Log.d(TAG, "AuthViewModel.loginWithFirebaseIdToken called with idToken length: ${idToken.length}")
        viewModelScope.launch {
            try {
                val response = userRepository.loginWithFirebaseIdToken(idToken)
                Log.d(TAG, "AuthViewModel.loginWithFirebaseIdToken success!")
                _authState.value = Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG, "AuthViewModel.loginWithFirebaseIdToken error: ${e.message}", e)
                _authState.value = Result.failure(e)
            }
        }
    }

    fun signup(name: String, email: String, password: String) {
        Log.d(TAG, "AuthViewModel.signup called for email: $email")
        viewModelScope.launch {
            try {
                val response = userRepository.signup(name, email, password)
                Log.d(TAG, "AuthViewModel.signup success for email: $email")
                _authState.value = Result.success(response)
            } catch (e: Exception) {
                Log.e(TAG, "AuthViewModel.signup error: ${e.message}", e)
                _authState.value = Result.failure(e)
            }
        }
    }
}
