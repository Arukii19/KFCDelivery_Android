package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etLogin = findViewById<EditText>(R.id.newPhone)
        val etPassword = findViewById<EditText>(R.id.newPassword)
        val btnLogin = findViewById<Button>(R.id.Login)
        val btnRegister = findViewById<Button>(R.id.Register)
        val tvBack = findViewById<TextView>(R.id.tvBackToRoles)

        tvBack?.setOnClickListener { finish() }

        // Pre-fill if coming back from registration
        val registeredLogin = intent.getStringExtra("REGISTERED_LOGIN")
        if (!registeredLogin.isNullOrEmpty()) {
            etLogin.setText(registeredLogin)
            Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("customers")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val doc = docs.documents[0]
                        val savedPassword = doc.getString("password")
                        if (savedPassword == password) {
                            val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
                            prefs.edit()
                                .putString("CUST_ID", doc.id)
                                .putString("CUST_FNAME", doc.getString("firstName") ?: "")
                                .putString("CUST_LNAME", doc.getString("lastName") ?: "")
                                .putString("CUST_PHONE", doc.getString("phone") ?: "")
                                .putString("CUST_EMAIL", doc.getString("email") ?: "")
                                .putString("CUST_ADDR", doc.getString("address") ?: "")
                                .apply()

                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No account found with this email", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
