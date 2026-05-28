package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrderStatusActivity : ComponentActivity() {

    private var statusListener: ListenerRegistration? = null

    private val statusSteps = listOf(
        "Pending",
        "Preparing",
        "Ready for Pickup",
        "Out for Delivery",
        "Delivered"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_status)

        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        val orderNum = intent.getStringExtra("ORDER_NUM") ?: ""
        val finalTotal = intent.getDoubleExtra("FINAL_TOTAL", 0.0)
        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "Cash"

        val tvOrderAmount = findViewById<TextView>(R.id.tvOrderAmount)
        val tvOrderPayment = findViewById<TextView>(R.id.tvOrderPayment)
        val tvLiveStatus = findViewById<TextView>(R.id.tvLiveStatus)
        val tvOrderId = findViewById<TextView?>(R.id.tvOrderId)
        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)
        val btnLeaveFeedback = findViewById<Button>(R.id.btnLeaveFeedback)

        tvOrderAmount?.text = "Total: ₱ ${"%.2f".format(finalTotal)}"
        tvOrderPayment?.text = "Payment: $paymentMethod"
        tvOrderId?.text = "Order #$orderNum"
        tvLiveStatus?.text = "Order placed! Waiting to be confirmed..."
        btnLeaveFeedback?.visibility = android.view.View.GONE

        btnBackToMenu.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        btnLeaveFeedback?.setOnClickListener {
            showFeedbackDialog(orderId)
        }

        // Real-time Firestore listener for order status
        if (orderId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            statusListener = db.collection("orders").document(orderId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                    val status = snapshot.getString("status") ?: "Pending"
                    val riderId = snapshot.getString("riderId")
                    val stepIndex = statusSteps.indexOf(status)

                    val emoji = when (status) {
                        "Pending" -> "⏳"
                        "Preparing" -> "👨‍🍳"
                        "Ready for Pickup" -> "📦"
                        "Out for Delivery" -> "🛵"
                        "Delivered" -> "✅"
                        "Canceled" -> "❌"
                        else -> "⏳"
                    }

                    tvLiveStatus?.text = "$emoji $status"

                    // Update step indicators
                    updateStepUI(stepIndex)

                    if (status == "Delivered") {
                        btnLeaveFeedback?.visibility = android.view.View.VISIBLE
                    }

                    if (status == "Canceled") {
                        val cancelReason = snapshot.getString("cancelReason") ?: ""
                        tvLiveStatus?.text = "❌ Order Canceled\n${if (cancelReason.isNotEmpty()) "Reason: $cancelReason" else ""}"
                        btnLeaveFeedback?.visibility = android.view.View.GONE
                    }
                }
        }
    }

    private fun showFeedbackDialog(orderId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etFeedbackComment)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitFeedback)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating
            val comment = etComment.text.toString().trim()
            val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
            val customerName = prefs.getString("CUST_FNAME", "Customer") ?: "Customer"

            val feedbackMap = hashMapOf(
                "orderId" to orderId,
                "customerName" to customerName,
                "rating" to rating,
                "comment" to comment,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            FirebaseFirestore.getInstance().collection("feedback").add(feedbackMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Thank you for your feedback! 🍗", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    val btnLeaveFeedback = findViewById<Button>(R.id.btnLeaveFeedback)
                    btnLeaveFeedback?.visibility = android.view.View.GONE
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun updateStepUI(currentStep: Int) {
        val stepIds = listOf(
            R.id.tvStep1, R.id.tvStep2, R.id.tvStep3, R.id.tvStep4, R.id.tvStep5
        )
        stepIds.forEachIndexed { index, id ->
            val tv = findViewById<TextView?>(id) ?: return@forEachIndexed
            if (index <= currentStep) {
                tv.setTextColor(0xFFE4002B.toInt())
                tv.alpha = 1f
            } else {
                tv.setTextColor(0xFF999999.toInt())
                tv.alpha = 0.5f
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        statusListener?.remove()
    }
}
