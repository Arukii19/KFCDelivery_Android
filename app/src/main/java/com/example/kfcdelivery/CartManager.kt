package com.example.kfcdelivery

object CartManager {
    val items = mutableListOf<CartItem>()
    var selectedBranchId: String = ""
    var selectedBranchName: String = ""

    fun addItem(menuItem: MenuItem) {
        val existing = items.find { it.menuItemId == menuItem.id }
        if (existing != null) {
            existing.quantity++
        } else {
            items.add(
                CartItem(
                    menuItemId = menuItem.id,
                    name = menuItem.name,
                    price = menuItem.price,
                    imageUrl = menuItem.imageUrl,
                    quantity = 1
                )
            )
        }
    }

    fun removeItem(menuItemId: String) {
        items.removeAll { it.menuItemId == menuItemId }
    }

    fun changeQuantity(menuItemId: String, delta: Int) {
        val item = items.find { it.menuItemId == menuItemId } ?: return
        item.quantity += delta
        if (item.quantity <= 0) removeItem(menuItemId)
    }

    fun getTotal(): Double = items.sumOf { it.price * it.quantity }

    fun getTotalQuantity(): Int = items.sumOf { it.quantity }

    fun clear() {
        items.clear()
    }
}

data class CartItem(
    val menuItemId: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    var quantity: Int
)
