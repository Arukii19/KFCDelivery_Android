package com.example.kfcdelivery

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class DashboardActivity : ComponentActivity() {

    private lateinit var tvTotalPrice: TextView
    private lateinit var menuAdapter: MenuAdapter
    private val menuItems = mutableListOf<MenuItem>()
    private val displayedMenuItems = mutableListOf<MenuItem>()
    private var currentCategory = "All"
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvTotalPrice = findViewById(R.id.tvTotalPrice)

        val tvMenuTitle = findViewById<TextView>(R.id.tvMenuTitle)
        var userName = intent.getStringExtra("USER_NAME")
        var userEmail = intent.getStringExtra("USER_EMAIL")
        
        val sharedPref = getSharedPreferences("KFCAppPrefs", android.content.Context.MODE_PRIVATE)
        if (userName != null && userEmail != null) {
            sharedPref.edit().putString("SESSION_USER_NAME", userName).putString("SESSION_USER_EMAIL", userEmail).apply()
        } else {
            userName = sharedPref.getString("SESSION_USER_NAME", "")
            userEmail = sharedPref.getString("SESSION_USER_EMAIL", "")
        }

        if (!userName.isNullOrEmpty()) {
            tvMenuTitle.text = "Welcome, $userName!"
        }

        findViewById<android.widget.ImageView>(R.id.btnProfile).setOnClickListener {
            val intent = android.content.Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_NAME", userName)
            intent.putExtra("USER_EMAIL", userEmail)
            startActivity(intent)
        }
        
        // Initialize Data
        menuItems.add(MenuItem("chicken", "Chicken", "Our signature 11 herbs and spices crispy fried chicken bucket.", 85.00, R.drawable.kfc_chicken))
        menuItems.add(MenuItem("burger", "Zinger Burger", "Crispy fried chicken sandwich with lettuce and mayonnaise.", 90.00, R.drawable.kfc_burger))
        menuItems.add(MenuItem("fries", "Large Fries", "Golden, crispy, and perfectly salted french fries.", 50.00, R.drawable.kfc_fries))
        menuItems.add(MenuItem("drink", "Large Drink", "Refreshing ice-cold fountain drink of your choice.", 45.00, R.drawable.kfc_drinks))
        menuItems.add(MenuItem("sundae", "KFC Sundae", "Vanilla soft serve ice cream drizzled with rich chocolate syrup.", 35.00, R.drawable.kfc_sundae))

        displayedMenuItems.addAll(menuItems)

        val lvMenu = findViewById<android.widget.ListView>(R.id.lvMenu)
        menuAdapter = MenuAdapter(this, R.layout.item_menu, displayedMenuItems, 
            onTotalChanged = {
                updateUI()
            },
            onImageClick = { item ->
                showItemDetailsDialog(item)
            }
        )
        lvMenu.adapter = menuAdapter

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val qtyChicken = menuItems.find { it.id == "chicken" }?.quantity ?: 0
            val qtyBurger = menuItems.find { it.id == "burger" }?.quantity ?: 0
            val qtyFries = menuItems.find { it.id == "fries" }?.quantity ?: 0
            val qtyDrink = menuItems.find { it.id == "drink" }?.quantity ?: 0
            val qtySundae = menuItems.find { it.id == "sundae" }?.quantity ?: 0

            if (qtyChicken == 0 && qtyBurger == 0 && qtyFries == 0 && qtyDrink == 0 && qtySundae == 0) {
                android.widget.Toast.makeText(this, "no food item selected", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val currentTotal = menuItems.sumOf { it.price * it.quantity }

            val intent = android.content.Intent(this, CartActivity::class.java)
            intent.putExtra("QTY_CHICKEN", qtyChicken)
            intent.putExtra("QTY_BURGER", qtyBurger)
            intent.putExtra("QTY_FRIES", qtyFries)
            intent.putExtra("QTY_DRINK", qtyDrink)
            intent.putExtra("QTY_SUNDAE", qtySundae)
            intent.putExtra("TOTAL_PRICE", currentTotal)
            startActivity(intent)
        }
        
        // Search Input Listener
        val etSearch = findViewById<android.widget.EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString() ?: ""
                filterMenu()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Category Click Listeners
        findViewById<android.widget.LinearLayout>(R.id.btnCategoryChicken).setOnClickListener {
            toggleCategory("chicken")
        }
        findViewById<android.widget.LinearLayout>(R.id.btnCategoryBurgers).setOnClickListener {
            toggleCategory("burger")
        }
        findViewById<android.widget.LinearLayout>(R.id.btnCategorySides).setOnClickListener {
            toggleCategory("fries")
        }
        findViewById<android.widget.LinearLayout>(R.id.btnCategoryDrinks).setOnClickListener {
            toggleCategory("drink")
        }
        findViewById<android.widget.LinearLayout>(R.id.btnCategoryDesserts).setOnClickListener {
            toggleCategory("sundae")
        }

        updateUI()
    }

    private fun toggleCategory(categoryId: String) {
        if (currentCategory == categoryId) {
            currentCategory = "All" // Deselect
        } else {
            currentCategory = categoryId
        }
        filterMenu()
    }

    private fun filterMenu() {
        displayedMenuItems.clear()
        
        val filtered = menuItems.filter { item ->
            val matchesCategory = if (currentCategory == "All") true else item.id == currentCategory
            val matchesSearch = if (currentQuery.isEmpty()) true else item.name.contains(currentQuery, ignoreCase = true) || item.description.contains(currentQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        
        displayedMenuItems.addAll(filtered)
        menuAdapter.notifyDataSetChanged()
    }

    private fun updateUI() {
        val currentTotal = menuItems.sumOf { it.price * it.quantity }
        tvTotalPrice.text = String.format(Locale.getDefault(), "Php %.2f", currentTotal)
    }

    private fun showItemDetailsDialog(item: MenuItem) {
        val intent = android.content.Intent(this, ItemDetailsActivity::class.java)
        intent.putExtra("ITEM_NAME", item.name)
        intent.putExtra("ITEM_DESC", item.description)
        intent.putExtra("ITEM_PRICE", item.price)
        intent.putExtra("ITEM_IMAGE", item.imageResId)
        startActivity(intent)
    }
}
