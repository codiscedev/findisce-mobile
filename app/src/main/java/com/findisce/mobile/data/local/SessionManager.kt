package com.findisce.mobile.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "finone_user_session"
        private const val KEY_TOKEN = "user_auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
    }

    fun saveSession(token: String?, email: String? = null, name: String? = null, userId: String? = null) {
        val editor = prefs.edit()
        if (token != null) editor.putString(KEY_TOKEN, token)
        if (email != null) editor.putString(KEY_EMAIL, email)
        if (name != null) editor.putString(KEY_NAME, name)
        if (userId != null) editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun fetchToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun fetchEmail(): String? {
        val email = prefs.getString(KEY_EMAIL, null)
        if (!email.isNullOrBlank()) return email
        return FirebaseAuth.getInstance().currentUser?.email
    }

    fun fetchName(): String? {
        val name = prefs.getString(KEY_NAME, null)
        if (!name.isNullOrBlank()) return name
        return FirebaseAuth.getInstance().currentUser?.displayName
    }

    fun fetchUserId(): String? {
        val userId = prefs.getString(KEY_USER_ID, null)
        if (!userId.isNullOrBlank()) return userId
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        if (!fetchToken().isNullOrBlank()) return true
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
