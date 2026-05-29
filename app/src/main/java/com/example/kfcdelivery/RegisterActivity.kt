package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)
        tvBackToLogin.setOnClickListener { finish() }

        val etFirstName = findViewById<EditText>(R.id.FirstName)
        val etLastName = findViewById<EditText>(R.id.LastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etPassword = findViewById<EditText>(R.id.Password)
        val btnRegister = findViewById<Button>(R.id.btnDoRegister)

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (firstName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length != 11) {
                Toast.makeText(this, "Phone number must be 11 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!email.lowercase().endsWith(".com")) {
                Toast.makeText(this, "Please enter a valid email ending with .com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()

            // Check if phone already exists
            db.collection("customers").whereEqualTo("phone", phone).get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        Toast.makeText(this, "This phone number is already registered", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val customerMap = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "phone" to phone,
                        "email" to email,
                        "address" to address,
                        "password" to password
                    )

                    db.collection("customers").add(customerMap)
                        .addOnSuccessListener {
                            val intent = Intent(this, MainActivity::class.java)
                            intent.putExtra("REGISTERED_LOGIN", email)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Registration Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
