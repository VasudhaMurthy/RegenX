package com.example.regenx.resident.geofence

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GeofenceService : Service() {

    private lateinit var dbRef: DatabaseReference
    private val residentLat = 12.9716   // Replace with stored resident location
    private val residentLng = 77.5946

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dbRef = FirebaseDatabase.getInstance().getReference("truck_location")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java) ?: return
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: return

                val distance = calculateDistance(residentLat, residentLng, lat, lng)
                if (distance <= 0.2) { // 0.2 km = 200 meters
                    GeofenceHelper.sendNotification(
                        this@GeofenceService,
                        "Truck is Near!",
                        "Please put your garbage out"
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return START_STICKY
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}