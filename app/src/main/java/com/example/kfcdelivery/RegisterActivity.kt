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
        val emailEditText = findViewById<android.widget.EditText>(R.id.Email)
        val firstNameEditText = findViewById<android.widget.EditText>(R.id.FirstName)
        val lastNameEditText = findViewById<android.widget.EditText>(R.id.LastName)
        val passwordEditText = findViewById<android.widget.EditText>(R.id.Password)
        
        btnRegister.setOnClickListener {
            val email = emailEditText.text.toString()
            val firstName = firstNameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Save the user's first name and password locally using the email as a key
            val sharedPref = getSharedPreferences("KFCAppPrefs", android.content.Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString(email, firstName)
                putString("PWD_$email", password)
                apply()
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("REGISTERED_EMAIL", email)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
