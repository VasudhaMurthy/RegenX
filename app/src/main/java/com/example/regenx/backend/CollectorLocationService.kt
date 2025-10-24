//package com.example.regenx.backend
//
//import android.Manifest
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.Service
//import android.content.pm.PackageManager
//import android.location.Location
//import android.os.Build
//import android.os.IBinder
//import android.util.Log
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import com.google.android.gms.location.*
//import com.google.firebase.firestore.FirebaseFirestore
//
//class CollectorLocationService : Service() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//
//    // Firestore instance
//    private val firestore = FirebaseFirestore.getInstance()
//
//    // Unique document ID in Firestore for this collector
////    private val collectorId = "truck_01"  // Adjust as needed
//    private val collectorId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_collector"
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("CollectorLocationService", "Service onCreate called")
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                for (location in result.locations) {
//                    sendLocationToFirestore(location)
//                    Log.d("CollectorLocationService", "Location received: ${location.latitude}, ${location.longitude}")
//
//                }
//            }
//        }
//    }
//
//    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
//        startForegroundServiceNotification()
//        startLocationUpdates()
//        return START_STICKY
//    }
//
//    private fun startForegroundServiceNotification() {
//        val channelId = "collector_location_channel"
//        val channelName = "Collector Location Tracking"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId, channelName, NotificationManager.IMPORTANCE_LOW
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//        val notification: Notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Collector Location Active")
//            .setContentText("Tracking your location in real time...")
//            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
//            .build()
//        startForeground(1, notification)
//    }
//
//    private fun startLocationUpdates() {
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            30_000 // every 30 seconds
//        ).build()
//
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.e("CollectorLocationService", "Location permission not granted!")
//            stopSelf()
//            return
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            mainLooper
//        )
//    }
//
//    private fun sendLocationToFirestore(location: Location) {
//        val locationData = mapOf(
//            "lat" to location.latitude,
//            "lng" to location.longitude,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        firestore.collection("collectors")
//            .document(collectorId)
//            .set(locationData)
//            .addOnSuccessListener {
//                Log.d("CollectorLocationService", "✅ Location updated in Firestore: $locationData")
//            }
//            .addOnFailureListener { e ->
//                Log.e("CollectorLocationService", "❌ Failed to update Firestore", e)
//            }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }
//
//    override fun onBind(intent: android.content.Intent?): IBinder? = null
//}





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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Required for Firestore.get().await()

class CollectorLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🌟 UNIQUE ID: This will be set to the government vehicle ID after fetching 🌟
    private var truckDocId: String = "unknown_asset"
    private val collectorUid = auth.currentUser?.uid ?: "unknown_user"

    // Trip State
    private var tripStartTime: Long = 0L
    private val locationHistory = mutableListOf<Map<String, Any>>()

    private val currentRouteId = "Route-A-42"
    private val currentStatus = "En Route"

    override fun onCreate() {
        super.onCreate()
        Log.d("CollectorLocationService", "Service onCreate called")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        tripStartTime = System.currentTimeMillis()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    recordLocation(location)
                    sendLocationToFirestore(location)
                    Log.d("CollectorLocationService", "Location received: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private fun recordLocation(location: Location) {
        val loc = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "time" to System.currentTimeMillis()
        )
        locationHistory.add(loc)
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()

        // 🌟 Start fetch process before location updates 🌟
        fetchOfficialTruckIdAndStartUpdates()

        return START_STICKY
    }

    private fun fetchOfficialTruckIdAndStartUpdates() {
        if (collectorUid == "unknown_user") {
            Log.e("CollectorService", "User not logged in. Stopping service.")
            stopSelf()
            return
        }

        serviceScope.launch {
            try {
                // Fetch the user's profile document
                val userDoc = firestore.collection("users").document(collectorUid).get().await()

                // Assuming the government-assigned ID is stored as "governmentVehicleId"
                val officialId = userDoc.getString("governmentVehicleId")

                if (!officialId.isNullOrEmpty()) {
                    truckDocId = officialId // Set the document ID for tracking
                    startLocationUpdates() // Proceed with tracking
                    Log.d("CollectorService", "Official Vehicle ID fetched: $truckDocId")
                } else {
                    Log.e("CollectorService", "Government Vehicle ID not found for user. Stopping service.")
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e("CollectorService", "Failed to fetch official truck ID", e)
                stopSelf()
            }
        }
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
            30_000L
        ).setMinUpdateIntervalMillis(30_000L)
            .setWaitForAccurateLocation(true)
            .build()


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
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "routeId" to currentRouteId,
            "status" to currentStatus,
            "startTime" to tripStartTime,
            "collectorUid" to collectorUid, // Keep UID for linking purposes
            "timestamp" to System.currentTimeMillis()
        )

        // 🌟 Use the official government ID as the document ID 🌟
        firestore.collection("trucks")
            .document(truckDocId)
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
        serviceJob.cancel()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("CollectorLocationService", "Service onDestroy called. Location updates stopped.")

        // 1. Update trucks document to remove active location and set status to Offline
        val stopData = mapOf(
            "status" to "Offline",
            "lastUpdateTime" to System.currentTimeMillis(),
            "latitude" to null,
            "longitude" to null
        )

        firestore.collection("trucks")
            .document(truckDocId)
            .update(stopData as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("CollectorLocationService", "✅ Truck set to Offline status.")
            }
            .addOnFailureListener { e ->
                Log.e("CollectorLocationService", "❌ Failed to set Offline status.", e)
            }

        // 2. Save the detailed trip report for path tracing history
        if (locationHistory.isNotEmpty()) {
            val tripReport = mapOf(
                "collectorId" to collectorUid, // Use UID for trip report indexing
                "routeId" to currentRouteId,
                "startTime" to tripStartTime,
                "endTime" to System.currentTimeMillis(),
                "path" to locationHistory,
                "startLocation" to locationHistory.first(),
                "endLocation" to locationHistory.last()
            )

            firestore.collection("trip_reports")
                .add(tripReport)
                .addOnSuccessListener {
                    Log.d("CollectorLocationService", "✅ Trip report saved.")
                }
                .addOnFailureListener { e ->
                    Log.e("CollectorLocationService", "❌ Failed to save trip report.", e)
                }
        }
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null
}