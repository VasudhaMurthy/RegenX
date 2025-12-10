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
//import com.google.firebase.firestore.SetOptions
//
//class CollectorLocationService : Service() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//    private val collectorId = auth.currentUser?.uid ?: "unknown_collector"
//
//    // Use MutableMap (HashMap) to avoid serialization/type mismatch with Firestore SDK
//    private var collectorDetails: MutableMap<String, Any>? = null
//
//    // trucks/{truckDocId}
//    private var truckDocId: String = collectorId
//
//    override fun onCreate() {
//        super.onCreate()
//
//        Log.d("CollectorLocationService", "Service onCreate for ID: $collectorId")
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // STEP 1: Fetch collector details from users/{uid}
//        fetchCollectorDetails()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                for (location in result.locations) {
//                    sendLocationToFirestore(location)
//                    Log.d(
//                        "CollectorLocationService",
//                        "Location: ${location.latitude}, ${location.longitude}"
//                    )
//                }
//            }
//        }
//    }
//
//    private fun fetchCollectorDetails() {
//        if (collectorId == "unknown_collector") return
//
//        firestore.collection("users").document(collectorId).get()
//            .addOnSuccessListener { doc ->
//                if (doc.exists()) {
//                    // doc.data is nullable; cast to MutableMap<String, Any> for safer manipulation
//                    @Suppress("UNCHECKED_CAST")
//                    val data = doc.data as? MutableMap<String, Any>
//                    collectorDetails = data
//
//                    Log.d("CollectorLocationService", "Collector details fetched: $collectorDetails")
//
//                    // Prefer using vehicleNo as truck doc ID
//                    val vehicleNo = collectorDetails?.get("vehicleNo") as? String
//                    truckDocId = vehicleNo?.takeIf { it.isNotBlank() } ?: collectorId
//
//                    Log.d("CollectorLocationService", "Using truckDocId = $truckDocId")
//                } else {
//                    Log.e("CollectorLocationService", "No user document found for $collectorId")
//                }
//            }
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed to fetch user details", it)
//            }
//    }
//
//    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
//        startForegroundServiceNotification()
//        startLocationUpdates()
//
//        // Mark truck Online
//        updateCollectorStatus("Online")
//
//        return START_STICKY
//    }
//
//    private fun startForegroundServiceNotification() {
//        val channelId = "collector_location_channel"
//        val channelName = "Collector Location Tracking"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                channelName,
//                NotificationManager.IMPORTANCE_LOW
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//        val notification: Notification =
//            NotificationCompat.Builder(this, channelId)
//                .setContentTitle("Collector Location Active")
//                .setContentText("Tracking your location in real-time...")
//                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
//                .build()
//
//        startForeground(1, notification)
//    }
//
//    private fun startLocationUpdates() {
//        val request = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            30_000 // update every 30 sec
//        ).build()
//
//        if (
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED ||
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.e("CollectorLocationService", "Location permission NOT granted!")
//            stopSelf()
//            return
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            request,
//            locationCallback,
//            mainLooper
//        )
//    }
//
//    private fun sendLocationToFirestore(location: Location) {
//        val timeNow = System.currentTimeMillis()
//
//        // Use concrete HashMap so Firestore SDK sees a serializable Map<..., Comparable & Serializable>
//        val locationData = HashMap<String, Any>()
//        locationData["collectorId"] = collectorId
//        locationData["latitude"] = location.latitude
//        locationData["longitude"] = location.longitude
//        locationData["timestamp"] = timeNow
//        locationData["lastUpdateTime"] = timeNow
//        locationData["status"] = "Online"
//
//        // Add user details (firstName, vehicleNo, etc), remove "role"
//        collectorDetails?.let { details ->
//            // Clone into a HashMap to guarantee the right concrete type
//            val cleanDetails = HashMap(details)
//            cleanDetails.remove("role")
//            locationData.putAll(cleanDetails)
//        }
//
//        // Write into trucks/{truckDocId}
//        firestore.collection("trucks")
//            .document(truckDocId)
//            .set(locationData, SetOptions.merge())
//            .addOnSuccessListener {
//                Log.d(
//                    "CollectorLocationService",
//                    "Truck updated successfully at trucks/$truckDocId"
//                )
//            }
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed updating truck document", it)
//            }
//    }
//
//    private fun updateCollectorStatus(newStatus: String) {
//        if (collectorId == "unknown_collector") return
//
//        val statusData = HashMap<String, Any>()
//        statusData["status"] = newStatus
//        statusData["lastUpdateTime"] = System.currentTimeMillis()
//
//        firestore.collection("trucks")
//            .document(truckDocId)
//            .set(statusData, SetOptions.merge())
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed setting status=$newStatus", it)
//            }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//
//        updateCollectorStatus("Offline")
//
//        Log.d(
//            "CollectorLocationService",
//            "Service destroyed → Truck Offline for trucks/$truckDocId"
//        )
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
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.SetOptions
//import com.google.firebase.firestore.WriteBatch
//import com.example.regenx.util.GeofenceUtils
//
//class CollectorLocationService : Service() {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//
//    private val firestore = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//    private val collectorId = auth.currentUser?.uid ?: "unknown_collector"
//
//    // Use MutableMap (HashMap) to avoid serialization/type mismatch with Firestore SDK
//    private var collectorDetails: MutableMap<String, Any>? = null
//
//    // trucks/{truckDocId}
//    private var truckDocId: String = collectorId
//
//    // ----- CONFIG -----
//    private val GEOFENCE_RADIUS_M: Double = GeofenceUtils.GEOFENCE_RADIUS_M // keep single source of truth
//    private val NOTIFICATION_THROTTLE_MS: Long = 30 * 60 * 1000L // 30 minutes throttle
//
//    override fun onCreate() {
//        super.onCreate()
//
//        Log.d("CollectorLocationService", "Service onCreate for ID: $collectorId")
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // STEP 1: Fetch collector details from users/{uid}
//        fetchCollectorDetails()
//
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(result: LocationResult) {
//                for (location in result.locations) {
//                    // write current location to Firestore; notification logic will be triggered after successful write
//                    sendLocationToFirestore(location)
//                    Log.d(
//                        "CollectorLocationService",
//                        "Location callback received: ${location.latitude}, ${location.longitude}"
//                    )
//                }
//            }
//        }
//    }
//
//    private fun fetchCollectorDetails() {
//        if (collectorId == "unknown_collector") return
//
//        firestore.collection("users").document(collectorId).get()
//            .addOnSuccessListener { doc ->
//                if (doc.exists()) {
//                    @Suppress("UNCHECKED_CAST")
//                    val data = doc.data as? MutableMap<String, Any>
//                    collectorDetails = data
//
//                    Log.d("CollectorLocationService", "Collector details fetched: $collectorDetails")
//
//                    // Prefer using vehicleNo as truck doc ID
//                    val vehicleNo = collectorDetails?.get("vehicleNo") as? String
//                    truckDocId = vehicleNo?.takeIf { it.isNotBlank() } ?: collectorId
//
//                    Log.d("CollectorLocationService", "Using truckDocId = $truckDocId")
//                } else {
//                    Log.e("CollectorLocationService", "No user document found for $collectorId")
//                }
//            }
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed to fetch user details", it)
//            }
//    }
//
//    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
//        startForegroundServiceNotification()
//        startLocationUpdates()
//
//        // Mark truck Online
//        updateCollectorStatus("En Route")
//
//        return START_STICKY
//    }
//
//    private fun startForegroundServiceNotification() {
//        val channelId = "collector_location_channel"
//        val channelName = "Collector Location Tracking"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                channelName,
//                NotificationManager.IMPORTANCE_LOW
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//        val notification: Notification =
//            NotificationCompat.Builder(this, channelId)
//                .setContentTitle("Collector Location Active")
//                .setContentText("Tracking your location in real-time...")
//                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
//                .build()
//
//        startForeground(1, notification)
//    }
//
//    private fun startLocationUpdates() {
//        // 1. Define the LocationRequest
//        val request = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            30_000 // update every 30 sec
//        ).build()
//
//        // 2. EXPLICITLY CHECK FOR PERMISSIONS (Required by Android Studio/Linter)
//        if (
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED &&
//            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            // If permissions are NOT granted (both Fine and Coarse)
//            Log.e("CollectorLocationService", "Location permission NOT granted! Stopping service.")
//
//            // It is recommended to send a status update or log here
//            // If your service was started because a user initiated it, stopping is usually the correct fallback.
//            stopSelf()
//            return // Stop execution here
//        }
//
//        // 3. Request updates (This line is now safe because the check passed)
//        fusedLocationClient.requestLocationUpdates(
//            request,
//            locationCallback,
//            mainLooper
//        )
//    }
//
//    private fun sendLocationToFirestore(location: Location) {
//        val timeNow = System.currentTimeMillis()
//
//        // Use concrete HashMap so Firestore SDK sees a serializable Map<..., Comparable & Serializable>
//        val locationData = HashMap<String, Any>()
//        locationData["collectorId"] = collectorId
//        locationData["latitude"] = location.latitude
//        locationData["longitude"] = location.longitude
//        locationData["timestamp"] = timeNow
//        locationData["lastUpdateTime"] = timeNow
//        locationData["status"] = "En Route"
//
//        // Add user details (firstName, vehicleNo, etc), remove "role"
//        collectorDetails?.let { details ->
//            val cleanDetails = HashMap(details)
//            cleanDetails.remove("role")
//            locationData.putAll(cleanDetails)
//        }
//
//        // Write into trucks/{truckDocId}
//        firestore.collection("trucks")
//            .document(truckDocId)
//            .set(locationData, SetOptions.merge())
//            .addOnSuccessListener {
//                Log.d(
//                    "CollectorLocationService",
//                    "Truck updated successfully at trucks/$truckDocId"
//                )
//                // Only after successful write, run geofence check & notifications
//                try {
//                    checkAndNotifyResidents(location)
//                } catch (ex: Exception) {
//                    Log.e("CollectorLocationService", "Error while checking & notifying residents", ex)
//                }
//            }
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed updating truck document", it)
//            }
//    }
//
//    private fun updateCollectorStatus(newStatus: String) {
//        if (collectorId == "unknown_collector") return
//
//        val statusData = HashMap<String, Any>()
//        statusData["status"] = newStatus
//        statusData["lastUpdateTime"] = System.currentTimeMillis()
//
//        firestore.collection("trucks")
//            .document(truckDocId)
//            .set(statusData, SetOptions.merge())
//            .addOnFailureListener {
//                Log.e("CollectorLocationService", "Failed setting status=$newStatus", it)
//            }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//
//        updateCollectorStatus("Offline")
//
//        Log.d(
//            "CollectorLocationService",
//            "Service destroyed → Truck Offline for trucks/$truckDocId"
//        )
//    }
//
//    override fun onBind(intent: android.content.Intent?): IBinder? = null
//
//    // ----------------- Geofencing & Resident Notification Methods -----------------
//
//    /**
//     * Query residents in a latitude-range around the truck location, then filter
//     * by longitude & Haversine distance (GeofenceUtils.haversineDistanceMeters).
//     *
//     * Writes batched alerts to residents/{residentId}/alerts and sets a per-truck
//     * last-notified timestamp on the resident doc to throttle re-notification.
//     */
//    private fun checkAndNotifyResidents(location: Location) {
//        try {
//            val lat = location.latitude
//            val lng = location.longitude
//            val radiusMeters = GEOFENCE_RADIUS_M
//            val now = System.currentTimeMillis()
//
//            // Build a simple bounding box (minLat, maxLat, minLng, maxLng)
//            val box = GeofenceUtils.boundingBox(lat, lng, radiusMeters)
//
//            // IMPORTANT: Firestore supports range queries only on a single field without composite index complications.
//            // We'll query by latitude range (narrow the candidate set), then filter longitude & exact distance client-side.
//            firestore.collection("residents")
//                .whereGreaterThanOrEqualTo("latitude", box.minLat)
//                .whereLessThanOrEqualTo("latitude", box.maxLat)
//                .get()
//                .addOnSuccessListener { snap ->
//                    if (snap == null || snap.isEmpty) {
//                        Log.d("CollectorLocationService", "No residents found in latitude range")
//                        return@addOnSuccessListener
//                    }
//
//                    val batch: WriteBatch = firestore.batch()
//                    var writesCount = 0
//
//                    for (doc in snap.documents) {
//                        val residentId = doc.id
//                        val rLat = doc.getDouble("latitude")
//                        val rLng = doc.getDouble("longitude")
//
//                        if (rLat == null || rLng == null) continue
//
//                        // quick longitude filter
//                        if (rLng < box.minLng || rLng > box.maxLng) continue
//
//                        // compute precise distance using your GeofenceUtils haversine function
//                        val distanceMeters = try {
//                            GeofenceUtils.haversineDistanceMeters(lat, lng, rLat, rLng)
//                        } catch (e: Exception) {
//                            // fallback to Android distanceBetween
//                            val tmp = FloatArray(1)
//                            android.location.Location.distanceBetween(lat, lng, rLat, rLng, tmp)
//                            tmp[0].toDouble()
//                        }
//
//                        if (distanceMeters <= radiusMeters) {
//                            // Throttle check using resident doc field `lastNotified_{truckDocId}`
//                            val lastNotifiedField = "lastNotified_$truckDocId"
//                            val lastNotifiedVal = (doc.get(lastNotifiedField) as? Number)?.toLong()
//                            if (lastNotifiedVal != null && (now - lastNotifiedVal) < NOTIFICATION_THROTTLE_MS) {
//                                // skip notifying this resident for this truck (recently notified)
//                                Log.d("CollectorLocationService", "Resident $residentId skipped due to throttle (lastNotified=$lastNotifiedVal)")
//                                continue
//                            }
//
//                            // Prepare alert doc
//                            val alert = HashMap<String, Any>()
//                            alert["truckId"] = truckDocId
//                            alert["collectorId"] = collectorId
//                            alert["truckLat"] = lat
//                            alert["truckLng"] = lng
//                            alert["distanceMeters"] = distanceMeters
//                            alert["message"] = "Garbage truck is nearby."
//                            alert["timestamp"] = now
//                            alert["seen"] = false
//
//                            val alertRef = firestore.collection("residents").document(residentId)
//                                .collection("alerts").document()
//
//                            batch.set(alertRef, alert)
//
//                            // update resident doc with last-notified timestamp for this truck
//                            val residentDocRef = firestore.collection("residents").document(residentId)
//                            val updateMap = hashMapOf<String, Any>(
//                                lastNotifiedField to now,
//                                "lastNotifiedAt" to now
//                            )
//                            batch.set(residentDocRef, updateMap, SetOptions.merge())
//
//                            writesCount++
//                        }
//                    }
//
//                    if (writesCount == 0) {
//                        Log.d("CollectorLocationService", "No residents within geofence radius to notify")
//                        return@addOnSuccessListener
//                    }
//
//                    // commit batch
//                    batch.commit()
//                        .addOnSuccessListener {
//                            Log.d("CollectorLocationService", "Wrote $writesCount alert(s) to residents' alerts collections")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("CollectorLocationService", "Failed committing alert batch", e)
//                        }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("CollectorLocationService", "Failed querying residents by latitude range", e)
//                }
//        } catch (ex: Exception) {
//            Log.e("CollectorLocationService", "checkAndNotifyResidents failed", ex)
//        }
//    }
//    // --------------------------------------------------------------------------------------
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