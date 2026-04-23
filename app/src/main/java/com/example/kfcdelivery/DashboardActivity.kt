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
        menuItems.add(MenuItem("fries", "Large Fries", "Golden, crispy, and perfectly salted french fries.", 50.00, R.drawable.kfc_fries))
        menuItems.add(MenuItem("drink", "Large Drink", "Refreshing ice-cold fountain drink of your choice.", 45.00, R.drawable.kfc_drinks))

        val lvMenu = findViewById<android.widget.ListView>(R.id.lvMenu)
        menuAdapter = MenuAdapter(this, R.layout.item_menu, menuItems, 
            onTotalChanged = {
                updateUI()
            },
            onImageClick = { item ->
                showItemDetailsDialog(item.name, item.description)
            }
        )
        lvMenu.adapter = menuAdapter

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            val qtyChicken = menuItems.find { it.id == "chicken" }?.quantity ?: 0
            val qtyFries = menuItems.find { it.id == "fries" }?.quantity ?: 0
            val qtyDrink = menuItems.find { it.id == "drink" }?.quantity ?: 0

            if (qtyChicken == 0 && qtyFries == 0 && qtyDrink == 0) {
                android.widget.Toast.makeText(this, "no food item selected", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val currentTotal = menuItems.sumOf { it.price * it.quantity }

            val intent = android.content.Intent(this, CartActivity::class.java)
            intent.putExtra("QTY_CHICKEN", qtyChicken)
            intent.putExtra("QTY_FRIES", qtyFries)
            intent.putExtra("QTY_DRINK", qtyDrink)
            intent.putExtra("TOTAL_PRICE", currentTotal)
            startActivity(intent)
        }
        
        updateUI()
    }

    private fun updateUI() {
        val currentTotal = menuItems.sumOf { it.price * it.quantity }
        tvTotalPrice.text = String.format(Locale.getDefault(), "Php %.2f", currentTotal)
    }

    private fun showItemDetailsDialog(title: String, description: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
