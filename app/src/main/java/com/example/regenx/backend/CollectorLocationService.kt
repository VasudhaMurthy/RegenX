package com.example.regenx.backend

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore

class CollectorLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Firestore instance
    private val firestore = FirebaseFirestore.getInstance()

    // Unique document ID in Firestore for this collector
    private val collectorId = "truck_01"  // Adjust as needed

    override fun onCreate() {
        super.onCreate()
        Log.d("CollectorLocationService", "Service onCreate called")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    sendLocationToFirestore(location)
                    Log.d("CollectorLocationService", "Location received: ${location.latitude}, ${location.longitude}")

                }
            }
        }
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        startLocationUpdates()
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "collector_location_channel"
        val channelName = "Collector Location Tracking"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Collector Location Active")
            .setContentText("Tracking your location in real time...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000 // every 30 seconds
        ).build()

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("CollectorLocationService", "Location permission not granted!")
            stopSelf()
            return
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )
    }

    private fun sendLocationToFirestore(location: Location) {
        val locationData = mapOf(
            "lat" to location.latitude,
            "lng" to location.longitude,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("collectors")
            .document(collectorId)
            .set(locationData)
            .addOnSuccessListener {
                Log.d("CollectorLocationService", "✅ Location updated in Firestore: $locationData")
            }
            .addOnFailureListener { e ->
                Log.e("CollectorLocationService", "❌ Failed to update Firestore", e)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null
}
