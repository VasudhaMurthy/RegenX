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
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await // Required for Firestore.get().await()
//
//class CollectorLocationService : Service() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private val serviceJob = SupervisorJob()
//    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
//
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    // 🌟 UNIQUE ID: This will be set to the government vehicle ID after fetching 🌟
//    private var truckDocId: String = "unknown_asset"
//    private val collectorUid = auth.currentUser?.uid ?: "unknown_user"
//
//    // Trip State
//    private var tripStartTime: Long = 0L
//    private val locationHistory = mutableListOf<Map<String, Any>>()
//
//    private val currentRouteId = "Route-A-42"
//    private val currentStatus = "En Route"
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("CollectorLocationService", "Service onCreate called")
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        tripStartTime = System.currentTimeMillis()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                for (location in result.locations) {
//                    recordLocation(location)
//                    sendLocationToFirestore(location)
//                    Log.d("CollectorLocationService", "Location received: ${location.latitude}, ${location.longitude}")
//                }
//            }
//        }
//    }
//
//    private fun recordLocation(location: Location) {
//        val loc = mapOf(
//            "latitude" to location.latitude,
//            "longitude" to location.longitude,
//            "time" to System.currentTimeMillis()
//        )
//        locationHistory.add(loc)
//    }
//
//    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
//        startForegroundServiceNotification()
//
//        // 🌟 Start fetch process before location updates 🌟
//        fetchOfficialTruckIdAndStartUpdates()
//
//        return START_STICKY
//    }
//
//    private fun fetchOfficialTruckIdAndStartUpdates() {
//        if (collectorUid == "unknown_user") {
//            Log.e("CollectorService", "User not logged in. Stopping service.")
//            stopSelf()
//            return
//        }
//
//        serviceScope.launch {
//            try {
//                // Fetch the user's profile document
//                val userDoc = firestore.collection("users").document(collectorUid).get().await()
//
//                // Assuming the government-assigned ID is stored as "governmentVehicleId"
//                val officialId = userDoc.getString("governmentVehicleId")
//
//                if (!officialId.isNullOrEmpty()) {
//                    truckDocId = officialId // Set the document ID for tracking
//                    startLocationUpdates() // Proceed with tracking
//                    Log.d("CollectorService", "Official Vehicle ID fetched: $truckDocId")
//                } else {
//                    Log.e("CollectorService", "Government Vehicle ID not found for user. Stopping service.")
//                    stopSelf()
//                }
//            } catch (e: Exception) {
//                Log.e("CollectorService", "Failed to fetch official truck ID", e)
//                stopSelf()
//            }
//        }
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
//            30_000L
//        ).setMinUpdateIntervalMillis(30_000L)
//            .setWaitForAccurateLocation(true)
//            .build()
//
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
//            "latitude" to location.latitude,
//            "longitude" to location.longitude,
//            "routeId" to currentRouteId,
//            "status" to currentStatus,
//            "startTime" to tripStartTime,
//            "collectorUid" to collectorUid, // Keep UID for linking purposes
//            "timestamp" to System.currentTimeMillis()
//        )
//
//        // 🌟 Use the official government ID as the document ID 🌟
//        firestore.collection("trucks")
//            .document(truckDocId)
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
//        serviceJob.cancel()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//        Log.d("CollectorLocationService", "Service onDestroy called. Location updates stopped.")
//
//        // 1. Update trucks document to remove active location and set status to Offline
//        val stopData = mapOf(
//            "status" to "Offline",
//            "lastUpdateTime" to System.currentTimeMillis(),
//            "latitude" to null,
//            "longitude" to null
//        )
//
//        firestore.collection("trucks")
//            .document(truckDocId)
//            .update(stopData as Map<String, Any>)
//            .addOnSuccessListener {
//                Log.d("CollectorLocationService", "✅ Truck set to Offline status.")
//            }
//            .addOnFailureListener { e ->
//                Log.e("CollectorLocationService", "❌ Failed to set Offline status.", e)
//            }
//
//        // 2. Save the detailed trip report for path tracing history
//        if (locationHistory.isNotEmpty()) {
//            val tripReport = mapOf(
//                "collectorId" to collectorUid, // Use UID for trip report indexing
//                "routeId" to currentRouteId,
//                "startTime" to tripStartTime,
//                "endTime" to System.currentTimeMillis(),
//                "path" to locationHistory,
//                "startLocation" to locationHistory.first(),
//                "endLocation" to locationHistory.last()
//            )
//
//            firestore.collection("trip_reports")
//                .add(tripReport)
//                .addOnSuccessListener {
//                    Log.d("CollectorLocationService", "✅ Trip report saved.")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("CollectorLocationService", "❌ Failed to save trip report.", e)
//                }
//        }
//    }
//
//    override fun onBind(intent: android.content.Intent?): IBinder? = null
//}









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
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.SetOptions // Needed for SetOptions.merge()
//
//class CollectorLocationService : Service() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//
//    private val collectorId = auth.currentUser?.uid ?: "unknown_collector"
//
//    // Store additional collector details once fetched
//    private var collectorDetails: Map<String, Any>? = null
//
//    override fun onCreate() {
//        super.onCreate()
//        Log.d("CollectorLocationService", "Service onCreate called for ID: $collectorId")
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // 🌟 STEP 1: Fetch additional details from 'users' collection 🌟
//        fetchCollectorDetails()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                for (location in result.locations) {
//                    sendLocationToFirestore(location) // Will use the updated structure
//                    Log.d("CollectorLocationService", "Location received: ${location.latitude}, ${location.longitude}")
//                }
//            }
//        }
//    }
//
//    private fun fetchCollectorDetails() {
//        if (collectorId == "unknown_collector") return
//
//        firestore.collection("users").document(collectorId).get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    // Store all relevant data (e.g., firstName, lastName, vehicleNo)
//                    // This data can be merged into the 'collectors' document later.
//                    collectorDetails = document.data
//                    Log.d("CollectorLocationService", "Collector details fetched successfully.")
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("CollectorLocationService", "Failed to fetch user details", e)
//            }
//    }
//
//    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
//        startForegroundServiceNotification()
//        startLocationUpdates()
//
//        // 🌟 STEP 3: Set status to 'Online' when service starts 🌟
//        updateCollectorStatus("Online")
//
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
//    /**
//     * 🌟 STEP 2: Updates Firestore using the desired structure (latitude, longitude, status)
//     * and includes relevant user details from the 'users' collection.
//     */
//    private fun sendLocationToFirestore(location: Location) {
//        val currentTime = System.currentTimeMillis()
//
//        // Prepare core location data
//        val locationData = mutableMapOf<String, Any>(
//            "collectorId" to collectorId,
//            "latitude" to location.latitude,
//            "longitude" to location.longitude,
//            "timestamp" to currentTime, // Timestamp of data creation
//            "lastUpdateTime" to currentTime, // Timestamp of last update
//            "status" to "Online" // Ensure status is 'Online' when location is updated
//        )
//
//        // Merge fetched user details (like firstName, vehicleNo) if available
//        collectorDetails?.let { details ->
//            locationData.putAll(details)
//            // Remove the 'role' field to keep the 'collectors' collection clean
//            locationData.remove("role")
//        }
//
//        // Use SetOptions.merge() to create/update the document without deleting existing fields
//        firestore.collection("collectors")
//            .document(collectorId)
//            .set(locationData, SetOptions.merge())
//            .addOnSuccessListener {
//                Log.d("CollectorLocationService", "✅ Location and details updated.")
//            }
//            .addOnFailureListener { e ->
//                Log.e("CollectorLocationService", "❌ Failed to update collector document", e)
//            }
//    }
//
//    /**
//     * Updates only the collector's status in Firestore.
//     */
//    private fun updateCollectorStatus(newStatus: String) {
//        if (collectorId == "unknown_collector") return
//
//        val statusData = mapOf(
//            "status" to newStatus,
//            "lastUpdateTime" to System.currentTimeMillis()
//        )
//
//        // Use SetOptions.merge() in case the document hasn't been created yet
//        firestore.collection("collectors")
//            .document(collectorId)
//            .set(statusData, SetOptions.merge())
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed to update status to $newStatus")
//            }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//
//        // 🌟 STEP 3: Set status to 'Offline' when service is destroyed 🌟
//        updateCollectorStatus("Offline")
//        Log.d("CollectorLocationService", "Service destroyed. Status set to Offline.")
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
import com.google.firebase.firestore.SetOptions

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

    override fun onCreate() {
        super.onCreate()

        Log.d("CollectorLocationService", "Service onCreate for ID: $collectorId")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // STEP 1: Fetch collector details from users/{uid}
        fetchCollectorDetails()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    sendLocationToFirestore(location)
                    Log.d(
                        "CollectorLocationService",
                        "Location: ${location.latitude}, ${location.longitude}"
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
                    // doc.data is nullable; cast to MutableMap<String, Any> for safer manipulation
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

        // Mark truck Online
        updateCollectorStatus("Online")

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
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            30_000 // update every 30 sec
        ).build()

        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("CollectorLocationService", "Location permission NOT granted!")
            stopSelf()
            return
        }

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
        locationData["status"] = "Online"

        // Add user details (firstName, vehicleNo, etc), remove "role"
        collectorDetails?.let { details ->
            // Clone into a HashMap to guarantee the right concrete type
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
                    "Truck updated successfully at trucks/$truckDocId"
                )
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
}
