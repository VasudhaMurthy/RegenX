//package com.example.regenx.screens.officials
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.example.regenx.R
//import com.google.android.gms.maps.model.BitmapDescriptor
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.maps.android.compose.*
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import androidx.appcompat.content.res.AppCompatResources
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.tasks.await
//
//// Data structure to hold truck location and trip info
//data class TruckLocation(
//    val id: String, // This is now the Government Vehicle ID
//    val latLng: LatLng,
//    val routeId: String?,
//    val status: String,
//    val startTime: Long? = null,
//    // Note: collectorUid might be added here if needed, but the vehicle ID is the primary key.
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FleetManagementScreen(navController: NavController) {
//
//    var truckLocations by remember { mutableStateOf(emptyList<TruckLocation>()) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    var selectedTruckId by remember { mutableStateOf<String?>(null) }
//    var currentTripPath by remember { mutableStateOf(emptyList<LatLng>()) }
//    var tripStartTime by remember { mutableStateOf<Long?>(null) }
//    var tripStartLatLng by remember { mutableStateOf<LatLng?>(null) }
//
//    val defaultCityCenter = LatLng(12.9716, 77.5946)
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(defaultCityCenter, 12f)
//    }
//
//    // ---------------------------------------------------------------------
//    // LIVE DATA FETCHING & LISTENER (Active Trucks)
//    // ---------------------------------------------------------------------
//
//    DisposableEffect(Unit) {
//        isLoading = true
//        val firestore = Firebase.firestore
//
//        val registration = firestore.collection("trucks")
//            .addSnapshotListener { snapshot, e ->
//                isLoading = false
//                if (e != null) {
//                    println("FleetManagementScreen: Firestore listen failed: $e")
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null) {
//                    truckLocations = snapshot.documents.mapNotNull { doc ->
//                        val data = doc.data
//                        val lat = data?.get("latitude") as? Double
//                        val lon = data?.get("longitude") as? Double
//
//                        if (lat != null && lon != null) {
//                            TruckLocation(
//                                // doc.id is now the Government Vehicle ID
//                                id = doc.id,
//                                latLng = LatLng(lat, lon),
//                                routeId = data["routeId"] as? String,
//                                status = data["status"] as? String ?: "Idle",
//                                startTime = data["startTime"] as? Long
//                            )
//                        } else null
//                    }
//                }
//            }
//
//        onDispose { registration.remove() }
//    }
//
//    // ---------------------------------------------------------------------
//    // SECONDARY EFFECT: Fetch Path History (COROUTINE FIX APPLIED)
//    // ---------------------------------------------------------------------
//
//    LaunchedEffect(selectedTruckId) {
//        currentTripPath = emptyList()
//        tripStartTime = null
//        tripStartLatLng = null
//
//        val vehicleId = selectedTruckId ?: return@LaunchedEffect
//
//        try {
//            // Note: Trip reports use the collectorUID, NOT the vehicleId, for lookup.
//            // We would need to look up the UID associated with the Vehicle ID first,
//            // OR change the Trip Report indexing to use the Vehicle ID.
//            // For simplicity, we assume Trip Reports are now indexed by the Vehicle ID.
//
//            val querySnapshot = Firebase.firestore.collection("trip_reports")
//                .whereEqualTo("collectorId", vehicleId) // 🌟 ASSUMPTION: Using Vehicle ID for indexing 🌟
//                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
//                .limit(1)
//                .get()
//                .await()
//
//            val report = querySnapshot.documents.firstOrNull()?.data
//            if (report != null) {
//
//                // Offload heavy processing safely using withContext
//                val (newPath, newStartTime, newStartLatLng) = withContext(Dispatchers.Default) {
//                    val pathData = report["path"] as? List<Map<String, Double>> ?: emptyList()
//                    val startTimeLong = report["startTime"] as? Long
//                    val startLocMap = report["startLocation"] as? Map<String, Double>
//
//                    val path = pathData.mapNotNull { point ->
//                        val pLat = point["latitude"]
//                        val pLon = point["longitude"]
//                        if (pLat != null && pLon != null) LatLng(pLat, pLon) else null
//                    }
//
//                    val startLoc = if (startLocMap != null) {
//                        LatLng(startLocMap["latitude"] ?: 0.0, startLocMap["longitude"] ?: 0.0)
//                    } else {
//                        path.firstOrNull()
//                    }
//
//                    Triple(path, startTimeLong, startLoc)
//                }
//
//                currentTripPath = newPath
//                tripStartTime = newStartTime
//                tripStartLatLng = newStartLatLng
//            }
//        } catch (e: Exception) {
//            println("Error fetching trip report: $e")
//        }
//    }
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Fleet & Route Management") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//        ) {
//            // Map View
//            GoogleMap(
//                modifier = Modifier.weight(0.6f).fillMaxWidth(),
//                cameraPositionState = cameraPositionState,
//                uiSettings = MapUiSettings(zoomControlsEnabled = false)
//            ) {
//                // Place a Marker for every actively tracked garbage truck
//                truckLocations.forEach { truck ->
//                    Marker(
//                        state = rememberMarkerState(position = truck.latLng),
//                        title = "Vehicle ${truck.id}",
//                        snippet = "Route: ${truck.routeId ?: "N/A"} | Status: ${truck.status}",
//                        icon = getTruckIcon(truck.status)
//                    )
//                }
//
//                // DRAW PATH: Polyline for the selected truck
//                if (currentTripPath.isNotEmpty()) {
//                    Polyline(
//                        points = currentTripPath,
//                        color = Color.Gray.copy(alpha = 0.7f),
//                        width = 8f
//                    )
//                }
//
//                // MARKER FOR START LOCATION
//                tripStartLatLng?.let { startPoint ->
//                    Marker(
//                        state = rememberMarkerState(position = startPoint),
//                        title = "Trip Start: ${formatTime(tripStartTime)}",
//                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
//                    )
//                }
//            }
//
//            // Fleet Summary & List
//            TruckListAndSummary(
//                trucks = truckLocations,
//                isLoading = isLoading,
//                selectedTruckId = selectedTruckId,
//                tripStartTime = tripStartTime,
//                onTruckClick = { id, latLng ->
//                    selectedTruckId = id
//                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
//                }
//            )
//        }
//    }
//}
//
//// ---------------------------------------------------------------------
//// HELPER FUNCTIONS & COMPOSABLES
//// ---------------------------------------------------------------------
//
//fun formatTime(timestamp: Long?): String {
//    if (timestamp == null || timestamp == 0L) return "N/A"
//    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
//}
//
//private fun bitmapDescriptorFromVector(
//    context: Context,
//    vectorResId: Int
//): BitmapDescriptor? {
//    val vectorDrawable = AppCompatResources.getDrawable(context, vectorResId) ?: return null
//
//    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
//    val bitmap = Bitmap.createBitmap(
//        vectorDrawable.intrinsicWidth,
//        vectorDrawable.intrinsicHeight,
//        Bitmap.Config.ARGB_8888
//    )
//    val canvas = Canvas(bitmap)
//    vectorDrawable.draw(canvas)
//    return BitmapDescriptorFactory.fromBitmap(bitmap)
//}
//
//@Composable
//fun getTruckIcon(status: String): BitmapDescriptor? {
//    val context = LocalContext.current
//    val baseIcon = remember(context) {
//        bitmapDescriptorFromVector(context, R.drawable.ic_launcher_foreground)
//    }
//
//    return when (status) {
//        "En Route" -> baseIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
//        "Idle" -> baseIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
//        else -> baseIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
//    }
//}
//
//
//@Composable
//fun SummaryStat(label: String, value: String, color: Color) {
//    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
//        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
//        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//    }
//}
//
//
//@Composable
//fun ColumnScope.TruckListAndSummary(
//    trucks: List<TruckLocation>,
//    isLoading: Boolean,
//    selectedTruckId: String?,
//    tripStartTime: Long?,
//    onTruckClick: (id: String, latLng: LatLng) -> Unit
//) {
//    val activeTrucks = trucks.count { it.status == "En Route" }
//    val idleTrucks = trucks.count { it.status == "Idle" }
//    val totalTrucks = trucks.size
//
//    Column(
//        modifier = Modifier
//            .weight(0.4f)
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        // Summary Row - SummaryStat is now resolved
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceAround
//        ) {
//            SummaryStat(label = "Total Fleet", value = totalTrucks.toString(), color = MaterialTheme.colorScheme.primary)
//            SummaryStat(label = "Active", value = activeTrucks.toString(), color = Color(0xFF4CAF50)) // Green
//            SummaryStat(label = "Idle", value = idleTrucks.toString(), color = Color(0xFFFF9800)) // Amber
//        }
//
//        Spacer(Modifier.height(8.dp))
//        Divider()
//        Spacer(Modifier.height(8.dp))
//
//        Text(
//            // 🌟 UPDATED UI TEXT 🌟
//            text = "Tracked Vehicles (${if(selectedTruckId != null) formatTime(tripStartTime) + " Start" else "Select a Vehicle"})",
//            style = MaterialTheme.typography.titleMedium,
//            fontWeight = FontWeight.Bold
//        )
//
//        // List of Trucks
//        if (isLoading) {
//            Text("Loading truck data...", style = MaterialTheme.typography.bodyMedium)
//        } else if (trucks.isEmpty()) {
//            Text("No collector vehicles are currently reporting location data.", style = MaterialTheme.typography.bodyMedium)
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                items(trucks, key = { it.id }) { truck ->
//                    TruckListItem(
//                        truck = truck,
//                        isSelected = truck.id == selectedTruckId,
//                        onClick = { onTruckClick(truck.id, truck.latLng) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun TruckListItem(truck: TruckLocation, isSelected: Boolean, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable(onClick = onClick),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else when (truck.status) {
//                "En Route" -> Color(0xFFE8F5E9)
//                "Idle" -> Color(0xFFFFF8E1)
//                else -> Color(0xFFFFEBEE)
//            }
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp).fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column {
//                // 🌟 UPDATED UI TEXT 🌟
//                Text("Vehicle ID: ${truck.id}", fontWeight = FontWeight.SemiBold)
//                Text("Route: ${truck.routeId ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall)
//            }
//            Text(truck.status, color = when (truck.status) {
//                "En Route" -> Color(0xFF4CAF50)
//                "Idle" -> Color(0xFFFF9800)
//                else -> Color(0xFFF44336)
//            })
//        }
//    }
//}







package com.example.regenx.screens.officials

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.regenx.R
import com.example.regenx.util.GeofenceUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.CameraUpdateFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.saveable.rememberSaveable

// Data structure to hold truck location and trip info
data class TruckLocation(
    val id: String, // Government Vehicle ID
    val latLng: LatLng,
    val routeId: String?,
    val status: String,
    val startTime: Long? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetManagementScreen(navController: NavController) {

    var truckLocations by remember { mutableStateOf(emptyList<TruckLocation>()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTruckId by remember { mutableStateOf<String?>(null) }
    var currentTripPath by remember { mutableStateOf(emptyList<LatLng>()) }
    var tripStartTime by remember { mutableStateOf<Long?>(null) }
    var tripStartLatLng by remember { mutableStateOf<LatLng?>(null) }

    // Keep user's current location (if permission granted)
    var myLocation by remember { mutableStateOf<LatLng?>(null) }

    val defaultCityCenter = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCityCenter, 12f)
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity

    // ---------------------------------------------------------------------
    // Attempt to get device's last known location (if permission granted)
    // ---------------------------------------------------------------------
    LaunchedEffect(Unit) {
        try {
            val fine = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)

            if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val loc = fused.lastLocation.await()
                if (loc != null) {
                    val myLatLng = LatLng(loc.latitude, loc.longitude)
                    myLocation = myLatLng
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f))
                    }
                }
            }
        } catch (e: Exception) {
            // ignore — fallback to defaultCityCenter
            println("Could not obtain last location: $e")
        }
    }

    // ---------------------------------------------------------------------
    // LIVE DATA FETCHING & LISTENER (Active Trucks)
    // ---------------------------------------------------------------------
    DisposableEffect(Unit) {
        isLoading = true
        val firestore = Firebase.firestore

        val registration = firestore.collection("trucks")
            .addSnapshotListener { snapshot, e ->
                isLoading = false
                if (e != null) {
                    println("FleetManagementScreen: Firestore listen failed: $e")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    truckLocations = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        val lat = data?.get("latitude") as? Double
                        val lon = data?.get("longitude") as? Double

                        if (lat != null && lon != null) {
                            TruckLocation(
                                id = doc.id,
                                latLng = LatLng(lat, lon),
                                routeId = data["routeId"] as? String,
                                status = data["status"] as? String ?: "Idle",
                                startTime = data["startTime"] as? Long
                            )
                        } else null
                    }
                }
            }

        onDispose { registration.remove() }
    }

    // ---------------------------------------------------------------------
    // SECONDARY EFFECT: Fetch Path History for selected truck
    // ---------------------------------------------------------------------
    LaunchedEffect(selectedTruckId) {
        currentTripPath = emptyList()
        tripStartTime = null
        tripStartLatLng = null

        val vehicleId = selectedTruckId ?: return@LaunchedEffect

        try {
            val querySnapshot = Firebase.firestore.collection("trip_reports")
                .whereEqualTo("collectorId", vehicleId)
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val report = querySnapshot.documents.firstOrNull()?.data
            if (report != null) {

                val (newPath, newStartTime, newStartLatLng) = withContext(Dispatchers.Default) {
                    val pathData = report["path"] as? List<Map<String, Double>> ?: emptyList()
                    val startTimeLong = report["startTime"] as? Long
                    val startLocMap = report["startLocation"] as? Map<String, Double>

                    val path = pathData.mapNotNull { point ->
                        val pLat = point["latitude"]
                        val pLon = point["longitude"]
                        if (pLat != null && pLon != null) LatLng(pLat, pLon) else null
                    }

                    val startLoc = if (startLocMap != null) {
                        LatLng(startLocMap["latitude"] ?: 0.0, startLocMap["longitude"] ?: 0.0)
                    } else {
                        path.firstOrNull()
                    }

                    Triple(path, startTimeLong, startLoc)
                }

                currentTripPath = newPath
                tripStartTime = newStartTime
                tripStartLatLng = newStartLatLng
            }
        } catch (e: Exception) {
            println("Error fetching trip report: $e")
        }
    }

    // ---------------------------------------------------------------------
    // UI
    // ---------------------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fleet & Route Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Map View takes 60% of vertical space
            Box(modifier = Modifier.weight(0.6f).fillMaxWidth()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
                ) {
                    // create a stable truck icon sized in dp (36dp by default)
                    val truckIcon = rememberTruckBitmapDescriptor(resId = R.drawable.ic_truck, sizeDp = 36.dp)

                    // Place a Marker for every actively tracked garbage truck (center-anchored icon)
                    truckLocations.forEach { truck ->
                        Marker(
                            state = rememberMarkerState(position = truck.latLng),
                            title = "Vehicle ${truck.id}",
                            snippet = "Route: ${truck.routeId ?: "N/A"} | Status: ${truck.status}",
                            icon = truckIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                            anchor = Offset(0.5f, 0.5f),
                            onClick = {
                                selectedTruckId = truck.id
                                coroutineScope.launch {
                                    // also center the map on the truck so it's in middle of radius
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(truck.latLng, 16f)
                                    )
                                }
                                true
                            }
                        )
                    }

                    // If a truck is selected, draw a Haversine/geofence circle around it (radius from GeofenceUtils)
                    selectedTruckId?.let { selId ->
                        truckLocations.find { it.id == selId }?.let { selTruck ->
                            Circle(
                                center = selTruck.latLng,
                                radius = GeofenceUtils.GEOFENCE_RADIUS_M,
                                strokeColor = Color(0xFF4CAF50),
                                strokeWidth = 3f,
                                fillColor = Color(0x224CAF50)
                            )
                        }
                    }

                    // DRAW PATH: Polyline for the selected truck
                    if (currentTripPath.isNotEmpty()) {
                        Polyline(
                            points = currentTripPath,
                            color = Color.Gray.copy(alpha = 0.7f),
                            width = 8f
                        )
                    }

                    // MARKER FOR START LOCATION (if present from trip report)
                    tripStartLatLng?.let { startPoint ->
                        Marker(
                            state = rememberMarkerState(position = startPoint),
                            title = "Trip Start: ${formatTime(tripStartTime)}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                    }
                }

                // Center button (top-right) — centers on selected truck or user's current location
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = androidx.compose.ui.Alignment.TopEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                val targetLatLng = selectedTruckId?.let { id ->
                                    truckLocations.find { it.id == id }?.latLng
                                } ?: myLocation ?: defaultCityCenter
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(targetLatLng, 15f)
                                )
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("Center")
                    }
                }
            }

            // Fleet Summary & List + Selected Truck Info Panel (remaining 40% of space)
            TruckListAndSummary(
                modifier = Modifier.weight(0.4f),
                trucks = truckLocations,
                isLoading = isLoading,
                selectedTruckId = selectedTruckId,
                tripStartTime = tripStartTime,
                onTruckClick = { id: String, latLng: LatLng ->
                    selectedTruckId = id
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(latLng, 16f)
                        )
                    }
                }
            )

            // Selected truck bottom info panel (shows when a truck is selected)
            selectedTruckId?.let { selId ->
                val selTruck = truckLocations.find { it.id == selId }
                selTruck?.let { truck ->
                    SelectedTruckInfoPanel(truck, currentTripPath, tripStartTime)
                }
            }
        }
    }
}

