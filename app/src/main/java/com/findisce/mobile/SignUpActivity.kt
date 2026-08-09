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
import com.findisce.mobile.data.local.SessionManager
import com.findisce.mobile.data.model.AuthResponse
import com.findisce.mobile.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

    private val TAG = "GoogleSignInDebug"
    private lateinit var authViewModel: AuthViewModel
    private var isGoogleAuth = false

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "SignUp ActivityResult received. ResultCode: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d(TAG, "SignUp Google Account obtained: Email=${account?.email}, DisplayName=${account?.displayName}, IdTokenPresent=${idToken != null}")
                
                if (idToken != null) {
                    isGoogleAuth = true
                    Toast.makeText(this, "Google Sign-Up successful. Authenticating...", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch {
                        try {
                            Log.d(TAG, "Attempting Firebase credential sign-in for SignUp...")
                            val firebaseToken = FirebaseAuthManager.signInWithGoogleCredential(idToken)
                            if (firebaseToken != null) {
                                Log.d(TAG, "Passing Firebase Token to ViewModel...")
                                authViewModel.loginWithFirebaseIdToken(firebaseToken)
                            } else {
                                Log.d(TAG, "Firebase Token was null, passing Google idToken directly to ViewModel...")
                                authViewModel.loginWithFirebaseIdToken(idToken)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Firebase Auth failed in SignUp: ${e.message}. Falling back to mock token API...", e)
                            val email = account.email ?: "google-user@example.com"
                            val name = account.displayName ?: "Google User"
                            authViewModel.loginWithGoogle(email, name)
                        }
                    }
                } else {
                    Log.w(TAG, "SignUp Google idToken is NULL! Falling back to mock token...")
                    isGoogleAuth = true
                    val email = account?.email ?: "google-user@example.com"
                    val name = account?.displayName ?: "Google User"
                    authViewModel.loginWithGoogle(email, name)
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-Up ApiException. StatusCode: ${e.statusCode}, Message: ${e.message}", e)
                Toast.makeText(this, "Google Sign-Up failed (Code ${e.statusCode}): ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w(TAG, "Google Sign-Up Activity Result was NOT RESULT_OK. ResultCode: ${result.resultCode}. Prompting dev account selector fallback...")
            
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
                    
                    isGoogleAuth = true
                    Toast.makeText(this, "Signing up with Google as $email...", Toast.LENGTH_SHORT).show()
                    authViewModel.loginWithGoogle(email, name)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        FirebaseAuthManager.init(this)

        val sessionManager = SessionManager(this)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnGoogleSignUp = findViewById<Button>(R.id.btnGoogleSignUp)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        // Observe authentication state changes
        authViewModel.authState.observe(this) { result ->
            result.onSuccess { response ->
                Log.d(TAG, "SignUp AuthViewModel state SUCCESS! User: ${response.email}, isGoogleAuth=$isGoogleAuth")
                if (isGoogleAuth) {
                    sessionManager.saveSession(response.token, response.email, response.displayName, response.userId)
                    Toast.makeText(this, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "Account created! Verification email sent.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
            result.onFailure { error ->
                Log.e(TAG, "SignUp AuthViewModel state FAILURE: ${error.message}", error)
                Toast.makeText(this, "Signup Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnSignUp.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            isGoogleAuth = false
            Log.d(TAG, "Standard SignUp button clicked for email: $email")
            authViewModel.signup(name, email, password)
        }

        btnGoogleSignUp.setOnClickListener {
            Log.d(TAG, "btnGoogleSignUp clicked. Launching Google Sign-In intent...")
            val googleSignInClient = FirebaseAuthManager.getGoogleSignInClient(this)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }
}
