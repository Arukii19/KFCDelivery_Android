package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DashboardActivity : ComponentActivity() {

    private val allMenuItems = mutableListOf<MenuItem>()
    private val displayedItems = mutableListOf<MenuItem>()
    private lateinit var menuAdapter: DashboardMenuAdapter
    private var currentCategory = "All"
    private var currentQuery = ""
    private lateinit var categoryContainer: LinearLayout
    private lateinit var tvTotalPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences("KFCAppPrefs", MODE_PRIVATE)
        val custFName = prefs.getString("CUST_FNAME", "Guest") ?: "Guest"

        // Update header with user name
        val tvMenuTitle = findViewById<TextView>(R.id.tvMenuTitle)
        tvMenuTitle.text = "Hi, $custFName! 🍗"

        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        updateTotalUI()

        // Profile button
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Menu label
        val tvPopularItems = findViewById<TextView>(R.id.tvPopularItems)
        tvPopularItems.text = "Menu"

        // Category container (horizontal scroll LinearLayout)
        val categoriesScroll = findViewById<HorizontalScrollView>(R.id.categoriesScroll)
        categoryContainer = categoriesScroll.getChildAt(0) as LinearLayout
        categoryContainer.removeAllViews()

        // Search
        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString() ?: ""
                filterAndRender()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // RecyclerView for menu items
        val lvMenu = findViewById<ListView>(R.id.lvMenu)
        menuAdapter = DashboardMenuAdapter(this, displayedItems, onQuantityChanged = {
            updateTotalUI()
        })
        lvMenu.adapter = menuAdapter

        // Checkout button
        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            if (CartManager.getTotalQuantity() == 0) {
                Toast.makeText(this, "Please add items to your cart first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, CartActivity::class.java))
        }

        // Load menu from Firestore
        fetchMenu()
    }

    private fun fetchMenu() {
        val db = FirebaseFirestore.getInstance()
        db.collection("menuItems")
            .get()
            .addOnSuccessListener { docs ->
                allMenuItems.clear()
                CartManager.items.forEach { cartItem ->
                    // Reset quantities from CartManager state on return
                }

                for (doc in docs) {
                    val cartQty = CartManager.items.find { it.menuItemId == doc.id }?.quantity ?: 0
                    allMenuItems.add(
                        MenuItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            description = doc.getString("description") ?: "",
                            category = doc.getString("category") ?: "Other",
                            price = (doc.getDouble("price") ?: 0.0),
                            imageUrl = doc.getString("imageUrl") ?: "",
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            quantity = cartQty
                        )
                    )
                }

                if (allMenuItems.isEmpty()) {
                    seedDefaultMenu(db)
                } else {
                    buildCategoryButtons()
                    filterAndRender()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load menu", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buildCategoryButtons() {
        categoryContainer.removeAllViews()
        val categories = listOf("All") + allMenuItems.map { it.category }.distinct().sorted()

        categories.forEach { category ->
            val ll = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(0, 0, 36, 0)
                isClickable = true
                isFocusable = true
            }

            val emoji = when (category) {
                "Meals" -> "🍱"; "Sandwiches", "Burgers" -> "🍔"
                "Buckets" -> "🪣"; "Snacks" -> "🍟"; "Sides" -> "🍟"
                "Drinks" -> "🥤"; "Desserts" -> "🍦"
                "All" -> "🍗"; else -> "🍽️"
            }

            val tvEmoji = TextView(this).apply {
                text = emoji
                textSize = 28f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(120, 120)
            }

            val tvLabel = TextView(this).apply {
                text = category
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(if (category == currentCategory) 0xFFE4002B.toInt() else 0xFF555555.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = 8 }
            }

            ll.addView(tvEmoji)
            ll.addView(tvLabel)

            ll.setOnClickListener {
                currentCategory = if (currentCategory == category) "All" else category
                buildCategoryButtons()
                filterAndRender()
            }

            categoryContainer.addView(ll)
        }
    }

    private fun filterAndRender() {
        displayedItems.clear()
        displayedItems.addAll(allMenuItems.filter { item ->
            val matchesCategory = currentCategory == "All" || item.category == currentCategory
            val matchesSearch = item.name.contains(currentQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        })
        menuAdapter.notifyDataSetChanged()
    }

    private fun updateTotalUI() {
        tvTotalPrice.text = String.format(Locale.getDefault(), "₱ %.2f", CartManager.getTotal())
    }

    private fun seedDefaultMenu(db: FirebaseFirestore) {
        val items = listOf(
            hashMapOf("name" to "KFC Chicken (1pc)", "category" to "Meals", "price" to 85.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "Zinger Burger", "category" to "Sandwiches", "price" to 90.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "Large Fries", "category" to "Sides", "price" to 50.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "Large Drink", "category" to "Drinks", "price" to 45.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "KFC Sundae", "category" to "Desserts", "price" to 35.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "6pc Bucket", "category" to "Buckets", "price" to 360.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "KFC Twister", "category" to "Sandwiches", "price" to 95.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "Mashed Potato", "category" to "Sides", "price" to 40.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "Coleslaw", "category" to "Sides", "price" to 35.0, "imageUrl" to "", "isAvailable" to true),
            hashMapOf("name" to "Iced Tea", "category" to "Drinks", "price" to 40.0, "imageUrl" to "", "isAvailable" to true)
        )
        var count = 0
        for (item in items) {
            db.collection("menuItems").add(item).addOnSuccessListener { ref ->
                allMenuItems.add(MenuItem(
                    id = ref.id,
                    name = item["name"] as String,
                    category = item["category"] as String,
                    price = item["price"] as Double,
                    imageUrl = "",
                    isAvailable = true
                ))
                count++
                if (count == items.size) {
                    buildCategoryButtons()
                    filterAndRender()
                }
            }
        }
    }
}

class DashboardMenuAdapter(
    private val context: android.content.Context,
    private val items: List<MenuItem>,
    private val onQuantityChanged: () -> Unit
) : ArrayAdapter<MenuItem>(context, R.layout.item_menu, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_menu, parent, false)
        val item = items[position]

        val ivImage = view.findViewById<ImageView>(R.id.ivMenuImage)
        val tvName = view.findViewById<TextView>(R.id.tvMenuName)
        val tvPrice = view.findViewById<TextView>(R.id.tvMenuPrice)
        val tvQty = view.findViewById<TextView>(R.id.tvMenuQuantity)
        val btnAdd = view.findViewById<Button>(R.id.btnAddMenu)
        val btnMinus = view.findViewById<Button>(R.id.btnMinusMenu)

        tvName.text = item.name
        tvPrice.text = String.format(Locale.getDefault(), "₱ %.2f", item.price)
        tvQty.text = item.quantity.toString()

        if (item.imageUrl.isNotEmpty()) {
            if (item.imageUrl.startsWith("http")) {
                Glide.with(context).load(item.imageUrl).placeholder(R.drawable.kfc_chicken).into(ivImage)
            } else {
                // Parse local image name (e.g., "images/kfc_burger.png" -> "kfc_burger")
                var resName = item.imageUrl.substringAfterLast("/")
                resName = resName.substringBeforeLast(".").lowercase()
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) {
                    ivImage.setImageResource(resId)
                } else {
                    ivImage.setImageResource(R.drawable.kfc_chicken)
                }
            }
        } else if (item.imageResId != 0) {
            ivImage.setImageResource(item.imageResId)
        } else {
            // Use category emoji fallback
            ivImage.setImageResource(R.drawable.kfc_chicken)
        }

        if (!item.isAvailable) {
            view.alpha = 0.4f
            btnAdd.isEnabled = false
            btnMinus.isEnabled = false
        } else {
            view.alpha = 1f
            btnAdd.isEnabled = true
            btnMinus.isEnabled = true
        }

        btnAdd.setOnClickListener {
            item.quantity++
            tvQty.text = item.quantity.toString()
            CartManager.addItem(item)
            onQuantityChanged()
        }

        btnMinus.setOnClickListener {
            if (item.quantity > 0) {
                item.quantity--
                tvQty.text = item.quantity.toString()
                CartManager.changeQuantity(item.id, -1)
                onQuantityChanged()
            }
        }

        return view
    }
}
