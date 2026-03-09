package com.example.sneakersapp

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val totalAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)