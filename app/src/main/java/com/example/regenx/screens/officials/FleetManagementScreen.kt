package com.example.regenx.screens.officials

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.regenx.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.appcompat.content.res.AppCompatResources
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

// Data structure to hold truck location and trip info
data class TruckLocation(
    val id: String, // This is now the Government Vehicle ID
    val latLng: LatLng,
    val routeId: String?,
    val status: String,
    val startTime: Long? = null,
    // Note: collectorUid might be added here if needed, but the vehicle ID is the primary key.
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

    val defaultCityCenter = LatLng(12.9716, 77.5946)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCityCenter, 12f)
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
                                // doc.id is now the Government Vehicle ID
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
    // SECONDARY EFFECT: Fetch Path History (COROUTINE FIX APPLIED)
    // ---------------------------------------------------------------------

    LaunchedEffect(selectedTruckId) {
        currentTripPath = emptyList()
        tripStartTime = null
        tripStartLatLng = null

        val vehicleId = selectedTruckId ?: return@LaunchedEffect

        try {
            // Note: Trip reports use the collectorUID, NOT the vehicleId, for lookup.
            // We would need to look up the UID associated with the Vehicle ID first,
            // OR change the Trip Report indexing to use the Vehicle ID.
            // For simplicity, we assume Trip Reports are now indexed by the Vehicle ID.

            val querySnapshot = Firebase.firestore.collection("trip_reports")
                .whereEqualTo("collectorId", vehicleId) // 🌟 ASSUMPTION: Using Vehicle ID for indexing 🌟
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val report = querySnapshot.documents.firstOrNull()?.data
            if (report != null) {

                // Offload heavy processing safely using withContext
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
            // Map View
            GoogleMap(
                modifier = Modifier.weight(0.6f).fillMaxWidth(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // Place a Marker for every actively tracked garbage truck
                truckLocations.forEach { truck ->
                    Marker(
                        state = rememberMarkerState(position = truck.latLng),
                        title = "Vehicle ${truck.id}",
                        snippet = "Route: ${truck.routeId ?: "N/A"} | Status: ${truck.status}",
                        icon = getTruckIcon(truck.status)
                    )
                }

                // DRAW PATH: Polyline for the selected truck
                if (currentTripPath.isNotEmpty()) {
                    Polyline(
                        points = currentTripPath,
                        color = Color.Gray.copy(alpha = 0.7f),
                        width = 8f
                    )
                }

                // MARKER FOR START LOCATION
                tripStartLatLng?.let { startPoint ->
                    Marker(
                        state = rememberMarkerState(position = startPoint),
                        title = "Trip Start: ${formatTime(tripStartTime)}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                }
            }

            // Fleet Summary & List
            TruckListAndSummary(
                trucks = truckLocations,
                isLoading = isLoading,
                selectedTruckId = selectedTruckId,
                tripStartTime = tripStartTime,
                onTruckClick = { id, latLng ->
                    selectedTruckId = id
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            )
        }
    }
}

// ---------------------------------------------------------------------
// HELPER FUNCTIONS & COMPOSABLES
// ---------------------------------------------------------------------

fun formatTime(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "N/A"
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
}

private fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int
): BitmapDescriptor? {
    val vectorDrawable = AppCompatResources.getDrawable(context, vectorResId) ?: return null

    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
fun getTruckIcon(status: String): BitmapDescriptor? {
    val context = LocalContext.current
    val baseIcon = remember(context) {
        bitmapDescriptorFromVector(context, R.drawable.ic_launcher_foreground)
    }

    return when (status) {
        "En Route" -> baseIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        "Idle" -> baseIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
        else -> baseIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
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
fun ColumnScope.TruckListAndSummary(
    trucks: List<TruckLocation>,
    isLoading: Boolean,
    selectedTruckId: String?,
    tripStartTime: Long?,
    onTruckClick: (id: String, latLng: LatLng) -> Unit
) {
    val activeTrucks = trucks.count { it.status == "En Route" }
    val idleTrucks = trucks.count { it.status == "Idle" }
    val totalTrucks = trucks.size

    Column(
        modifier = Modifier
            .weight(0.4f)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Summary Row - SummaryStat is now resolved
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryStat(label = "Total Fleet", value = totalTrucks.toString(), color = MaterialTheme.colorScheme.primary)
            SummaryStat(label = "Active", value = activeTrucks.toString(), color = Color(0xFF4CAF50)) // Green
            SummaryStat(label = "Idle", value = idleTrucks.toString(), color = Color(0xFFFF9800)) // Amber
        }

        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        Text(
            // 🌟 UPDATED UI TEXT 🌟
            text = "Tracked Vehicles (${if(selectedTruckId != null) formatTime(tripStartTime) + " Start" else "Select a Vehicle"})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // List of Trucks
        if (isLoading) {
            Text("Loading truck data...", style = MaterialTheme.typography.bodyMedium)
        } else if (trucks.isEmpty()) {
            Text("No collector vehicles are currently reporting location data.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
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
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // 🌟 UPDATED UI TEXT 🌟
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