package com.example.kfcdelivery

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val imageResId: Int = 0,           // kept for legacy compatibility
    val isAvailable: Boolean = true,
    var quantity: Int = 0
)
