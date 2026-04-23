package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userName = intent.getStringExtra("USER_NAME")
        if (!userName.isNullOrEmpty()) {
            findViewById<TextView>(R.id.tvProfileName).text = userName
        }

        val userEmail = intent.getStringExtra("USER_EMAIL")
        if (!userEmail.isNullOrEmpty()) {
            findViewById<TextView>(R.id.tvProfileEmail).text = userEmail
        }

        findViewById<Button>(R.id.btnBackToDashboard).setOnClickListener {
            finish() // returns to dashboard without destroying the current one
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Logs the user out and clears the activity trace
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
