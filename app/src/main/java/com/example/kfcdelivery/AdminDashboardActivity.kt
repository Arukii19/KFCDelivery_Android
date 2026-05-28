package com.example.kfcdelivery

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.util.Locale

data class AdminOrder(
    val id: String,
    val orderNum: String,
    val customerName: String,
    val status: String,
    val items: List<Map<String, Any>>,
    val total: Double
)

data class AdminStaff(
    val id: String,
    val firstName: String,
    val email: String,
    val role: String
)

data class AdminRider(
    val id: String,
    val firstName: String,
    val vehicle: String,
    val isApproved: Boolean
)

class AdminDashboardActivity : ComponentActivity() {

    private var menuListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var staffListener: ListenerRegistration? = null
    private var ridersListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()

    private var selectedItemForImage: MenuItem? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
        if (uri != null) {
            uploadImageToStorage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val adminName = prefs.getString("ADMIN_NAME", "Admin") ?: "Admin"
        
        findViewById<TextView>(R.id.tvAdminName).text = "Hi, $adminName 👑"

        findViewById<Button>(R.id.btnAdminLogout).setOnClickListener {
            prefs.edit().remove("ADMIN_ID").remove("ADMIN_NAME").remove("ADMIN_ROLE").apply()
            finish()
        }

        // Setup Tabs
        val btnTabMenu = findViewById<Button>(R.id.btnTabMenu)
        val btnTabOrders = findViewById<Button>(R.id.btnTabOrders)
        val btnTabStaff = findViewById<Button>(R.id.btnTabStaff)
        val btnTabRiders = findViewById<Button>(R.id.btnTabRiders)
        
        val layoutMenu = findViewById<LinearLayout>(R.id.layoutAdminMenu)
        val layoutOrders = findViewById<LinearLayout>(R.id.layoutAdminOrders)
        val layoutStaff = findViewById<android.widget.RelativeLayout>(R.id.layoutAdminStaff)
        val layoutRiders = findViewById<LinearLayout>(R.id.layoutAdminRiders)

        fun showTab(tab: String) {
            layoutMenu.visibility = if (tab == "menu") View.VISIBLE else View.GONE
            layoutOrders.visibility = if (tab == "orders") View.VISIBLE else View.GONE
            layoutStaff.visibility = if (tab == "staff") View.VISIBLE else View.GONE
            layoutRiders.visibility = if (tab == "riders") View.VISIBLE else View.GONE
            
            btnTabMenu.backgroundTintList = ColorStateList.valueOf(if (tab == "menu") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabMenu.setTextColor(if (tab == "menu") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
            
            btnTabOrders.backgroundTintList = ColorStateList.valueOf(if (tab == "orders") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabOrders.setTextColor(if (tab == "orders") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
            
            btnTabStaff.backgroundTintList = ColorStateList.valueOf(if (tab == "staff") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabStaff.setTextColor(if (tab == "staff") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
            
            btnTabRiders.backgroundTintList = ColorStateList.valueOf(if (tab == "riders") 0xFFE4002B.toInt() else 0xFFF0F0F0.toInt())
            btnTabRiders.setTextColor(if (tab == "riders") 0xFFFFFFFF.toInt() else 0xFF333333.toInt())
        }

        btnTabMenu.setOnClickListener { showTab("menu") }
        btnTabOrders.setOnClickListener { showTab("orders") }
        btnTabStaff.setOnClickListener { showTab("staff") }
        btnTabRiders.setOnClickListener { showTab("riders") }

        val rvMenu = findViewById<RecyclerView>(R.id.rvAdminMenu)
        rvMenu.layoutManager = LinearLayoutManager(this)
        val rvOrders = findViewById<RecyclerView>(R.id.rvAdminOrders)
        rvOrders.layoutManager = LinearLayoutManager(this)

        // Menu Listener
        menuListener = db.collection("menuItems")
            .addSnapshotListener { docs, _ ->
                if (docs == null) return@addSnapshotListener
                val items = docs.map { doc ->
                    MenuItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        category = doc.getString("category") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        isAvailable = doc.getBoolean("isAvailable") ?: true
                    )
                }.sortedBy { it.name }

                rvMenu.adapter = AdminMenuAdapter(items,
                    onToggleAvailability = { item, isAvailable ->
                        db.collection("menuItems").document(item.id)
                            .update("isAvailable", isAvailable)
                    },
                    onEditPrice = { item ->
                        showEditPriceDialog(item)
                    },
                    onEditImage = { item ->
                        selectedItemForImage = item
                        pickImageLauncher.launch("image/*")
                    }
                )
            }

        // Orders Listener
        ordersListener = db.collection("orders")
            .addSnapshotListener { docs, _ ->
                if (docs == null) return@addSnapshotListener

                val activeOrders = docs.filter { doc ->
                    val status = doc.getString("status") ?: ""
                    status == "Pending" || status == "Ready for Pickup"
                }.map { doc ->
                    @Suppress("UNCHECKED_CAST")
                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    AdminOrder(
                        id = doc.id,
                        orderNum = doc.getString("orderNum") ?: doc.id,
                        customerName = doc.getString("customerId") ?: "Customer",
                        status = doc.getString("status") ?: "Pending",
                        items = items,
                        total = doc.getDouble("total") ?: 0.0
                    )
                }.sortedByDescending { it.status } // Shows Ready for Pickup first, then Pending

                val tvEmpty = findViewById<TextView>(R.id.tvNoAdminOrders)
                if (activeOrders.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvOrders.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvOrders.visibility = View.VISIBLE
                    rvOrders.adapter = AdminOrderAdapter(activeOrders) { order, action ->
                        handleOrderAction(order, action)
                    }
                }
            }

        // Staff Listener
        val rvStaff = findViewById<RecyclerView>(R.id.rvAdminStaff)
        rvStaff.layoutManager = LinearLayoutManager(this)
        staffListener = db.collection("employees")
            .addSnapshotListener { docs, _ ->
                if (docs == null) return@addSnapshotListener
                val staffList = docs.map { doc ->
                    AdminStaff(
                        id = doc.id,
                        firstName = doc.getString("firstName") ?: "",
                        email = doc.getString("email") ?: "",
                        role = doc.getString("role") ?: ""
                    )
                }.sortedBy { it.firstName }
                
                rvStaff.adapter = AdminStaffAdapter(staffList)
            }

        val fabAddStaff = findViewById<android.widget.ImageButton>(R.id.fabAddStaff)
        fabAddStaff.setOnClickListener {
            showAddStaffDialog()
        }

        // Riders Listener
        val rvRiders = findViewById<RecyclerView>(R.id.rvAdminRiders)
        rvRiders.layoutManager = LinearLayoutManager(this)
        ridersListener = db.collection("riders")
            .addSnapshotListener { docs, _ ->
                if (docs == null) return@addSnapshotListener
                val ridersList = docs.map { doc ->
                    AdminRider(
                        id = doc.id,
                        firstName = doc.getString("firstName") ?: "",
                        vehicle = doc.getString("vehicle") ?: "",
                        isApproved = doc.getBoolean("isApproved") ?: false
                    )
                }.sortedBy { it.firstName }
                
                val tvNoRiders = findViewById<TextView>(R.id.tvNoAdminRiders)
                if (ridersList.none { !it.isApproved }) {
                    // Just a visual cue if no one is pending, but still show the list
                    // Actually, let's just show the list always.
                    tvNoRiders.visibility = View.GONE
                } else {
                    tvNoRiders.visibility = View.GONE
                }
                
                rvRiders.adapter = AdminRiderAdapter(ridersList) { riderId ->
                    db.collection("riders").document(riderId).update("isApproved", true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Rider Approved!", Toast.LENGTH_SHORT).show()
                        }
                }
            }

        showTab("menu")
    }

    private fun handleOrderAction(order: AdminOrder, action: String) {
        if (action == "accept") {
            db.collection("orders").document(order.id)
                .update("status", "Accepted")
                .addOnSuccessListener {
                    Toast.makeText(this, "Order Sent to Kitchen", Toast.LENGTH_SHORT).show()
                }
        } else if (action == "assign") {
            showAssignRiderDialog(order)
        }
    }

    private fun showAssignRiderDialog(order: AdminOrder) {
        db.collection("riders").get()
            .addOnSuccessListener { docs ->
                if (docs.isEmpty) {
                    Toast.makeText(this, "No riders are currently Available!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val riders = docs.map { doc ->
                    val name = doc.getString("firstName") + " " + doc.getString("lastName")
                    val vehicle = doc.getString("vehicle") ?: ""
                    Pair(doc.id, "$name ($vehicle)")
                }

                val dialogView = layoutInflater.inflate(R.layout.dialog_assign_rider, null)
                val llRidersList = dialogView.findViewById<LinearLayout>(R.id.llRidersList)
                val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelAssign)

                val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setView(dialogView)
                    .create()
                
                // Remove default background so custom rounded corners show
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                btnCancel.setOnClickListener { dialog.dismiss() }

                for (rider in riders) {
                    val riderBtn = Button(this).apply {
                        text = rider.second
                        isAllCaps = false
                        backgroundTintList = ColorStateList.valueOf(0xFFF9F9FB.toInt())
                        setTextColor(0xFF333333.toInt())
                        setPadding(16, 24, 16, 24)
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, 16)
                        layoutParams = params

                        setOnClickListener {
                            val selectedRiderId = rider.first
                            val selectedRiderName = rider.second

                            db.collection("orders").document(order.id)
                                .update(mapOf(
                                    "status" to "Out for Delivery",
                                    "riderId" to selectedRiderId
                                ))
                                .addOnSuccessListener {
                                    Toast.makeText(this@AdminDashboardActivity, "Assigned to $selectedRiderName", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                }
                        }
                    }
                    llRidersList.addView(riderBtn)
                }

                dialog.show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load riders", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToStorage(uri: android.net.Uri) {
        val item = selectedItemForImage ?: return
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("menu_images/${item.id}_${System.currentTimeMillis()}.jpg")
        
        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("menuItems").document(item.id)
                        .update("imageUrl", downloadUrl.toString())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Image updated!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditPriceDialog(item: MenuItem) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        input.setText(item.price.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Price: ${item.name}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPrice = input.text.toString().toDoubleOrNull()
                if (newPrice != null) {
                    db.collection("menuItems").document(item.id)
                        .update("price", newPrice)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Price updated to ₱$newPrice", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddStaffDialog() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        builder.setTitle("Add New Staff")

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(this).apply {
            hint = "First Name"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        val emailInput = EditText(this).apply {
            hint = "Email Address"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }
        val passwordInput = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        }

        layout.addView(nameInput)
        layout.addView(emailInput)
        layout.addView(passwordInput)

        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val name = nameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val newStaff = hashMapOf(
                    "firstName" to name,
                    "lastName" to "",
                    "email" to email,
                    "password" to password,
                    "role" to "Kitchen",
                    "branchId" to "",
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("employees").add(newStaff)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Staff added successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to add staff: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        menuListener?.remove()
        ordersListener?.remove()
    }
}

class AdminMenuAdapter(
    private val items: List<MenuItem>,
    private val onToggleAvailability: (MenuItem, Boolean) -> Unit,
    private val onEditPrice: (MenuItem) -> Unit,
    private val onEditImage: (MenuItem) -> Unit
) : RecyclerView.Adapter<AdminMenuAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAdminItemName)
        val tvCategory: TextView = view.findViewById(R.id.tvAdminItemCategory)
        val tvPrice: TextView = view.findViewById(R.id.tvAdminItemPrice)
        val swAvailable: Switch = view.findViewById(R.id.swItemAvailable)
        val btnEditPrice: Button = view.findViewById(R.id.btnEditPrice)
        val ivImage: ImageView = view.findViewById(R.id.ivAdminItemImage)
        val btnEditImage: Button = view.findViewById(R.id.btnEditImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_menu, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvCategory.text = item.category
        holder.tvPrice.text = String.format(Locale.getDefault(), "₱ %.2f", item.price)

        holder.swAvailable.setOnCheckedChangeListener(null)
        holder.swAvailable.isChecked = item.isAvailable
        holder.swAvailable.setOnCheckedChangeListener { _, isChecked ->
            onToggleAvailability(item, isChecked)
        }

        holder.btnEditPrice.setOnClickListener {
            onEditPrice(item)
        }

        holder.btnEditImage.setOnClickListener {
            onEditImage(item)
        }

        if (item.imageUrl.isNotEmpty()) {
            if (item.imageUrl.startsWith("http")) {
                Glide.with(holder.itemView.context)
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(holder.ivImage)
            } else {
                var resName = item.imageUrl.substringAfterLast("/")
                resName = resName.substringBeforeLast(".").lowercase()
                val resId = holder.itemView.context.resources.getIdentifier(resName, "drawable", holder.itemView.context.packageName)
                if (resId != 0) {
                    holder.ivImage.setImageResource(resId)
                } else {
                    holder.ivImage.setImageResource(R.drawable.kfc_logo)
                }
            }
        } else {
            holder.ivImage.setImageResource(R.drawable.kfc_logo)
        }
    }

    override fun getItemCount() = items.size
}

class AdminOrderAdapter(
    private val orders: List<AdminOrder>,
    private val onAction: (AdminOrder, String) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNum: TextView = view.findViewById(R.id.tvAdminOrderNum)
        val tvStatus: TextView = view.findViewById(R.id.tvAdminOrderStatus)
        val tvItems: TextView = view.findViewById(R.id.tvAdminOrderItems)
        val btnAction: Button = view.findViewById(R.id.btnAdminOrderAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_order, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val order = orders[position]
        holder.tvNum.text = "Order #${order.orderNum}"

        val statusColor = when (order.status) {
            "Ready for Pickup" -> 0xFF10B981.toInt() // Green
            else -> 0xFFE4002B.toInt() // Red (Pending)
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

        if (order.status == "Pending") {
            holder.btnAction.text = "Accept Order"
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(0xFFE4002B.toInt())
            holder.btnAction.setOnClickListener { onAction(order, "accept") }
        } else if (order.status == "Ready for Pickup") {
            holder.btnAction.text = "Assign Rider"
            holder.btnAction.backgroundTintList = ColorStateList.valueOf(0xFF10B981.toInt())
            holder.btnAction.setOnClickListener { onAction(order, "assign") }
        }
    }

    override fun getItemCount() = orders.size
}

class AdminStaffAdapter(private val items: List<AdminStaff>) : RecyclerView.Adapter<AdminStaffAdapter.ViewHolder>() {
    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAdminStaffName)
        val tvEmail: TextView = view.findViewById(R.id.tvAdminStaffEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_staff, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = "${item.firstName} (${item.role})"
        holder.tvEmail.text = item.email
    }

    override fun getItemCount() = items.size
}

class AdminRiderAdapter(
    private val items: List<AdminRider>,
    private val onApprove: (String) -> Unit
) : RecyclerView.Adapter<AdminRiderAdapter.ViewHolder>() {
    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvAdminRiderName)
        val tvVehicle: TextView = view.findViewById(R.id.tvAdminRiderVehicle)
        val tvStatus: TextView = view.findViewById(R.id.tvAdminRiderStatus)
        val btnApprove: Button = view.findViewById(R.id.btnApproveRider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_rider, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.firstName
        holder.tvVehicle.text = item.vehicle
        
        if (item.isApproved) {
            holder.tvStatus.text = "Approved"
            holder.tvStatus.setTextColor(0xFF10B981.toInt()) // Green
            holder.btnApprove.visibility = android.view.View.GONE
        } else {
            holder.tvStatus.text = "Pending Approval"
            holder.tvStatus.setTextColor(0xFFE4002B.toInt()) // Red
            holder.btnApprove.visibility = android.view.View.VISIBLE
        }

        holder.btnApprove.setOnClickListener {
            onApprove(item.id)
        }
    }

    override fun getItemCount() = items.size
}
