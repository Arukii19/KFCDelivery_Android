package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerButton = findViewById<Button>(R.id.Register)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val emailEditText = findViewById<EditText>(R.id.Email)
        
        val registeredEmail = intent.getStringExtra("REGISTERED_EMAIL")
        if (!registeredEmail.isNullOrEmpty()) {
            emailEditText.setText(registeredEmail)
            android.widget.Toast.makeText(this, "Registration successful! Please log in.", android.widget.Toast.LENGTH_SHORT).show()
        }

        val loginButton = findViewById<Button>(R.id.Login)
        val passwordEditText = findViewById<EditText>(R.id.newPassword)
        
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            val sharedPref = getSharedPreferences("KFCAppPrefs", android.content.Context.MODE_PRIVATE)
            val savedPassword = sharedPref.getString("PWD_$email", null)

            // Validate password if one was saved for this email
            if (savedPassword != null && savedPassword != password) {
                android.widget.Toast.makeText(this, "Invalid Password", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Retrieve the registered name if it exists, otherwise fallback to the email username
            val fallbackName = email.substringBefore("@")
            val savedName = sharedPref.getString(email, fallbackName)
            
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("USER_NAME", savedName)
            intent.putExtra("USER_EMAIL", email)
            startActivity(intent)
        }
    }
}
