package com.findisce.mobile.auth

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

object FirebaseAuthManager {

    private const val TAG = "GoogleSignInDebug"

    private const val FIREBASE_API_KEY = "AIzaSyDMoM-1Ba-6-Gm0ketzybujafXairabT9k"
    private const val FIREBASE_APP_ID = "1:130125343157:android:0a5a0cd7160b6df0e9d1fc"
    private const val FIREBASE_PROJECT_ID = "finone-6f8ed"
    private const val FIREBASE_STORAGE_BUCKET = "finone-6f8ed.firebasestorage.app"
    
    // Official Web Client ID extracted from google-services.json
    const val WEB_CLIENT_ID = "130125343157-9tat4akotvrqn283t228bh7ff4dkjiq8.apps.googleusercontent.com"

    fun init(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                Log.d(TAG, "Initializing FirebaseApp programmatically...")
                val options = FirebaseOptions.Builder()
                    .setApiKey(FIREBASE_API_KEY)
                    .setApplicationId(FIREBASE_APP_ID)
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "FirebaseApp initialized successfully!")
            } else {
                Log.d(TAG, "FirebaseApp is already initialized.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FirebaseApp: ${e.message}", e)
        }
    }

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        Log.d(TAG, "Creating GoogleSignInClient with WEB_CLIENT_ID: $WEB_CLIENT_ID")
        init(context)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun signInWithGoogleCredential(idToken: String): String? {
        return try {
            Log.d(TAG, "Exchanging Google ID token with Firebase Auth...")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            Log.d(TAG, "Firebase Auth sign-in success. User UID: ${firebaseUser?.uid}, Email: ${firebaseUser?.email}")
            
            if (firebaseUser == null) {
                Log.e(TAG, "FirebaseUser is null after successful credential sign in.")
                return null
            }
            
            val tokenResult = firebaseUser.getIdToken(true).await()
            val token = tokenResult.token
            Log.d(TAG, "Firebase ID Token fetched successfully. Length: ${token?.length}")
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to authenticate credential with Firebase Auth: ${e.message}", e)
            throw e
        }
    }

    fun getCurrentFirebaseToken(callback: (String?) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d(TAG, "Fetching current token for user: ${currentUser.email}")
            currentUser.getIdToken(true)
                .addOnSuccessListener { 
                    Log.d(TAG, "Current user token fetched successfully!")
                    callback(it.token) 
                }
                .addOnFailureListener { 
                    Log.e(TAG, "Failed to fetch current user token: ${it.message}", it)
                    callback(null) 
                }
        } else {
            Log.d(TAG, "No current user in Firebase Auth.")
            callback(null)
        }
    }
}
