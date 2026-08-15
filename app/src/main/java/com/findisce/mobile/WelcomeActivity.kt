package com.findisce.mobile

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.findisce.mobile.data.local.SessionManager

class WelcomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val goToDashboardButton = findViewById<Button>(R.id.go_to_dashboard)
        val createAccountButton = findViewById<Button>(R.id.create_account)
        val tvHeadlineSimplified = findViewById<TextView>(R.id.tvHeadlineSimplified)

        // Apply visual gradient text effect on "simplified." headline
        tvHeadlineSimplified.post {
            val textWidth = tvHeadlineSimplified.paint.measureText(tvHeadlineSimplified.text.toString())
            if (textWidth > 0) {
                val shader = LinearGradient(
                    0f, 0f, textWidth, 0f,
                    intArrayOf(
                        Color.parseColor("#10B981"), // Emerald
                        Color.parseColor("#14B8A6"), // Teal
                        Color.parseColor("#3B82F6")  // Blue
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
                tvHeadlineSimplified.paint.shader = shader
                tvHeadlineSimplified.invalidate()
            }
        }

        // Hero Primary CTA: Go to Dashboard
        goToDashboardButton.setOnClickListener {
            navigateToDashboardOrLogin()
        }

        // Hero Secondary CTA: Create Free Account
        createAccountButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun navigateToDashboardOrLogin() {
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
