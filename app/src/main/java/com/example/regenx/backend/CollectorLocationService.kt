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
// Removed unnecessary Firestore imports (Query, WriteBatch)
import com.google.firebase.firestore.SetOptions
// Removed GeofenceUtils as it is no longer needed on the client
// import com.example.regenx.util.GeofenceUtils

class CollectorLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collectorId = auth.currentUser?.uid ?: "unknown_collector"

    // Use MutableMap (HashMap) to avoid serialization/type mismatch with Firestore SDK
    private var collectorDetails: MutableMap<String, Any>? = null

    // trucks/{truckDocId}
    private var truckDocId: String = collectorId

    // ----- CONFIG (Removed client-side config) -----
    // private val GEOFENCE_RADIUS_M: Double = GeofenceUtils.GEOFENCE_RADIUS_M
    // private val NOTIFICATION_THROTTLE_MS: Long = 30 * 60 * 1000L

    override fun onCreate() {
        super.onCreate()

        Log.d("CollectorLocationService", "Service onCreate for ID: $collectorId")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // STEP 1: Fetch collector details from users/{uid}
        fetchCollectorDetails()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    // write current location to Firestore; this write is the ONLY trigger
                    sendLocationToFirestore(location)
                    Log.d(
                        "CollectorLocationService",
                        "Location callback received: ${location.latitude}, ${location.longitude}"
                    )
                }
            }
        }
    }

    private fun fetchCollectorDetails() {
        if (collectorId == "unknown_collector") return

        firestore.collection("users").document(collectorId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val data = doc.data as? MutableMap<String, Any>
                    collectorDetails = data

                    Log.d("CollectorLocationService", "Collector details fetched: $collectorDetails")

                    // Prefer using vehicleNo as truck doc ID
                    val vehicleNo = collectorDetails?.get("vehicleNo") as? String
                    truckDocId = vehicleNo?.takeIf { it.isNotBlank() } ?: collectorId

                    Log.d("CollectorLocationService", "Using truckDocId = $truckDocId")
                } else {
                    Log.e("CollectorLocationService", "No user document found for $collectorId")
                }
            }
            .addOnFailureListener {
                Log.e("CollectorLocationService", "Failed to fetch user details", it)
            }
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()
        startLocationUpdates()

        // Mark truck En Route (This is the critical status for the Cloud Function)
        updateCollectorStatus("En Route")

        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "collector_location_channel"
        val channelName = "Collector Location Tracking"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Collector Location Active")
                .setContentText("Tracking your location in real-time...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        // 1. Define the LocationRequest
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000 // update every 30 sec
        ).build()

        // 2. EXPLICITLY CHECK FOR PERMISSIONS (Required by Android Studio/Linter)
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // If permissions are NOT granted (both Fine and Coarse)
            Log.e("CollectorLocationService", "Location permission NOT granted! Stopping service.")

            stopSelf()
            return // Stop execution here
        }

        // 3. Request updates (This line is now safe because the check passed)
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )
    }

    private fun sendLocationToFirestore(location: Location) {
        val timeNow = System.currentTimeMillis()

        // Use concrete HashMap so Firestore SDK sees a serializable Map<..., Comparable & Serializable>
        val locationData = HashMap<String, Any>()
        locationData["collectorId"] = collectorId
        locationData["latitude"] = location.latitude
        locationData["longitude"] = location.longitude
        locationData["timestamp"] = timeNow
        locationData["lastUpdateTime"] = timeNow
        locationData["status"] = "En Route" // This is the trigger status

        // Add user details (firstName, vehicleNo, etc), remove "role"
        collectorDetails?.let { details ->
            val cleanDetails = HashMap(details)
            cleanDetails.remove("role")
            locationData.putAll(cleanDetails)
        }

        // Write into trucks/{truckDocId}
        firestore.collection("trucks")
            .document(truckDocId)
            .set(locationData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    "CollectorLocationService",
                    "Truck updated successfully at trucks/$truckDocId. CLOUD FUNCTION SHOULD NOW RUN."
                )
                // !!! CRITICAL CHANGE: REMOVED CALL TO checkAndNotifyResidents(location) !!!
            }
            .addOnFailureListener {
                Log.e("CollectorLocationService", "Failed updating truck document", it)
            }
    }

    private fun updateCollectorStatus(newStatus: String) {
        if (collectorId == "unknown_collector") return

        val statusData = HashMap<String, Any>()
        statusData["status"] = newStatus
        statusData["lastUpdateTime"] = System.currentTimeMillis()

        firestore.collection("trucks")
            .document(truckDocId)
            .set(statusData, SetOptions.merge())
            .addOnFailureListener {
                Log.e("CollectorLocationService", "Failed setting status=$newStatus", it)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        updateCollectorStatus("Offline")

        Log.d(
            "CollectorLocationService",
            "Service destroyed → Truck Offline for trucks/$truckDocId"
        )
    }

    override fun onBind(intent: android.content.Intent?): IBinder? = null

    // !!! CRITICAL CHANGE: ENTIRE checkAndNotifyResidents FUNCTION IS REMOVED !!!
    // All geofencing logic is now handled by the deployed Cloud Function.
}