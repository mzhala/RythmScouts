package com.example.rythmscouts.models

data class NotificationItem(
    val title: String = "",
    val subtitle: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
