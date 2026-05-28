package com.example.kfcdelivery

import android.content.res.ColorStateList
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

class RiderDashboardActivity : ComponentActivity() {

    private var deliveriesListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_dashboard)

        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val riderId = prefs.getString("RIDER_ID", "") ?: ""
        val fName = prefs.getString("RIDER_FNAME", "") ?: ""
        val lName = prefs.getString("RIDER_LNAME", "") ?: ""
        val phone = prefs.getString("RIDER_PHONE", "") ?: ""
        val vehicle = prefs.getString("RIDER_VEHICLE", "") ?: ""
        var currentStatus = prefs.getString("RIDER_STATUS", "Offline") ?: "Offline"

        // Header
        val tvRiderName = findViewById<TextView>(R.id.tvRiderName)
        tvRiderName.text = "Hi, $fName! 🛵"

        // Tabs
        val btnTabActive = findViewById<Button>(R.id.btnTabActive)
        val btnTabHistory = findViewById<Button>(R.id.btnTabHistory)
        val btnTabProfile = findViewById<Button>(R.id.btnTabRiderProfile)
        val layoutActive = findViewById<LinearLayout>(R.id.layoutRiderActive)
        val layoutHistory = findViewById<LinearLayout>(R.id.layoutRiderHistory)
        val layoutProfile = findViewById<LinearLayout>(R.id.layoutRiderProfile)

        val rvActive = findViewById<RecyclerView>(R.id.rvRiderActive)
        val rvHistory = findViewById<RecyclerView>(R.id.rvRiderHistory)
        rvActive.layoutManager = LinearLayoutManager(this)
        rvHistory.layoutManager = LinearLayoutManager(this)

        fun showTab(tab: String) {
            layoutActive.visibility = if (tab == "active") View.VISIBLE else View.GONE
            layoutHistory.visibility = if (tab == "history") View.VISIBLE else View.GONE
            layoutProfile.visibility = if (tab == "profile") View.VISIBLE else View.GONE
            btnTabActive.backgroundTintList = ColorStateList.valueOf(if (tab == "active") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabActive.setTextColor(if (tab == "active") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
            btnTabHistory.backgroundTintList = ColorStateList.valueOf(if (tab == "history") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabHistory.setTextColor(if (tab == "history") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
            btnTabProfile.backgroundTintList = ColorStateList.valueOf(if (tab == "profile") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabProfile.setTextColor(if (tab == "profile") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
        }

        btnTabActive.setOnClickListener { showTab("active") }
        btnTabHistory.setOnClickListener { showTab("history") }
        btnTabProfile.setOnClickListener { showTab("profile") }

        // Profile tab
        findViewById<TextView>(R.id.tvRiderProfileName).text = "$fName $lName"
        findViewById<TextView>(R.id.tvRiderProfilePhone).text = phone
        findViewById<TextView>(R.id.tvRiderProfileVehicle).text = vehicle
        val btnLogout = findViewById<Button>(R.id.btnRiderLogout)
        btnLogout.setOnClickListener {
            prefs.edit().remove("RIDER_ID").remove("RIDER_FNAME").remove("RIDER_LNAME")
                .remove("RIDER_PHONE").remove("RIDER_VEHICLE").remove("RIDER_BRANCH_ID").remove("RIDER_STATUS")
                .apply()
            finish()
        }

        // Load active deliveries
        if (riderId.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()

            deliveriesListener = db.collection("orders")
                .whereEqualTo("riderId", riderId)
                .whereEqualTo("status", "Out for Delivery")
                .addSnapshotListener { docs, _ ->
                    if (docs == null) return@addSnapshotListener
                    val deliveries = docs.map { doc ->
                        @Suppress("UNCHECKED_CAST")
                        RiderDelivery(
                            id = doc.id,
                            orderNum = doc.getString("orderNum") ?: doc.id,
                            customerName = "Customer",  // lookup separately if needed
                            customerPhone = "",
                            deliveryAddress = doc.getString("deliveryAddress") ?: "",
                            total = doc.getDouble("total") ?: 0.0,
                            paymentMethod = doc.getString("paymentMethod") ?: "Cash"
                        )
                    }
                    val tvEmpty = findViewById<TextView>(R.id.tvNoDeliveries)
                    if (deliveries.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvActive.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvActive.visibility = View.VISIBLE
                        rvActive.adapter = RiderActiveAdapter(deliveries) { delivery ->
                            confirmDelivery(delivery, riderId)
                        }
                    }
                }

            historyListener = db.collection("orders")
                .whereEqualTo("riderId", riderId)
                .whereEqualTo("status", "Delivered")
                .addSnapshotListener { docs, _ ->
                    if (docs == null) return@addSnapshotListener
                    val history = docs.map { doc ->
                        RiderDelivery(
                            id = doc.id,
                            orderNum = doc.getString("orderNum") ?: doc.id,
                            customerName = "Customer",
                            customerPhone = "",
                            deliveryAddress = doc.getString("deliveryAddress") ?: "",
                            total = doc.getDouble("total") ?: 0.0,
                            paymentMethod = doc.getString("paymentMethod") ?: "Cash",
                            date = doc.getDate("createdAt")
                        )
                    }.sortedByDescending { it.date }
                    rvHistory.adapter = RiderHistoryAdapter(history)
                }
        }

        showTab("active")
    }

    private fun confirmDelivery(delivery: RiderDelivery, riderId: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delivery")
            .setMessage("Mark Order #${delivery.orderNum} as delivered?\nCollect: ₱${"%.2f".format(delivery.total)}")
            .setPositiveButton("Yes, Delivered!") { _, _ ->
                FirebaseFirestore.getInstance().collection("orders").document(delivery.id)
                    .update(mapOf(
                        "status" to "Delivered",
                        "paymentStatus" to "Completed"
                    ))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Great job! Delivery complete 🎉", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        deliveriesListener?.remove()
        historyListener?.remove()
    }
}

data class RiderDelivery(
    val id: String,
    val orderNum: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val total: Double,
    val paymentMethod: String,
    val date: java.util.Date? = null
)

class RiderActiveAdapter(
    private val deliveries: List<RiderDelivery>,
    private val onMarkDelivered: (RiderDelivery) -> Unit
) : RecyclerView.Adapter<RiderActiveAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderNum: TextView = view.findViewById(R.id.tvRiderOrderNum)
        val tvAddress: TextView = view.findViewById(R.id.tvRiderAddress)
        val tvTotal: TextView = view.findViewById(R.id.tvRiderTotal)
        val tvPayment: TextView = view.findViewById(R.id.tvRiderPayment)
        val btnDelivered: Button = view.findViewById(R.id.btnMarkDelivered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rider_delivery, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = deliveries[position]
        holder.tvOrderNum.text = "Order #${d.orderNum}"
        holder.tvAddress.text = "📍 ${d.deliveryAddress}"
        holder.tvTotal.text = "Collect: ₱${"%.2f".format(d.total)}"
        holder.tvPayment.text = "Payment: ${d.paymentMethod}"
        holder.btnDelivered.setOnClickListener { onMarkDelivered(d) }
    }

    override fun getItemCount() = deliveries.size
}

class RiderHistoryAdapter(
    private val history: List<RiderDelivery>
) : RecyclerView.Adapter<RiderHistoryAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderNum: TextView = view.findViewById(R.id.tvRiderHistOrderNum)
        val tvAddress: TextView = view.findViewById(R.id.tvRiderHistAddress)
        val tvTotal: TextView = view.findViewById(R.id.tvRiderHistTotal)
        val tvDate: TextView = view.findViewById(R.id.tvRiderHistDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rider_history, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = history[position]
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        holder.tvOrderNum.text = "Order #${d.orderNum}"
        holder.tvAddress.text = "📍 ${d.deliveryAddress}"
        holder.tvTotal.text = "₱${"%.2f".format(d.total)}"
        holder.tvDate.text = if (d.date != null) sdf.format(d.date) else "—"
    }

    override fun getItemCount() = history.size
}
