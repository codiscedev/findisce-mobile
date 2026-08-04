package com.findisce.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.findisce.mobile.ui.viewmodel.AuthViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvLoginLink = findViewById<TextView>(R.id.tvLoginLink)

        // Observe authentication state changes
        authViewModel.authState.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Account created! Verification email sent.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            result.onFailure { error ->
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

            // Call signup method inside View Model
            authViewModel.signup(name, email)
        }

        tvLoginLink.setOnClickListener {
            finish()
        }
    }
}
