package com.findisce.mobile.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.findisce.mobile.data.model.AuthResponse
import com.findisce.mobile.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _authState = MutableLiveData<Result<AuthResponse>>()
    val authState: LiveData<Result<AuthResponse>> = _authState

    fun login(email: String, name: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.login(email, name)
                _authState.value = Result.success(response)
            } catch (e: Exception) {
                _authState.value = Result.failure(e)
            }
        }
    }

    fun signup(name: String, email: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.signup(name, email)
                _authState.value = Result.success(response)
            } catch (e: Exception) {
                _authState.value = Result.failure(e)
            }
        }
    }
}
