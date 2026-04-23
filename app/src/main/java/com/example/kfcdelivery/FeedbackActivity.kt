package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.ComponentActivity

class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        val btnBack = findViewById<ImageView>(R.id.btnBackFeedback)
        btnBack.setOnClickListener {
            finish()
        }

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitFeedback)

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            Toast.makeText(this, "Thank you for your $rating star feedback!", Toast.LENGTH_LONG).show()

            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
