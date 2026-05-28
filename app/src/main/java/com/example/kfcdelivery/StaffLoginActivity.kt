package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class StaffLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_login)

        val etLogin = findViewById<EditText>(R.id.etStaffPhone)
        val etPassword = findViewById<EditText>(R.id.etStaffPassword)
        val btnLogin = findViewById<Button>(R.id.btnStaffLogin)
        val tvBack = findViewById<TextView>(R.id.tvBackFromStaff)

        tvBack.setOnClickListener { finish() }

        btnLogin.setOnClickListener {
            val email = etLogin.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("employees")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { docs ->
                    if (docs.isEmpty) {
                        Toast.makeText(this, "No staff account found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val doc = docs.documents[0]
                    val savedPassword = doc.getString("password") ?: ""
                    val role = doc.getString("role") ?: ""

                    if (savedPassword != password) {
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    if (role == "BranchAdmin" || role == "SuperAdmin" || role == "Admin") {
                        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("ADMIN_ID", doc.id)
                            .putString("ADMIN_NAME", doc.getString("firstName") ?: "")
                            .putString("ADMIN_ROLE", role)
                            .apply()
                        startActivity(Intent(this@StaffLoginActivity, AdminDashboardActivity::class.java))
                        finish()
                        return@addOnSuccessListener
                    }

                    val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
                    prefs.edit()
                        .putString("STAFF_ID", doc.id)
                        .putString("STAFF_NAME", doc.getString("firstName") ?: "")
                        .putString("STAFF_BRANCH_ID", doc.getString("branchId") ?: "")
                        .putString("STAFF_BRANCH_NAME", doc.getString("branchName") ?: "")
                        .putString("STAFF_ROLE", role)
                        .apply()

                    startActivity(Intent(this, StaffDashboardActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
