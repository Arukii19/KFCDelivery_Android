package com.example.kfcdelivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class StaffOrder(
    val id: String,
    val orderNum: String,
    val customerName: String,
    val status: String,
    val items: List<Map<String, Any>>
)

class StaffDashboardActivity : ComponentActivity() {

    private var ordersListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_dashboard)

        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val staffName = prefs.getString("STAFF_NAME", "Staff") ?: "Staff"

        findViewById<TextView>(R.id.tvStaffName).text = "Hi, $staffName 👨‍🍳"

        val rvOrders = findViewById<RecyclerView>(R.id.rvStaffOrders)
        rvOrders.layoutManager = LinearLayoutManager(this)

        val btnLogout = findViewById<Button>(R.id.btnStaffLogout)
        btnLogout.setOnClickListener {
            prefs.edit()
                .remove("STAFF_ID").remove("STAFF_NAME")
                .remove("STAFF_ROLE")
                .apply()
            finish()
        }

        // Real-time listener for kitchen queue
        val db = FirebaseFirestore.getInstance()
        ordersListener = db.collection("orders")
            .addSnapshotListener { docs, _ ->
                if (docs == null) return@addSnapshotListener

                val activeOrders = docs.filter { doc ->
                    val status = doc.getString("status") ?: ""
                    status == "Accepted" || status == "Preparing" || status == "Ready for Pickup"
                }.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    StaffOrder(
                        id = doc.id,
                        orderNum = doc.getString("orderNum") ?: doc.id,
                        customerName = doc.getString("customerId") ?: "Customer",
                        status = doc.getString("status") ?: "Pending",
                        items = items
                    )
                }.sortedBy { it.status }

                val tvEmpty = findViewById<TextView>(R.id.tvNoOrders)
                if (activeOrders.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvOrders.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvOrders.visibility = View.VISIBLE
                    rvOrders.adapter = StaffOrderAdapter(activeOrders) { order, action ->
                        handleOrderAction(order, action)
                    }
                }
            }
    }

    private fun handleOrderAction(order: StaffOrder, action: String) {
        val newStatus = when (action) {
            "confirm" -> "Preparing"
            "ready" -> "Ready for Pickup"
            else -> return
        }
        FirebaseFirestore.getInstance().collection("orders").document(order.id)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Order #${order.orderNum} → $newStatus", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        ordersListener?.remove()
    }
}

class StaffOrderAdapter(
    private val orders: List<StaffOrder>,
    private val onAction: (StaffOrder, String) -> Unit
) : RecyclerView.Adapter<StaffOrderAdapter.StaffOrderVH>() {

    inner class StaffOrderVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNum: TextView = view.findViewById(R.id.tvStaffOrderNum)
        val tvStatus: TextView = view.findViewById(R.id.tvStaffOrderStatus)
        val tvItems: TextView = view.findViewById(R.id.tvStaffOrderItems)
        val btnAction: Button = view.findViewById(R.id.btnStaffOrderAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffOrderVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff_order, parent, false)
        return StaffOrderVH(view)
    }

    override fun onBindViewHolder(holder: StaffOrderVH, position: Int) {
        val order = orders[position]
        holder.tvNum.text = "Order #${order.orderNum}"

        val statusColor = when (order.status) {
            "Preparing" -> 0xFFF59E0B.toInt()
            "Ready for Pickup" -> 0xFF10B981.toInt()
            else -> 0xFFE4002B.toInt()
        }
        holder.tvStatus.text = order.status
        holder.tvStatus.setTextColor(statusColor)

        // Build items list
        val itemsText = order.items.joinToString("\n") { item ->
            val qty = (item["quantity"] as? Long)?.toInt() ?: 1
            val name = item["name"] as? String ?: "Item"
            "  • ${qty}x $name"
        }
        holder.tvItems.text = itemsText.ifEmpty { "No items" }

        when (order.status) {
            "Accepted" -> {
                holder.btnAction.text = "✓ Start Preparing"
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFF59E0B.toInt())
                holder.btnAction.isEnabled = true
                holder.btnAction.setOnClickListener { onAction(order, "confirm") }
            }
            "Preparing" -> {
                holder.btnAction.text = "✓ Mark Ready for Pickup"
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF10B981.toInt())
                holder.btnAction.isEnabled = true
                holder.btnAction.setOnClickListener { onAction(order, "ready") }
            }
            "Ready for Pickup" -> {
                holder.btnAction.text = "⏳ Waiting for Rider..."
                holder.btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFCCCCCC.toInt())
                holder.btnAction.isEnabled = false
            }
        }
    }

    override fun getItemCount() = orders.size
}
