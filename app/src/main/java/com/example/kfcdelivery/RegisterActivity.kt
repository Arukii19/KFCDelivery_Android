package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        tvBackToLogin.setOnClickListener {
            finish()
        }

        val btnRegister = findViewById<android.widget.Button>(R.id.btnDoRegister)
        btnRegister.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
