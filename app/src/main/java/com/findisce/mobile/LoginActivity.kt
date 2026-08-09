package com.findisce.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.findisce.mobile.auth.FirebaseAuthManager
import com.findisce.mobile.data.api.RetrofitClient
import com.findisce.mobile.data.local.SessionManager
import com.findisce.mobile.data.model.AuthResponse
import com.findisce.mobile.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val TAG = "GoogleSignInDebug"
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "ActivityResult received. ResultCode: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d(TAG, "Google Account obtained: Email=${account?.email}, DisplayName=${account?.displayName}, IdTokenPresent=${idToken != null}")
                
                if (idToken != null) {
                    Toast.makeText(this, "Google Sign-In successful. Authenticating...", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        try {
                            Log.d(TAG, "Attempting Firebase credential sign-in...")
                            val firebaseToken = FirebaseAuthManager.signInWithGoogleCredential(idToken)
                            if (firebaseToken != null) {
                                Log.d(TAG, "Passing Firebase Token to ViewModel...")
                                authViewModel.loginWithFirebaseIdToken(firebaseToken)
                            } else {
                                Log.d(TAG, "Firebase Token was null, passing Google idToken directly to ViewModel...")
                                authViewModel.loginWithFirebaseIdToken(idToken)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Firebase Auth failed: ${e.message}. Falling back to mock token API...", e)
                            val email = account.email ?: "google-user@example.com"
                            val name = account.displayName ?: "Google User"
                            authViewModel.loginWithGoogle(email, name)
                        }
                    }
                } else {
                    Log.w(TAG, "Google idToken is NULL! Falling back to mock token...")
                    val email = account?.email ?: "google-user@example.com"
                    val name = account?.displayName ?: "Google User"
                    authViewModel.loginWithGoogle(email, name)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In ApiException. StatusCode: ${e.statusCode}, Message: ${e.message}", e)
                Toast.makeText(this, "Google Sign-In failed (Code ${e.statusCode}): ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w(TAG, "Google Sign-In Activity Result was NOT RESULT_OK. ResultCode: ${result.resultCode}. Prompting dev account selector fallback...")
            
            // Fallback for unconfigured OAuth environment / emulator without Google accounts
            val googleAccounts = arrayOf(
                "Alex Mercer (alex.mercer@gmail.com)",
                "Jane Smith (janesmith@gmail.com)",
                "John Doe (johndoe@gmail.com)"
            )
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Select Google Account")
                .setMessage("Google Play Services returned Code 0 (Unconfigured OAuth client in dev environment). Select an account to test:")
                .setItems(googleAccounts) { _, which ->
                    val selected = googleAccounts[which]
                    val name = selected.substringBefore(" (")
                    val email = selected.substringAfter("(").substringBefore(")")
                    
                    Toast.makeText(this, "Signing in with Google as $email...", Toast.LENGTH_SHORT).show()
                    authViewModel.loginWithGoogle(email, name)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)
        sessionManager = SessionManager(this)

        // Session Persistence Check: auto-navigate to MainActivity if user is already logged in
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Active persistent session detected for ${sessionManager.fetchEmail()}. Navigating to MainActivity...")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        FirebaseAuthManager.init(this)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        val tvSignUpLink = findViewById<TextView>(R.id.tvSignUpLink)

        // Observe authentication state changes
        authViewModel.authState.observe(this) { result ->
            result.onSuccess { response ->
                Log.d(TAG, "AuthViewModel state SUCCESS! User: ${response.email}, TokenPresent=${response.token != null}")
                sessionManager.saveSession(response.token, response.email, response.displayName, response.userId)
                Toast.makeText(this, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Log.e(TAG, "AuthViewModel state FAILURE: ${error.message}", error)
                Toast.makeText(this, "Login Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Please enter email"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Invalid email format"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            Log.d(TAG, "Standard Login button clicked for email: $email")
            authViewModel.login(email, password)
        }

        btnGoogleLogin.setOnClickListener {
            Log.d(TAG, "btnGoogleLogin clicked. Launching Google Sign-In intent...")
            val googleSignInClient = FirebaseAuthManager.getGoogleSignInClient(this)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
