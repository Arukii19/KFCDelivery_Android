package com.example.kfcdelivery

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class RiderRegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_register)

        val etFirstName = findViewById<EditText>(R.id.etRiderFirstName)
        val etLastName = findViewById<EditText>(R.id.etRiderLastName)
        val etEmail = findViewById<EditText>(R.id.etRiderRegEmail)
        val etPhone = findViewById<EditText>(R.id.etRiderRegPhone)
        val etVehicle = findViewById<EditText>(R.id.etRiderRegVehicle)
        val etPassword = findViewById<EditText>(R.id.etRiderRegPassword)
        
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRiderApp)
        val tvBack = findViewById<TextView>(R.id.tvBackToRiderLogin)

        tvBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val vehicle = etVehicle.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || vehicle.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
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

            btnSubmit.isEnabled = false
            btnSubmit.text = "Submitting..."

            // Check if email already exists
            db.collection("riders").whereEqualTo("email", email).get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        Toast.makeText(this, "Email is already registered", Toast.LENGTH_SHORT).show()
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Submit Application"
                        return@addOnSuccessListener
                    }

                    val riderData = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email,
                        "phone" to phone,
                        "vehicle" to vehicle,
                        "password" to password,
                        "isApproved" to false,
                        "branchId" to "", // Explicitly setting this if multiple branches ever needed
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

                    db.collection("riders").add(riderData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Application submitted! Please wait for Admin approval.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            btnSubmit.isEnabled = true
                            btnSubmit.text = "Submit Application"
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking email: ${e.message}", Toast.LENGTH_SHORT).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Application"
                }
        }
    }
}
