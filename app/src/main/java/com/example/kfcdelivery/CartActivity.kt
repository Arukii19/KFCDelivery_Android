package com.example.kfcdelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)
        btnConfirmOrder.setOnClickListener {
            val intent = Intent(this, OrderStatusActivity::class.java)
            startActivity(intent)
            finish()
        }

        val qtyChicken = intent.getIntExtra("QTY_CHICKEN", 0)
        val qtyFries = intent.getIntExtra("QTY_FRIES", 0)
        val qtyDrink = intent.getIntExtra("QTY_DRINK", 0)
        val totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)

        val itemsBuilder = StringBuilder()
        val pricesBuilder = StringBuilder()

        if (qtyChicken > 0) {
            itemsBuilder.append("${qtyChicken}x Chicken\n")
            pricesBuilder.append(String.format(Locale.getDefault(), "₱ %.2f\n", qtyChicken * 18.50))
        }
        if (qtyFries > 0) {
            itemsBuilder.append("${qtyFries}x French Fries\n")
            pricesBuilder.append(String.format(Locale.getDefault(), "₱ %.2f\n", qtyFries * 4.00))
        }
        if (qtyDrink > 0) {
            itemsBuilder.append("${qtyDrink}x Soft Drink\n")
            pricesBuilder.append(String.format(Locale.getDefault(), "₱ %.2f\n", qtyDrink * 2.50))
        }

        val tvOrderItems = findViewById<TextView>(R.id.tvOrderItems)
        val tvOrderPrices = findViewById<TextView>(R.id.tvOrderPrices)
        val tvFinalTotal = findViewById<TextView>(R.id.tvFinalTotal)

        if (itemsBuilder.isEmpty()) {
            tvOrderItems.text = "No items in cart"
            tvOrderPrices.text = ""
        } else {
            // trim() removes the trailing newline
            tvOrderItems.text = itemsBuilder.toString().trim()
            tvOrderPrices.text = pricesBuilder.toString().trim()
        }

        tvFinalTotal.text = String.format(Locale.getDefault(), "₱ %.2f", totalPrice)
    }
}
