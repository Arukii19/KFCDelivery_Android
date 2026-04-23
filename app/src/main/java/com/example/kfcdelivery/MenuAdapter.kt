package com.example.kfcdelivery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.util.Locale

class MenuAdapter(
    context: Context,
    private val resource: Int,
    private val items: List<MenuItem>,
    private val onTotalChanged: () -> Unit,
    private val onImageClick: (MenuItem) -> Unit
) : ArrayAdapter<MenuItem>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)

        val menuItem = items[position]

        val ivMenuImage = view.findViewById<ImageView>(R.id.ivMenuImage)
        val tvMenuName = view.findViewById<TextView>(R.id.tvMenuName)
        val tvMenuPrice = view.findViewById<TextView>(R.id.tvMenuPrice)
        val tvMenuQuantity = view.findViewById<TextView>(R.id.tvMenuQuantity)
        val btnMinusMenu = view.findViewById<Button>(R.id.btnMinusMenu)
        val btnAddMenu = view.findViewById<Button>(R.id.btnAddMenu)

        ivMenuImage.setImageResource(menuItem.imageResId)
        tvMenuName.text = menuItem.name
        tvMenuPrice.text = String.format(Locale.getDefault(), "Php %.2f", menuItem.price)
        tvMenuQuantity.text = menuItem.quantity.toString()

        btnAddMenu.setOnClickListener {
            menuItem.quantity++
            tvMenuQuantity.text = menuItem.quantity.toString()
            onTotalChanged()
        }

        btnMinusMenu.setOnClickListener {
            if (menuItem.quantity > 0) {
                menuItem.quantity--
                tvMenuQuantity.text = menuItem.quantity.toString()
                onTotalChanged()
            }
        }
        
        ivMenuImage.setOnClickListener {
            onImageClick(menuItem)
        }

        return view
    }
}
