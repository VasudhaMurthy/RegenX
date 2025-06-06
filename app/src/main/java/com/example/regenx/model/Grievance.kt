package com.example.regenx.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Grievance(
    val id: String = "",
    val type: String = "", // "resident" or "collector"
    val userId: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val status: String = "PENDING",
    val location: GeoPoint? = null
)
