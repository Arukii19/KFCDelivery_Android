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
            val lastName = lastNameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || firstName.isEmpty() || password.isEmpty()) {
                android.widget.Toast.makeText(this, "Please fill all required fields", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userMap = hashMapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "password" to password
            )

            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(email)
                .set(userMap)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("REGISTERED_EMAIL", email)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    android.widget.Toast.makeText(this, "Registration Failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }
}
