package com.example.regenx.backend

import com.google.firebase.database.*

class FirebaseService {

    private val database = FirebaseDatabase.getInstance()

    /**
     * Listen to real-time location updates of a collector
     * @param collectorId ID of the collector in Firebase
     * @param onLocationUpdate Callback with latitude & longitude
     */
    fun listenToCollectorLocation(
        collectorId: String,
        onLocationUpdate: (Double, Double) -> Unit
    ) {
        val ref = database.getReference("collectors/$collectorId/location")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)
                if (lat != null && lng != null) {
                    onLocationUpdate(lat, lng)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log or handle errors
            }
        })
    }
}
