package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class RiderLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_login)

        val etLogin = findViewById<EditText>(R.id.etRiderEmail)
        val etPassword = findViewById<EditText>(R.id.etRiderPassword)
        val btnLogin = findViewById<Button>(R.id.btnRiderLogin)
        val btnApply = findViewById<Button>(R.id.btnApplyRider)
        val tvBack = findViewById<TextView>(R.id.tvBackFromRider)

        tvBack.setOnClickListener { finish() }

        btnApply.setOnClickListener {
            startActivity(Intent(this, RiderRegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("riders")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        Toast.makeText(this, "No rider account found with this email", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val doc = docs.documents[0]
                    val savedPassword = doc.getString("password") ?: ""
                    val isApproved = doc.getBoolean("isApproved") ?: false

                    if (savedPassword != password) {
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    if (!isApproved) {
                        Toast.makeText(this, "Your rider account is pending approval from the branch admin", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("RIDER_ID", doc.id)
                        .putString("RIDER_FNAME", doc.getString("firstName") ?: "")
                        .putString("RIDER_LNAME", doc.getString("lastName") ?: "")
                        .putString("RIDER_PHONE", doc.getString("phone") ?: "")
                        .putString("RIDER_VEHICLE", doc.getString("vehicle") ?: "")
                        .putString("RIDER_BRANCH_ID", doc.getString("branchId") ?: "")
                        .putString("RIDER_STATUS", doc.getString("status") ?: "Offline")
                        .apply()

                    startActivity(Intent(this, RiderDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
