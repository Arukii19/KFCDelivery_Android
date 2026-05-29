package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import android.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileActivity : ComponentActivity() {

    private var ordersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val custId = prefs.getString("CUST_ID", "") ?: ""

        // Profile tab views
        val etFName = findViewById<EditText>(R.id.etProfFirstName)
        val etLName = findViewById<EditText>(R.id.etProfLastName)
        val etPhone = findViewById<EditText>(R.id.etProfPhone)
        val etEmail = findViewById<EditText>(R.id.etProfEmail)
        val etAddress = findViewById<EditText>(R.id.etProfAddress)
        val tvInitials = findViewById<TextView>(R.id.tvProfileInitials)
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        // Tabs
        val btnTabProfile = findViewById<Button>(R.id.btnTabProfile)
        val btnTabOrders = findViewById<Button>(R.id.btnTabOrders)
        val layoutProfile = findViewById<LinearLayout>(R.id.layoutProfileDetails)
        val layoutOrders = findViewById<LinearLayout>(R.id.layoutMyOrders)

        // Order history RecyclerView
        val rvOrders = findViewById<RecyclerView>(R.id.rvOrders)
        rvOrders.layoutManager = LinearLayoutManager(this)

        // Logout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            CartManager.clear()
            val intent = Intent(this, RoleSelectActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Back
        val btnBack = findViewById<ImageView?>(R.id.btnBackProfile)
        btnBack?.setOnClickListener { finish() }

        // Tab switching
        btnTabProfile.setOnClickListener {
            layoutProfile.visibility = View.VISIBLE
            layoutOrders.visibility = View.GONE
            btnTabProfile.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE4002B.toInt())
            btnTabProfile.setTextColor(0xFFFFFFFF.toInt())
            btnTabOrders.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF0F0F0.toInt())
            btnTabOrders.setTextColor(0xFF333333.toInt())
        }

        btnTabOrders.setOnClickListener {
            layoutProfile.visibility = View.GONE
            layoutOrders.visibility = View.VISIBLE
            btnTabOrders.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE4002B.toInt())
            btnTabOrders.setTextColor(0xFFFFFFFF.toInt())
            btnTabProfile.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF0F0F0.toInt())
            btnTabProfile.setTextColor(0xFF333333.toInt())
            loadOrders(custId, rvOrders)
        }

        // Load profile from Firestore
        if (custId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("customers").document(custId).get()
                .addOnSuccessListener { doc ->
                    val fName = doc.getString("firstName") ?: prefs.getString("CUST_FNAME", "") ?: ""
                    val lName = doc.getString("lastName") ?: prefs.getString("CUST_LNAME", "") ?: ""
                    val phone = doc.getString("phone") ?: prefs.getString("CUST_PHONE", "") ?: ""
                    val email = doc.getString("email") ?: prefs.getString("CUST_EMAIL", "") ?: ""
                    val address = doc.getString("address") ?: prefs.getString("CUST_ADDR", "") ?: ""

                    etFName.setText(fName)
                    etLName.setText(lName)
                    etPhone.setText(phone)
                    etEmail.setText(email)
                    etAddress.setText(address)

                    val initials = "${fName.firstOrNull() ?: ""}${lName.firstOrNull() ?: ""}".uppercase()
                    tvInitials.text = initials.ifEmpty { "?" }
                    tvName.text = "$fName $lName"
                    tvEmail.text = phone
                }
        } else {
            val fName = prefs.getString("CUST_FNAME", "") ?: ""
            val lName = prefs.getString("CUST_LNAME", "") ?: ""
            etFName.setText(fName)
            etLName.setText(lName)
            etPhone.setText(prefs.getString("CUST_PHONE", "") ?: "")
            etAddress.setText(prefs.getString("CUST_ADDR", "") ?: "")
            tvName.text = "$fName $lName"
            tvInitials.text = "${fName.firstOrNull() ?: "?"}".uppercase()
        }

        // Save profile
        btnSave.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (!email.lowercase().endsWith(".com")) {
                Toast.makeText(this, "Please enter a valid email ending with .com", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = hashMapOf<String, Any>(
                "firstName" to etFName.text.toString().trim(),
                "lastName" to etLName.text.toString().trim(),
                "phone" to etPhone.text.toString().trim(),
                "email" to etEmail.text.toString().trim(),
                "address" to etAddress.text.toString().trim()
            )

            if (custId.isNotEmpty()) {
                FirebaseFirestore.getInstance().collection("customers").document(custId)
                    .update(updates)
                    .addOnSuccessListener {
                        prefs.edit()
                            .putString("CUST_FNAME", updates["firstName"] as String)
                            .putString("CUST_LNAME", updates["lastName"] as String)
                            .putString("CUST_PHONE", updates["phone"] as String)
                            .putString("CUST_EMAIL", updates["email"] as String)
                            .putString("CUST_ADDR", updates["address"] as String)
                            .apply()
                        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadOrders(custId: String, rv: RecyclerView) {
        ordersListener?.remove()
        if (custId.isEmpty()) return

        val db = FirebaseFirestore.getInstance()
        ordersListener = db.collection("orders")
            .whereEqualTo("customerId", custId)
            .addSnapshotListener { docs, _ ->
                if (docs == null) return@addSnapshotListener
                val orders = docs.map { doc ->
                    OrderSummary(
                        id = doc.id,
                        orderNum = doc.getString("orderNum") ?: doc.id,
                        status = doc.getString("status") ?: "Pending",
                        total = doc.getDouble("total") ?: 0.0,
                        paymentMethod = doc.getString("paymentMethod") ?: "",
                        deliveryAddress = doc.getString("deliveryAddress") ?: "",
                        createdAt = doc.getDate("createdAt")
                    )
                }.sortedByDescending { it.createdAt }

                rv.adapter = OrderHistoryAdapter(orders,
                    onTrack = { order ->
                        val intent = Intent(this, OrderStatusActivity::class.java)
                        intent.putExtra("ORDER_ID", order.id)
                        intent.putExtra("ORDER_NUM", order.orderNum)
                        intent.putExtra("FINAL_TOTAL", order.total)
                        intent.putExtra("PAYMENT_METHOD", order.paymentMethod)
                        startActivity(intent)
                    },
                    onCancel = { order ->
                        showCancelDialog(order)
                    }
                )
            }
    }

    private fun showCancelDialog(order: OrderSummary) {
        val reasons = arrayOf(
            "Changed my mind",
            "Ordered the wrong items",
            "Delivery time is too long",
            "Decided to dine-in instead",
            "Other"
        )
        var selectedReason = reasons[0]

        AlertDialog.Builder(this)
            .setTitle("Cancel Order #${order.orderNum}")
            .setSingleChoiceItems(reasons, 0) { _, which ->
                selectedReason = reasons[which]
            }
            .setPositiveButton("Confirm Cancel") { _, _ ->
                FirebaseFirestore.getInstance().collection("orders").document(order.id)
                    .update(mapOf("status" to "Canceled", "cancelReason" to selectedReason))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Order canceled", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Go Back", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ordersListener?.remove()
    }
}

data class OrderSummary(
    val id: String,
    val orderNum: String,
    val status: String,
    val total: Double,
    val paymentMethod: String,
    val deliveryAddress: String,
    val createdAt: java.util.Date?
)

class OrderHistoryAdapter(
    private val orders: List<OrderSummary>,
    private val onTrack: (OrderSummary) -> Unit,
    private val onCancel: (OrderSummary) -> Unit
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNum: TextView = view.findViewById(R.id.tvOrderNum)
        val tvStatus: TextView = view.findViewById(R.id.tvOrderStatus)
        val tvTotal: TextView = view.findViewById(R.id.tvOrderTotal)
        val tvDate: TextView = view.findViewById(R.id.tvOrderDate)
        val btnTrack: Button = view.findViewById(R.id.btnTrackOrder)
        val btnCancel: Button = view.findViewById(R.id.btnCancelOrder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.tvNum.text = "Order #${order.orderNum}"
        holder.tvStatus.text = order.status
        holder.tvTotal.text = "₱ ${"%.2f".format(order.total)}"

        val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
        holder.tvDate.text = if (order.createdAt != null) sdf.format(order.createdAt) else "Just now"

        // Status badge color
        val statusColor = when (order.status) {
            "Delivered" -> 0xFF28A745.toInt()
            "Canceled" -> 0xFF999999.toInt()
            "Out for Delivery" -> 0xFF007BFF.toInt()
            "Preparing", "Ready for Pickup" -> 0xFFF59E0B.toInt()
            else -> 0xFFE4002B.toInt()
        }
        holder.tvStatus.setTextColor(statusColor)

        // Show track button for active orders
        val isActive = order.status in listOf("Pending", "Preparing", "Ready for Pickup", "Out for Delivery")
        holder.btnTrack.visibility = if (isActive || order.status == "Delivered") View.VISIBLE else View.GONE
        holder.btnTrack.text = if (order.status == "Delivered") "View Details" else "Track Order"

        // Show cancel button only for Pending
        holder.btnCancel.visibility = if (order.status == "Pending") View.VISIBLE else View.GONE

        holder.btnTrack.setOnClickListener { onTrack(order) }
        holder.btnCancel.setOnClickListener { onCancel(order) }
    }

    override fun getItemCount() = orders.size
}
