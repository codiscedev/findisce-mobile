package com.findisce.mobile.data.model

data class AuthResponse(
    val userId: String,
    val email: String,
    val displayName: String?,
    val token: String?,
    val subscriptionTier: String = "FREE",
    val subscriptionStatus: String = "INACTIVE"
)
