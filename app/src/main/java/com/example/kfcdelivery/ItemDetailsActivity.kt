package com.example.kfcdelivery

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale

class ItemDetailsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        // Retrieve data from Intent
        val itemName = intent.getStringExtra("ITEM_NAME") ?: "Unknown Item"
        val itemDesc = intent.getStringExtra("ITEM_DESC") ?: "No description available."
        val itemPrice = intent.getDoubleExtra("ITEM_PRICE", 0.0)
        val itemImageRes = intent.getIntExtra("ITEM_IMAGE", R.drawable.kfc_logo)

        // Bind data to UI views
        val ivDetailImage = findViewById<ImageView>(R.id.ivDetailImage)
        val tvDetailName = findViewById<TextView>(R.id.tvDetailName)
        val tvDetailPrice = findViewById<TextView>(R.id.tvDetailPrice)
        val tvDetailDescription = findViewById<TextView>(R.id.tvDetailDescription)

        ivDetailImage.setImageResource(itemImageRes)
        tvDetailName.text = itemName
        tvDetailPrice.text = String.format(Locale.getDefault(), "Php %.2f", itemPrice)
        tvDetailDescription.text = itemDesc

        // Setup click listeners
        findViewById<ImageView>(R.id.btnBackFromDetails).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnBackToDashboard).setOnClickListener {
            finish()
        }
    }
}
