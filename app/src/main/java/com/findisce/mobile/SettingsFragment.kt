package com.findisce.mobile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val etProfileName = view.findViewById<EditText>(R.id.etProfileName)
        val btnSaveProfile = view.findViewById<Button>(R.id.btnSaveProfile)
        val rgThemeAccent = view.findViewById<RadioGroup>(R.id.rgThemeAccent)
        val btnSignOut = view.findViewById<Button>(R.id.btnSignOut)

        btnSaveProfile.setOnClickListener {
            val name = etProfileName.text.toString().trim()
            if (name.isEmpty()) {
                etProfileName.error = "Name cannot be empty"
                return@setOnClickListener
            }
            Toast.makeText(context, "Preferences saved successfully!", Toast.LENGTH_SHORT).show()
        }

        rgThemeAccent.setOnCheckedChangeListener { _, checkedId ->
            val accentName = when (checkedId) {
                R.id.rbThemeBlue -> "Blue"
                R.id.rbThemeRose -> "Rose"
                R.id.rbThemeEmerald -> "Green"
                else -> "Blue"
            }
            Toast.makeText(context, "Theme accent switched to $accentName", Toast.LENGTH_SHORT).show()
        }

        btnSignOut.setOnClickListener {
            Toast.makeText(context, "Signed Out Successfully.", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        return view
    }
}
