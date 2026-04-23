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

        var qtyChicken = intent.getIntExtra("QTY_CHICKEN", 0)
        var qtyFries = intent.getIntExtra("QTY_FRIES", 0)
        var qtyDrink = intent.getIntExtra("QTY_DRINK", 0)

        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)
        val rgPaymentMethod = findViewById<android.widget.RadioGroup>(R.id.rgPaymentMethod)
        val tvFinalTotal = findViewById<TextView>(R.id.tvFinalTotal)
        val etLocation = findViewById<android.widget.EditText>(R.id.etLocation)

        val rowChicken = findViewById<android.widget.LinearLayout>(R.id.rowChicken)
        val rowFries = findViewById<android.widget.LinearLayout>(R.id.rowFries)
        val rowDrink = findViewById<android.widget.LinearLayout>(R.id.rowDrink)
        val tvEmptyCart = findViewById<TextView>(R.id.tvEmptyCart)

        val tvCartQtyChicken = findViewById<TextView>(R.id.tvCartQtyChicken)
        val tvCartQtyFries = findViewById<TextView>(R.id.tvCartQtyFries)
        val tvCartQtyDrink = findViewById<TextView>(R.id.tvCartQtyDrink)

        val btnAddCartChicken = findViewById<Button>(R.id.btnAddCartChicken)
        val btnMinusCartChicken = findViewById<Button>(R.id.btnMinusCartChicken)
        val btnAddCartFries = findViewById<Button>(R.id.btnAddCartFries)
        val btnMinusCartFries = findViewById<Button>(R.id.btnMinusCartFries)
        val btnAddCartDrink = findViewById<Button>(R.id.btnAddCartDrink)
        val btnMinusCartDrink = findViewById<Button>(R.id.btnMinusCartDrink)

        fun updateCartUI() {
            val currentTotal = (qtyChicken * 85.00) + (qtyFries * 50.00) + (qtyDrink * 45.00)
            tvFinalTotal.text = String.format(Locale.getDefault(), "₱ %.2f", currentTotal)

            if (qtyChicken > 0) {
                rowChicken.visibility = android.view.View.VISIBLE
                tvCartQtyChicken.text = qtyChicken.toString()
            } else {
                rowChicken.visibility = android.view.View.GONE
            }

            if (qtyFries > 0) {
                rowFries.visibility = android.view.View.VISIBLE
                tvCartQtyFries.text = qtyFries.toString()
            } else {
                rowFries.visibility = android.view.View.GONE
            }

            if (qtyDrink > 0) {
                rowDrink.visibility = android.view.View.VISIBLE
                tvCartQtyDrink.text = qtyDrink.toString()
            } else {
                rowDrink.visibility = android.view.View.GONE
            }

            if (qtyChicken == 0 && qtyFries == 0 && qtyDrink == 0) {
                tvEmptyCart.visibility = android.view.View.VISIBLE
            } else {
                tvEmptyCart.visibility = android.view.View.GONE
            }
        }

        btnAddCartChicken.setOnClickListener { qtyChicken++; updateCartUI() }
        btnMinusCartChicken.setOnClickListener { if (qtyChicken > 0) qtyChicken--; updateCartUI() }
        btnAddCartFries.setOnClickListener { qtyFries++; updateCartUI() }
        btnMinusCartFries.setOnClickListener { if (qtyFries > 0) qtyFries--; updateCartUI() }
        btnAddCartDrink.setOnClickListener { qtyDrink++; updateCartUI() }
        btnMinusCartDrink.setOnClickListener { if (qtyDrink > 0) qtyDrink--; updateCartUI() }

        btnConfirmOrder.setOnClickListener {
            if (qtyChicken == 0 && qtyFries == 0 && qtyDrink == 0) {
                android.widget.Toast.makeText(this, "Your cart is empty", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (etLocation.text.toString().trim().isEmpty()) {
                android.widget.Toast.makeText(this, "No address detected", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val paymentMethod = if (rgPaymentMethod.checkedRadioButtonId == R.id.rbOnline) {
                "Online Payment"
            } else {
                "Cash on Delivery (COD)"
            }
            val statusIntent = Intent(this, OrderStatusActivity::class.java)
            val finalTotal = (qtyChicken * 85.00) + (qtyFries * 50.00) + (qtyDrink * 45.00)
            statusIntent.putExtra("FINAL_TOTAL", finalTotal)
            statusIntent.putExtra("PAYMENT_METHOD", paymentMethod)
            startActivity(statusIntent)
            finish()
        }

        updateCartUI()
    }
}
