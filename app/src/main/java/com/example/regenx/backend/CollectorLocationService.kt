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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.example.regenx.util.GeofenceUtils

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
                    // ----- ADDED: check residents within geofence and notify them -----
                    checkAndNotifyResidents(location)
                    // ------------------------------------------------------------------
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

    // ----------------- ADDED: Geofencing & Resident Notification Methods -----------------

    /**
     * Query residents in a bounding box around the truck location, then filter
     * by Haversine distance (GeofenceUtils.haversineDistanceMeters) and
     * write alerts into residents/{residentId}/alerts if within radius.
     *
     * Assumes resident documents contain numeric fields: "latitude", "longitude", optional "address".
     */
    private fun checkAndNotifyResidents(location: Location) {
        try {
            val lat = location.latitude
            val lng = location.longitude
            val radius = GeofenceUtils.GEOFENCE_RADIUS_M

            val box = GeofenceUtils.boundingBox(lat, lng, radius)

            // Bounding-box query to limit reads
            firestore.collection("residents")
                .whereGreaterThanOrEqualTo("latitude", box.minLat)
                .whereLessThanOrEqualTo("latitude", box.maxLat)
                .whereGreaterThanOrEqualTo("longitude", box.minLng)
                .whereLessThanOrEqualTo("longitude", box.maxLng)
                .orderBy("latitude", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener { snap ->
                    if (snap.isEmpty) {
                        Log.d("CollectorLocationService", "No residents in bounding box")
                        return@addOnSuccessListener
                    }

                    for (doc in snap.documents) {
                        val residentId = doc.id
                        val rLat = doc.getDouble("latitude")
                        val rLng = doc.getDouble("longitude")
                        val address = doc.getString("address")

                        if (rLat == null || rLng == null) continue

                        val distance = GeofenceUtils.haversineDistanceMeters(lat, lng, rLat, rLng)
                        if (distance <= radius) {
                            Log.d("CollectorLocationService", "Resident $residentId is within radius: $distance m")
                            sendAlertToResident(
                                residentId = residentId,
                                distance = distance,
                                truckLat = lat,
                                truckLng = lng,
                                address = address ?: ""
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CollectorLocationService", "Failed querying residents", e)
                }
        } catch (ex: Exception) {
            Log.e("CollectorLocationService", "checkAndNotifyResidents failed", ex)
        }
    }

    /**
     * Writes an alert document under residents/{residentId}/alerts with truck info.
     * Fields: truckId, collectorId, truckLat, truckLng, distanceMeters, message, address, timestamp, seen
     */
    private fun sendAlertToResident(
        residentId: String,
        distance: Double,
        truckLat: Double,
        truckLng: Double,
        address: String
    ) {
        try {
            val alert = HashMap<String, Any>()
            alert["truckId"] = truckDocId
            alert["collectorId"] = collectorId
            alert["truckLat"] = truckLat
            alert["truckLng"] = truckLng
            alert["distanceMeters"] = distance
            alert["message"] = "Garbage truck has arrived nearby."
            alert["address"] = address
            alert["timestamp"] = System.currentTimeMillis()
            alert["seen"] = false

            firestore.collection("residents")
                .document(residentId)
                .collection("alerts")
                .add(alert)
                .addOnSuccessListener {
                    Log.d("CollectorLocationService", "Alert written for resident $residentId")
                }
                .addOnFailureListener { e ->
                    Log.e("CollectorLocationService", "Failed to write alert for $residentId", e)
                }
        } catch (ex: Exception) {
            Log.e("CollectorLocationService", "sendAlertToResident failed for $residentId", ex)
        }
    }
    // --------------------------------------------------------------------------------------
}