@Composable
fun TruckListAndSummary(
    modifier: Modifier = Modifier,
    trucks: List<TruckLocation>,
    isLoading: Boolean,
    selectedTruckId: String?,
    tripStartTime: Long?,
    onTruckClick: (String, LatLng) -> Unit
) {
    val activeTrucks = trucks.count { it.status == "En Route" }
    val idleTrucks = trucks.count { it.status == "Idle" }
    val totalTrucks = trucks.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryStat(label = "Total Fleet", value = totalTrucks.toString(), color = MaterialTheme.colorScheme.primary)
            SummaryStat(label = "Active", value = activeTrucks.toString(), color = Color(0xFF4CAF50))
            SummaryStat(label = "Idle", value = idleTrucks.toString(), color = Color(0xFFFF9800))
        }

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Tracked Vehicles (${if(selectedTruckId != null) formatTime(tripStartTime) + " Start" else "Select a Vehicle"})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            Text("Loading truck data...", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
        } else if (trucks.isEmpty()) {
            Text("No collector vehicles are currently reporting location data.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
            ) {
                items(trucks, key = { it.id }) { truck ->
                    TruckListItem(
                        truck = truck,
                        isSelected = truck.id == selectedTruckId,
                        onClick = { onTruckClick(truck.id, truck.latLng) }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun TruckListItem(truck: TruckLocation, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else when (truck.status) {
                "En Route" -> Color(0xFFE8F5E9)
                "Idle" -> Color(0xFFFFF8E1)
                else -> Color(0xFFFFEBEE)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Vehicle ID: ${truck.id}", fontWeight = FontWeight.SemiBold)
                Text("Route: ${truck.routeId ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall)
            }
            Text(truck.status, color = when (truck.status) {
                "En Route" -> Color(0xFF4CAF50)
                "Idle" -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            })
        }
    }
}

@Composable
fun SelectedTruckInfoPanel(truck: TruckLocation, path: List<LatLng>, tripStartTime: Long?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.98f))
            .padding(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Selected Vehicle: ${truck.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Status: ${truck.status}", style = MaterialTheme.typography.bodyMedium)
                Text("Route: ${truck.routeId ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text("Trip Start: ${formatTime(tripStartTime)}", style = MaterialTheme.typography.bodySmall)
                Text("Path Points: ${path.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (path.isEmpty()) {
            Text("No recent trip path available for this vehicle.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        } else {
            Text("Recent path preview:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            path.take(3).forEachIndexed { idx, p ->
                Text("${idx + 1}. ${"%.5f".format(p.latitude)}, ${"%.5f".format(p.longitude)}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun formatTime(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "N/A"
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

/**
 * Create a BitmapDescriptor sized in dp (consistent across screen densities).
 * This is a @Composable so we can use LocalDensity and remember the generated descriptor.
 */
@Composable
fun rememberTruckBitmapDescriptor(resId: Int, sizeDp: Dp = 36.dp): BitmapDescriptor? {
    val context = LocalContext.current
    val density = LocalDensity.current

    // compute px size based on dp using LocalDensity
    val px = with(density) { sizeDp.toPx().toInt() }

    return remember(resId, sizeDp) {
        val vectorDrawable = AppCompatResources.getDrawable(context, resId) ?: return@remember null
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, px, px)
        vectorDrawable.draw(canvas)
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

