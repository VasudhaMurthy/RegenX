package com.example.regenx.backend

import android.util.Log
import com.google.firebase.database.*

class FirebaseService {

    private val database = FirebaseDatabase.getInstance()

    /**
     * Listen to real-time location updates of a collector from Realtime Database
     * @param collectorId The collector's unique ID in the database (e.g., "truck_01")
     * @param onLocationUpdate Callback with latitude & longitude
     */
    fun listenToCollectorLocation(
        collectorId: String,
        onLocationUpdate: (Double, Double) -> Unit
    ) {
        val ref = database.getReference("collectors/$collectorId/location")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.w("FirebaseService", "No location data found for collector: $collectorId")
                    return
                }

                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)

                if (lat != null && lng != null) {
                    Log.d("FirebaseService", "🔄 Location update: $lat, $lng")
                    onLocationUpdate(lat, lng)
                } else {
                    Log.w("FirebaseService", "Latitude or longitude missing for collector: $collectorId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseService", "❌ Failed to listen to location: ${error.message}")
            }
        })
    }
}
