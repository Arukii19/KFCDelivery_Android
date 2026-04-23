package com.example.kfcdelivery

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageResId: Int,
    var quantity: Int = 0
)
