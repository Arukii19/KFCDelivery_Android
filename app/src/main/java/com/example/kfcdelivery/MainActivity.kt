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
            
            if (email.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Please enter email and password", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(email).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val savedPassword = document.getString("password")
                        if (savedPassword == password) {
                            val savedName = document.getString("firstName") ?: email.substringBefore("@")
                            
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.putExtra("USER_NAME", savedName)
                            intent.putExtra("USER_EMAIL", email)
                            startActivity(intent)
                        } else {
                            android.widget.Toast.makeText(this, "Invalid Password", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(this, "User not found. Please register.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(this, "Login failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }
}
