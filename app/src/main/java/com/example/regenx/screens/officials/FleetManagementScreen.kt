//package com.example.regenx.screens.officials
//
//import android.app.Activity
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.drawable.Drawable
//import android.util.Log
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.Spring
//import androidx.compose.animation.core.spring
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.gestures.Orientation
//import androidx.compose.foundation.gestures.draggable
//import androidx.compose.foundation.gestures.rememberDraggableState
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.core.app.ActivityCompat
//import androidx.core.graphics.drawable.DrawableCompat
//import androidx.navigation.NavController
//import com.example.regenx.R
//import com.example.regenx.util.GeofenceUtils
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.maps.model.BitmapDescriptor
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.maps.android.compose.*
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import androidx.appcompat.content.res.AppCompatResources
//import com.google.android.gms.maps.CameraUpdateFactory
//import java.text.DecimalFormat
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.tasks.await
//import kotlinx.coroutines.launch
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.runtime.mutableStateMapOf
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.withContext as cwContext
//import kotlinx.coroutines.delay
//
//// ------------------ Data model ------------------
//data class TruckLocation(
//    val id: String,
//    val latLng: LatLng,
//    val routeId: String?,
//    val status: String,
//    val startTime: Long? = null
//)
//
//// ------------------ Main screen ------------------
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FleetManagementScreen(navController: NavController) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val activity = LocalContext.current as? Activity
//
//    var truckLocations by remember { mutableStateOf(emptyList<TruckLocation>()) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    var selectedTruckId by remember { mutableStateOf<String?>(null) }
//    var currentTripPath by remember { mutableStateOf<List<LatLng>>(emptyList()) }
//    var tripStartTime by remember { mutableStateOf<Long?>(null) }
//    var tripStartLatLng by remember { mutableStateOf<LatLng?>(null) }
//
//    var myLocation by remember { mutableStateOf<LatLng?>(null) }
//
//    val defaultCityCenter = LatLng(12.9716, 77.5946)
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(defaultCityCenter, 12f)
//    }
//
//    // persistent marker states to enable smooth moving
//    val markerStates = remember { mutableStateMapOf<String, MarkerState>() }
//
//    // FIXED icon size (won't shrink). Increase if too small on phones.
//    val fixedIconDp = 112.dp
//
//    // Try to create a vector->bitmap descriptor, but fallback to a PNG resource if needed.
//    val truckIcon: BitmapDescriptor? = rememberTruckBitmapDescriptor(resId = R.drawable.ic_truck, sizeDp = fixedIconDp)
//    val truckFallback: BitmapDescriptor? = remember {
//        try {
//            BitmapDescriptorFactory.fromResource(R.drawable.ic_truck)
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    // helpers
//    val selectedTruck = selectedTruckId?.let { id -> truckLocations.find { it.id == id } }
//
//    // get last known device location to center map initially (best-effort)
//    LaunchedEffect(Unit) {
//        try {
//            val fine = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
//            val coarse = ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//            if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
//                val fused = LocationServices.getFusedLocationProviderClient(context)
//                val loc = fused.lastLocation.await()
//                if (loc != null) {
//                    val myLatLng = LatLng(loc.latitude, loc.longitude)
//                    myLocation = myLatLng
//                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(myLatLng, 13f))
//                }
//            }
//        } catch (e: Exception) {
//            Log.w("FleetOfficials", "Could not obtain last location: $e")
//        }
//    }
//
//    // Listen trucks collection
//    DisposableEffect(Unit) {
//        isLoading = true
//        val firestore = Firebase.firestore
//        val registration = firestore.collection("trucks")
//            .addSnapshotListener { snapshot, e ->
//                isLoading = false
//                if (e != null) {
//                    Log.w("FleetOfficials", "Firestore listen failed: $e")
//                    return@addSnapshotListener
//                }
//                if (snapshot != null) {
//                    truckLocations = snapshot.documents.mapNotNull { doc ->
//                        val d = doc.data
//                        val lat = d?.get("latitude") as? Double
//                        val lon = d?.get("longitude") as? Double
//                        if (lat != null && lon != null) {
//                            TruckLocation(
//                                id = doc.id,
//                                latLng = LatLng(lat, lon),
//                                routeId = d["routeId"] as? String,
//                                status = d["status"] as? String ?: "Idle",
//                                startTime = (d["startTime"] as? Number)?.toLong()
//                            )
//                        } else null
//                    }
//                }
//            }
//
//        onDispose { registration.remove() }
//    }
//
//    // When selected truck changes: fetch latest trip report (path & start)
//    LaunchedEffect(selectedTruckId) {
//        currentTripPath = emptyList()
//        tripStartTime = null
//        tripStartLatLng = null
//
//        val vehicleId = selectedTruckId ?: return@LaunchedEffect
//
//        coroutineScope.launch {
//            try {
//                val snap = Firebase.firestore.collection("trip_reports")
//                    .whereEqualTo("collectorId", vehicleId)
//                    .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
//                    .limit(1)
//                    .get()
//                    .await()
//
//                val report = snap.documents.firstOrNull()?.data
//                if (report != null) {
//                    val (path, startTime, startLoc) = withContext(Dispatchers.Default) {
//                        val pathData = report["path"] as? List<Map<String, Double>> ?: emptyList()
//                        val startTimeLong = (report["startTime"] as? Number)?.toLong()
//                        val startLocMap = report["startLocation"] as? Map<String, Double>
//
//                        val pathList = pathData.mapNotNull { pt ->
//                            val pLat = pt["latitude"]
//                            val pLon = pt["longitude"]
//                            if (pLat != null && pLon != null) LatLng(pLat, pLon) else null
//                        }
//
//                        val start = if (startLocMap != null) {
//                            LatLng(startLocMap["latitude"] ?: 0.0, startLocMap["longitude"] ?: 0.0)
//                        } else {
//                            pathList.firstOrNull()
//                        }
//
//                        Triple(pathList, startTimeLong, start)
//                    }
//
//                    currentTripPath = path
//                    tripStartTime = startTime
//                    tripStartLatLng = startLoc
//                }
//            } catch (e: Exception) {
//                Log.w("FleetOfficials", "Failed load trip report: $e")
//            }
//        }
//    }
//
//    // ---------------- Draggable drawer state ----------------
//    val drawerPeekHeight = 140.dp
//    val drawerMaxHeight = 460.dp
//
//    val density = LocalDensity.current
//    // convert to px
//    val peekPx = with(density) { drawerPeekHeight.toPx() }
//    val maxPx = with(density) { drawerMaxHeight.toPx() }
//
//    // the drawer offset from bottom in px (0 means fully expanded, maxPx - peekPx means collapsed)
//    val animOffset = remember { Animatable(maxPx - peekPx) } // initial collapsed
//
//    // internal convenience to get current expanded state
//    val isExpanded by derivedStateOf { animOffset.value < (maxPx - peekPx) / 2.0 }
//
//    // Draggable state (vertical)
//    val draggableState = rememberDraggableState { delta ->
//        coroutineScope.launch {
//            val new = (animOffset.value + delta).coerceIn(0f, maxPx - peekPx)
//            animOffset.snapTo(new)
//        }
//    }
//
//
//    // Helper to settle to expanded/collapsed with spring using velocity
//    suspend fun settleDrawer(velocity: Float) {
//        val threshold = (maxPx - peekPx) / 2f
//        val target = if (animOffset.value <= threshold) 0f else (maxPx - peekPx)
//        animOffset.animateTo(target, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
//    }
//
//    // ---------------- UI ----------------
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
//        },
//        contentWindowInsets = WindowInsets(0,0,0,0)
//    ) { padding ->
//        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
//            // MAP (full available)
//            GoogleMap(
//                modifier = Modifier.fillMaxSize(),
//                cameraPositionState = cameraPositionState,
//                uiSettings = MapUiSettings(zoomControlsEnabled = false),
//                properties = MapProperties(
//                    isMyLocationEnabled = (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//                )
//            ) {
//                // Place markers and update MarkerState positions so they move
//                truckLocations.forEach { truck ->
//                    val state = markerStates.getOrPut(truck.id) { MarkerState(truck.latLng) }
//                    if (state.position != truck.latLng) {
//                        state.position = truck.latLng
//                    }
//
//                    val iconToUse = truckIcon ?: truckFallback ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
//
//                    Marker(
//                        state = state,
//                        title = "Vehicle: ${shortenText(truck.id)}",
//                        snippet = "Status: ${truck.status} • Route: ${truck.routeId ?: "Unassigned"}",
//                        icon = iconToUse,
//                        anchor = Offset(0.5f, 0.5f),
//                        onClick = {
//                            selectedTruckId = truck.id
//                            coroutineScope.launch {
//                                // expand drawer fully
//                                animOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
//                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(truck.latLng, 15f))
//                            }
//                            true
//                        }
//                    )
//                }
//
//                // draw selected truck geofence + path + start marker (if available)
//                selectedTruck?.let { sel ->
//                    Circle(
//                        center = sel.latLng,
//                        radius = GeofenceUtils.GEOFENCE_RADIUS_M,
//                        strokeColor = Color(0xFF4CAF50),
//                        strokeWidth = 3f,
//                        fillColor = Color(0x224CAF50)
//                    )
//
//                    tripStartLatLng?.let { start ->
//                        Marker(
//                            state = MarkerState(start),
//                            title = "Trip Start",
//                            snippet = "Started at: ${formatTime(tripStartTime)}",
//                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
//                        )
//                    }
//                }
//
//                if (currentTripPath.isNotEmpty()) {
//                    Polyline(points = currentTripPath, color = Color.Gray.copy(alpha = 0.85f), width = 8f)
//                }
//            }
//
//            // ---------------- Draggable Drawer overlay ----------------
//            val drawerWidthMod = Modifier
//                .fillMaxWidth()
//                .offset {
//                    // offset Y: animOffset.value px from bottom -> convert to IntOffset
//                    androidx.compose.ui.unit.IntOffset(x = 0, y = animOffset.value.toInt())
//                }
//                // intercept vertical drag gestures
//                .draggable(
//                    state = rememberDraggableState { delta ->
//                        // We update animOffset.snapTo in coroutine
//                        // Use a coroutine inside rememberCoroutineScope
//                        coroutineScope.launch {
//                            val new = (animOffset.value + delta).coerceIn(0f, maxPx - peekPx)
//                            animOffset.snapTo(new)
//                        }
//                    },
//                    orientation = Orientation.Vertical,
//                    onDragStopped = { velocity ->
//                        coroutineScope.launch {
//                            settleDrawer(velocity)
//                        }
//                    }
//                )
//
//            // Draw the drawer surface (placed using offset)
//            Box(
//                modifier = drawerWidthMod
//            ) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(drawerMaxHeight) // fixed container height; vertical offset controls visible area
//                        .align(androidx.compose.ui.Alignment.BottomCenter),
//                    shape = MaterialTheme.shapes.large,
//                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//                ) {
//                    // content inside drawer — keep same layout as before
//                    Column(modifier = Modifier.fillMaxSize()) {
//                        // header/handle area (tap toggles)
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    coroutineScope.launch {
//                                        val target = if (animOffset.value <= (maxPx - peekPx)/2f) (maxPx - peekPx) else 0f
//                                        animOffset.animateTo(target, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
//                                    }
//                                }
//                                .padding(horizontal = 12.dp, vertical = 8.dp)
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .align(androidx.compose.ui.Alignment.CenterHorizontally)
//                                    .width(48.dp)
//                                    .height(4.dp)
//                                    .background(Color.LightGray, shape = MaterialTheme.shapes.small)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                                SummaryStat(label = "Total", value = "${truckLocations.size}", color = MaterialTheme.colorScheme.primary)
//                                SummaryStat(label = "Active", value = "${truckLocations.count { it.status == "En Route" }}", color = Color(0xFF4CAF50))
//                                SummaryStat(label = "Idle", value = "${truckLocations.count { it.status == "Idle" }}", color = Color(0xFFFF9800))
//                            }
//                        }
//
//                        Divider()
//
//                        // Content area: left = list, right = details
//                        Row(modifier = Modifier.fillMaxSize()) {
//                            Column(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
//                                Text(
//                                    text = "Tracked Vehicles",
//                                    style = MaterialTheme.typography.titleMedium,
//                                    modifier = Modifier.padding(12.dp),
//                                    fontWeight = FontWeight.SemiBold
//                                )
//                                Divider()
//                                LazyColumn(modifier = Modifier.fillMaxSize()) {
//                                    items(truckLocations, key = { it.id }) { t ->
//                                        TruckListItem(truck = t, isSelected = t.id == selectedTruck?.id, onClick = {
//                                            // select and center map
//                                            coroutineScope.launch {
//                                                selectedTruckId = t.id
//                                                animOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow))
//                                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(t.latLng, 15f))
//                                            }
//                                        })
//                                        Divider()
//                                    }
//                                }
//                            }
//
//                            Column(modifier = Modifier.weight(0.45f).fillMaxHeight().padding(12.dp)) {
//                                if (selectedTruck == null) {
//                                    Text("Select a vehicle to see details", style = MaterialTheme.typography.bodyMedium)
//                                } else {
//                                    Text("Selected Vehicle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                    Text("ID: ${selectedTruck.id}", style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
//                                    Text("Status: ${selectedTruck.status}", style = MaterialTheme.typography.bodySmall, color = if (selectedTruck.status == "En Route") Color(0xFF2E7D32) else Color.Gray)
//                                    Text("Route: ${selectedTruck.routeId ?: "Unassigned"}", style = MaterialTheme.typography.bodySmall)
//                                    Spacer(modifier = Modifier.height(8.dp))
//
//                                    Text("Trip Start: ${formatTime(tripStartTime)}", style = MaterialTheme.typography.bodySmall)
//                                    Text("Path points: ${currentTripPath.size}", style = MaterialTheme.typography.bodySmall)
//
//                                    // distance covered by currentTripPath
//                                    val distMeters = computePolylineDistanceMeters(currentTripPath)
//                                    Text("Distance covered: ${DecimalFormat("#,##0.##").format(distMeters)} m", style = MaterialTheme.typography.bodySmall)
//
//                                    Spacer(modifier = Modifier.height(12.dp))
//
//                                    Button(onClick = {
//                                        coroutineScope.launch {
//                                            notifyResidentsWithinGeofenceOnce(
//                                                truckId = selectedTruck.id,
//                                                truckLat = selectedTruck.latLng.latitude,
//                                                truckLng = selectedTruck.latLng.longitude,
//                                                radiusMeters = GeofenceUtils.GEOFENCE_RADIUS_M
//                                            )
//                                        }
//                                    }) {
//                                        Text("Notify Residents in Geofence")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// ---------------- small composables ----------------
//@Composable
//fun TruckListItem(truck: TruckLocation, isSelected: Boolean, onClick: () -> Unit) {
//    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
//        Column(modifier = Modifier.weight(1f)) {
//            Text(truck.id, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
//            Text(truck.routeId ?: "Route: Unassigned", style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
//        }
//        Text(truck.status, style = MaterialTheme.typography.bodySmall, color = when (truck.status) { "En Route" -> Color(0xFF2E7D32); "Idle" -> Color(0xFFFF9800); else -> Color.Gray }, modifier = Modifier.padding(start = 8.dp))
//    }
//}
//
//@Composable
//fun SummaryStat(label: String, value: String, color: Color) {
//    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
//        Text(value, style = MaterialTheme.typography.headlineSmall, color = color, textAlign = TextAlign.Center)
//        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//    }
//}
//
//// ---------------- Utility helpers ----------------
//
//fun formatTime(timestamp: Long?): String {
//    if (timestamp == null || timestamp == 0L) return "N/A"
//    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
//}
//
///** compute total straight-line distance along polyline (meters) */
//fun computePolylineDistanceMeters(points: List<LatLng>): Double {
//    if (points.size < 2) return 0.0
//    var total = 0.0
//    for (i in 0 until points.size - 1) {
//        total += GeofenceUtils.haversineDistanceMeters(points[i].latitude, points[i].longitude, points[i+1].latitude, points[i+1].longitude)
//    }
//    return total
//}
//
///**
// * Create a BitmapDescriptor sized in dp (consistent across screen densities).
// * This implementation is tolerant of AdaptiveIconDrawable/vector issues on devices.
// */
//@Composable
//fun rememberTruckBitmapDescriptor(resId: Int, sizeDp: Dp = 96.dp): BitmapDescriptor? {
//    val context = LocalContext.current
//    val density = LocalDensity.current
//
//    return remember(resId, sizeDp) {
//        try {
//            val drawable: Drawable = AppCompatResources.getDrawable(context, resId) ?: return@remember null
//            val wrapped = DrawableCompat.wrap(drawable).mutate()
//            val pxSize = with(density) { sizeDp.toPx() }.toInt().coerceAtLeast(24)
//            wrapped.setBounds(0, 0, pxSize, pxSize)
//            val bitmap = Bitmap.createBitmap(pxSize, pxSize, Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(bitmap)
//            try {
//                wrapped.draw(canvas)
//            } catch (drawEx: Exception) {
//                drawable.setBounds(0, 0, pxSize, pxSize)
//                drawable.draw(canvas)
//            }
//            BitmapDescriptorFactory.fromBitmap(bitmap)
//        } catch (e: Exception) {
//            Log.w("FleetOfficials", "Failed create truck descriptor: $e")
//            null
//        }
//    }
//}
//
///** shorten very long IDs for UI */
//fun shortenText(text: String, max: Int = 32): String {
//    if (text.length <= max) return text
//    val head = text.take(12)
//    val tail = text.takeLast(12)
//    return "$head...$tail"
//}
//
//// ------------------ Notification / Alert writer (one-time per resident per 24h) ------------------
//
///**
// * Query residents within bounding box for (truckLat,truckLng,radiusMeters) and write
// * an alert document into residents/{residentId}/alerts for those not already alerted
// * in the last 24 hours for this truckId.
// *
// * This is client-triggered; for production it's better to do this on the server (Cloud Function)
// * to avoid abuse and to run reliably even when official's device is offline.
// */
//suspend fun notifyResidentsWithinGeofenceOnce(
//    truckId: String,
//    truckLat: Double,
//    truckLng: Double,
//    radiusMeters: Double
//) = cwContext(Dispatchers.IO) {
//    try {
//        val firestore = Firebase.firestore
//        val box = GeofenceUtils.boundingBox(truckLat, truckLng, radiusMeters)
//
//        // Query residents within bounding box to limit reads
//        val snap = firestore.collection("residents")
//            .whereGreaterThanOrEqualTo("latitude", box.minLat)
//            .whereLessThanOrEqualTo("latitude", box.maxLat)
//            .whereGreaterThanOrEqualTo("longitude", box.minLng)
//            .whereLessThanOrEqualTo("longitude", box.maxLng)
//            .get()
//            .await()
//
//        if (snap.isEmpty) {
//            Log.d("FleetOfficials", "No residents in bounding box")
//            return@cwContext
//        }
//
//        val now = System.currentTimeMillis()
//        val dayMillis = 24L * 60L * 60L * 1000L
//
//        for (doc in snap.documents) {
//            val residentId = doc.id
//            val rLat = doc.getDouble("latitude")
//            val rLng = doc.getDouble("longitude")
//            val address = doc.getString("address") ?: ""
//
//            if (rLat == null || rLng == null) continue
//
//            val distance = GeofenceUtils.haversineDistanceMeters(truckLat, truckLng, rLat, rLng)
//            if (distance > radiusMeters) continue
//
//            try {
//                // Check if an alert for this truck exists in resident's alerts within last 24 hours
//                val alertsRef = firestore.collection("residents").document(residentId).collection("alerts")
//                val existing = alertsRef
//                    .whereEqualTo("truckId", truckId)
//                    .whereGreaterThan("timestamp", now - dayMillis)
//                    .limit(1)
//                    .get()
//                    .await()
//
//                if (!existing.isEmpty) {
//                    // recently alerted; skip
//                    Log.d("FleetOfficials", "Resident $residentId already alerted for $truckId within 24h")
//                    continue
//                }
//
//                // Write new alert
//                val alert = HashMap<String, Any>()
//                alert["truckId"] = truckId
//                alert["truckLat"] = truckLat
//                alert["truckLng"] = truckLng
//                alert["distanceMeters"] = distance
//                alert["message"] = "Garbage truck has arrived nearby."
//                alert["address"] = address
//                alert["timestamp"] = now
//                alert["seen"] = false
//
//                alertsRef.add(alert).await()
//                Log.d("FleetOfficials", "Alert written for resident $residentId (truck $truckId)")
//            } catch (inner: Exception) {
//                Log.w("FleetOfficials", "Failed notify resident $residentId: $inner")
//            }
//        }
//    } catch (e: Exception) {
//        Log.e("FleetOfficials", "notifyResidentsWithinGeofenceOnce failed: $e")
//    }
//}
//
//







package com.example.regenx.screens.officials

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// ---------------- DATA MODEL ----------------
data class TruckLocation(
    val id: String,
    val latLng: LatLng,
    val status: String,
    val routeId: String?
)

// ---------------- SCREEN ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetManagementScreen(navController: NavController) {

    Log.e("FleetDebug", "🚨 FleetManagementScreen COMPOSED")

    val scope = rememberCoroutineScope()

    var trucks by remember { mutableStateOf<List<TruckLocation>>(emptyList()) }
    var selectedTruck by remember { mutableStateOf<TruckLocation?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(12.9716, 77.5946),
            12f
        )
    }

    // ---------------- FIRESTORE LISTENER ----------------
    DisposableEffect(Unit) {
        val registration = Firebase.firestore
            .collection("trucks")
            .addSnapshotListener { snapshot, e ->

                Log.e("FleetDebug", "📡 Snapshot callback fired")

                if (e != null) {
                    Log.e("FleetDebug", "❌ Firestore error", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    Log.e("FleetDebug", "✅ Snapshot size = ${snapshot.size()}")

                    trucks = snapshot.documents.mapNotNull { doc ->
                        Log.e("FleetDebug", "🛻 Truck doc ${doc.id} → ${doc.data}")

                        val lat = doc.getDouble("latitude")
                        val lng = doc.getDouble("longitude")
                        val status = doc.getString("status") ?: "Unknown"

                        if (lat != null && lng != null) {
                            TruckLocation(
                                id = doc.id,
                                latLng = LatLng(lat, lng),
                                status = status,
                                routeId = null
                            )
                        } else null
                    }
                }
            }

        onDispose { registration.remove() }
    }

    // ---------------- UI ----------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fleet Monitoring") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ---------------- MAP ----------------
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                trucks.forEach { truck ->
                    Marker(
                        state = MarkerState(truck.latLng),
                        title = truck.id,
                        snippet = truck.status,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        ),
                        onClick = {
                            selectedTruck = truck
                            scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        truck.latLng,
                                        15f
                                    )
                                )
                            }
                            true
                        }
                    )
                }
            }

            // ---------------- BOTTOM DRAWER ----------------
            selectedTruck?.let { truck ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp
                    ),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = "Truck ID",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(
                            text = truck.id,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Status",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )

                        Text(
                            text = truck.status,
                            fontWeight = FontWeight.Bold,
                            color = if (truck.status == "En Route")
                                Color(0xFF4CAF50)
                            else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
