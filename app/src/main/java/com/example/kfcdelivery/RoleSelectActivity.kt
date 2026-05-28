package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore

class RoleSelectActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_select)
        
        seedTestAccounts()

        // Check if already logged in as customer → skip to dashboard
        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val customerId = prefs.getString("CUST_ID", null)
        if (!customerId.isNullOrEmpty()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        // Check if already logged in as staff
        val staffId = prefs.getString("STAFF_ID", null)
        if (!staffId.isNullOrEmpty()) {
            startActivity(Intent(this, StaffDashboardActivity::class.java))
            finish()
            return
        }

        // Check if already logged in as rider
        val riderId = prefs.getString("RIDER_ID", null)
        if (!riderId.isNullOrEmpty()) {
            startActivity(Intent(this, RiderDashboardActivity::class.java))
            finish()
            return
        }

        findViewById<CardView>(R.id.cardCustomer).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<CardView>(R.id.cardStaff).setOnClickListener {
            startActivity(Intent(this, StaffLoginActivity::class.java))
        }

        findViewById<CardView>(R.id.cardRider).setOnClickListener {
            startActivity(Intent(this, RiderLoginActivity::class.java))
        }
    }

    private fun seedTestAccounts() {
        val db = FirebaseFirestore.getInstance()
        
        // Seed Admin
        db.collection("employees").whereEqualTo("email", "admin@kfc.com").get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    val admin = hashMapOf(
                        "firstName" to "Super",
                        "lastName" to "Admin",
                        "email" to "admin@kfc.com",
                        "password" to "admin123",
                        "role" to "SuperAdmin",
                        "branchName" to "All Branches"
                    )
                    db.collection("employees").add(admin)
                }
            }

        // Seed Staff
        db.collection("employees").whereEqualTo("email", "staff@kfc.com").get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    val staff = hashMapOf(
                        "firstName" to "Test",
                        "lastName" to "Staff",
                        "email" to "staff@kfc.com",
                        "password" to "staff123",
                        "role" to "Staff",
                        "branchName" to "Main Street" // Matches default branch
                    )
                    db.collection("employees").add(staff)
                }
            }

        // Seed Rider
        db.collection("riders").whereEqualTo("email", "rider@kfc.com").get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    val rider = hashMapOf(
                        "firstName" to "Speedy",
                        "lastName" to "Gonzales",
                        "email" to "rider@kfc.com",
                        "phone" to "09222222222",
                        "password" to "rider123",
                        "vehicle" to "Motorcycle",
                        "status" to "Offline",
                        "isApproved" to true
                    )
                    db.collection("riders").add(rider)
                }
            }
    }
}
