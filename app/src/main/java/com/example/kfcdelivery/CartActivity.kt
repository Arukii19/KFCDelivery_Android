package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class CartActivity : ComponentActivity() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var tvFinalTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        tvFinalTotal = findViewById(R.id.tvFinalTotal)

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        rvCart.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(CartManager.items, onQuantityChanged = { updateTotal() })
        rvCart.adapter = cartAdapter

        updateTotal()

        val etLocation = findViewById<EditText>(R.id.etLocation)
        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val savedAddress = prefs.getString("CUST_ADDR", "") ?: ""
        if (savedAddress.isNotEmpty()) etLocation.setText(savedAddress)

        val rgPaymentMethod = findViewById<RadioGroup>(R.id.rgPaymentMethod)

        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)
        btnConfirmOrder.setOnClickListener {
            if (CartManager.items.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val address = etLocation.text.toString().trim()
            if (address.isEmpty()) {
                Toast.makeText(this, "Please enter a delivery address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val paymentMethod = when (rgPaymentMethod.checkedRadioButtonId) {
                R.id.rbGCash -> "GCash"
                else -> "Cash"
            }

            val custId = prefs.getString("CUST_ID", "") ?: ""
            val custFName = prefs.getString("CUST_FNAME", "") ?: ""
            val custLName = prefs.getString("CUST_LNAME", "") ?: ""
            val customerName = "$custFName $custLName".trim().ifEmpty { "Customer" }

            if (custId.isEmpty()) {
                Toast.makeText(this, "Please log in to place an order", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                return@setOnClickListener
            }

            btnConfirmOrder.isEnabled = false
            btnConfirmOrder.text = "Placing Order..."

            val orderItems = CartManager.items.map { item ->
                hashMapOf(
                    "menuItemId" to item.menuItemId,
                    "name" to item.name,
                    "quantity" to item.quantity,
                    "price" to item.price,
                    "subtotal" to (item.price * item.quantity)
                )
            }

            val orderNum = "KFC-${System.currentTimeMillis() % 100000}"

            val finalTotal = CartManager.getTotal()

            val orderMap = hashMapOf(
                "orderNum" to orderNum,
                "customerId" to custId,
                "customerName" to customerName,
                "riderId" to null,
                "items" to orderItems,
                "total" to finalTotal,
                "status" to "Pending",
                "deliveryAddress" to address,
                "paymentMethod" to paymentMethod,
                "paymentStatus" to "Pending",
                "cancelReason" to null,
                "createdAt" to FieldValue.serverTimestamp()
            )

            val db = FirebaseFirestore.getInstance()
            db.collection("orders").add(orderMap)
                .addOnSuccessListener { docRef ->
                    CartManager.clear()
                    val intent = Intent(this, OrderStatusActivity::class.java)
                    intent.putExtra("ORDER_ID", docRef.id)
                    intent.putExtra("ORDER_NUM", orderNum)
                    intent.putExtra("FINAL_TOTAL", finalTotal)
                    intent.putExtra("PAYMENT_METHOD", paymentMethod)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    btnConfirmOrder.isEnabled = true
                    btnConfirmOrder.text = "Confirm Order"
                    Toast.makeText(this, "Order failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun updateTotal() {
        tvFinalTotal.text = String.format(Locale.getDefault(), "₱ %.2f", CartManager.getTotal())
    }
}

class CartAdapter(
    private val items: MutableList<CartItem>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCartItemName)
        val tvPrice: TextView = view.findViewById(R.id.tvCartItemPrice)
        val tvQty: TextView = view.findViewById(R.id.tvCartItemQty)
        val btnAdd: Button = view.findViewById(R.id.btnCartAdd)
        val btnMinus: Button = view.findViewById(R.id.btnCartMinus)
        val btnRemove: ImageView = view.findViewById(R.id.btnCartRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvPrice.text = String.format(Locale.getDefault(), "₱ %.2f", item.price * item.quantity)
        holder.tvQty.text = item.quantity.toString()

        holder.btnAdd.setOnClickListener {
            CartManager.changeQuantity(item.menuItemId, 1)
            notifyItemChanged(position)
            onQuantityChanged()
        }

        holder.btnMinus.setOnClickListener {
            if (item.quantity > 1) {
                CartManager.changeQuantity(item.menuItemId, -1)
                notifyItemChanged(position)
                onQuantityChanged()
            } else {
                CartManager.removeItem(item.menuItemId)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, items.size)
                onQuantityChanged()
            }
        }

        holder.btnRemove.setOnClickListener {
            CartManager.removeItem(item.menuItemId)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, items.size)
            onQuantityChanged()
        }
    }

    override fun getItemCount() = items.size
}
