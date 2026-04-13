package com.example.kfcdelivery

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class DashboardActivity : ComponentActivity() {

    private val CHICKEN_PRICE = 18.50
    private val FRIES_PRICE = 4.00
    private val DRINK_PRICE = 2.50

    private var qtyChicken = 0
    private var qtyFries = 0
    private var qtyDrink = 0
    private var currentTotal = 0.0

    private lateinit var tvQuantityChicken: TextView
    private lateinit var tvQuantityFries: TextView
    private lateinit var tvQuantityDrink: TextView
    private lateinit var tvTotalPrice: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvQuantityChicken = findViewById(R.id.tvQuantityChicken)
        tvQuantityFries = findViewById(R.id.tvQuantityFries)
        tvQuantityDrink = findViewById(R.id.tvQuantityDrink)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)

        findViewById<Button>(R.id.btnAddChicken).setOnClickListener {
            qtyChicken++
            updateUI()
        }
        findViewById<Button>(R.id.btnMinusChicken).setOnClickListener {
            if (qtyChicken > 0) qtyChicken--
            updateUI()
        }

        findViewById<Button>(R.id.btnAddFries).setOnClickListener {
            qtyFries++
            updateUI()
        }
        findViewById<Button>(R.id.btnMinusFries).setOnClickListener {
            if (qtyFries > 0) qtyFries--
            updateUI()
        }

        findViewById<Button>(R.id.btnAddDrink).setOnClickListener {
            qtyDrink++
            updateUI()
        }
        findViewById<Button>(R.id.btnMinusDrink).setOnClickListener {
            if (qtyDrink > 0) qtyDrink--
            updateUI()
        }

        findViewById<Button>(R.id.btnCheckout).setOnClickListener {
            if (qtyChicken == 0 && qtyFries == 0 && qtyDrink == 0) {
                android.widget.Toast.makeText(this, "no food item selected", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = android.content.Intent(this, CartActivity::class.java)
            intent.putExtra("QTY_CHICKEN", qtyChicken)
            intent.putExtra("QTY_FRIES", qtyFries)
            intent.putExtra("QTY_DRINK", qtyDrink)
            intent.putExtra("TOTAL_PRICE", currentTotal)
            startActivity(intent)
        }
    }

    private fun updateUI() {
        tvQuantityChicken.text = qtyChicken.toString()
        tvQuantityFries.text = qtyFries.toString()
        tvQuantityDrink.text = qtyDrink.toString()

        currentTotal = (qtyChicken * CHICKEN_PRICE) + (qtyFries * FRIES_PRICE) + (qtyDrink * DRINK_PRICE)

        tvTotalPrice.text = String.format(Locale.getDefault(), "₱ %.2f", currentTotal)
    }
}
