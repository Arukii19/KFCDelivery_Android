package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class OrderStatusActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_status)

        val finalTotal = intent.getDoubleExtra("FINAL_TOTAL", 0.0)
        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD")

        val tvOrderAmount = findViewById<TextView>(R.id.tvOrderAmount)
        if (tvOrderAmount != null) {
            tvOrderAmount.text = String.format(Locale.getDefault(), "Amount Paid: ₱ %.2f", finalTotal)
        }

        val tvOrderPayment = findViewById<TextView>(R.id.tvOrderPayment)
        if (tvOrderPayment != null && !paymentMethod.isNullOrEmpty()) {
            tvOrderPayment.text = "Method: $paymentMethod"
        }

        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)
        btnBackToMenu.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val btnLeaveFeedback = findViewById<Button>(R.id.btnLeaveFeedback)
        btnLeaveFeedback.setOnClickListener {
            val intent = Intent(this, FeedbackActivity::class.java)
            startActivity(intent)
        }

        val tvLiveStatus = findViewById<TextView>(R.id.tvLiveStatus)
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        handler.postDelayed({
            tvLiveStatus?.text = "Food prepared! Assigning a delivery rider..."
        }, 3000)

        handler.postDelayed({
            tvLiveStatus?.text = "Rider John Doe is on the way with your food!"
        }, 6000)

        handler.postDelayed({
            tvLiveStatus?.text = "Delivered! Enjoy your KFC!"
            btnLeaveFeedback?.visibility = android.view.View.VISIBLE
        }, 10000)
    }
}
