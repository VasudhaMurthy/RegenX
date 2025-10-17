package com.example.regenx.models

data class Complaint(
    val id: String = "",
    val userId: String = "",
    val userType: String = "",
    val subject: String = "",
    val description: String = "",
    val photoUrl: String = "",
    val timestamp: Long = 0L,
    val status: String = "pending"
)
