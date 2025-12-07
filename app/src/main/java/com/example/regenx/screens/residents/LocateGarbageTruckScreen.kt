//package com.example.regenx.screens.residents
//
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.util.Log
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.navigation.NavController
//import com.example.regenx.R
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.model.BitmapDescriptor
//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.CameraPosition
//import com.google.android.gms.maps.model.LatLng
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//import com.google.maps.android.compose.GoogleMap
//import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
//import com.google.maps.android.compose.Polyline
//import com.google.maps.android.compose.rememberCameraPositionState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.ui.graphics.Color
//
//@Composable
//fun LocateGarbageTruckScreen(navController: NavController) {
//
//    val context = LocalContext.current
//
//    // List of (truckId, position)
//    var truckList by remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }
//    var isLoadingTrucks by remember { mutableStateOf(true) }
//
//    // Logged-in resident location
//    var residentPosition by remember { mutableStateOf<LatLng?>(null) }
//    var isLoadingResident by remember { mutableStateOf(true) }
//
//    // Bigger marker icon (60dp x 60dp)
//    val truckIcon: BitmapDescriptor? = remember {
//        try {
//            bitmapDescriptorFromVector(
//                context = context,
//                vectorResId = R.drawable.ic_truck,
//                widthDp = 60,
//                heightDp = 60
//            )
//        } catch (e: Exception) {
//            Log.e("LocateGarbageTruck", "Error creating truck icon", e)
//            null
//        }
//    }
//
//    // Camera state – start somewhere neutral
//    val cameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 14f)
//    }
//
//    // --- Firestore listener for TRUCKS ---
//    DisposableEffect(Unit) {
//        val registration: ListenerRegistration =
//            FirebaseFirestore.getInstance()
//                .collection("trucks")
//                .addSnapshotListener { snapshot, e ->
//                    Log.d(
//                        "LocateGarbageTruck",
//                        "snapshot null? ${snapshot == null}, error: $e"
//                    )
//
//                    if (e != null) {
//                        Log.e("LocateGarbageTruck", "Error fetching trucks", e)
//                        isLoadingTrucks = false
//                        return@addSnapshotListener
//                    }
//
//                    if (snapshot != null) {
//                        val updatedList = mutableListOf<Pair<String, LatLng>>()
//
//                        for (doc in snapshot.documents) {
//                            val lat = doc.getDouble("latitude")
//                            val lng = doc.getDouble("longitude")
//
//                            if (lat != null && lng != null) {
//                                updatedList.add(doc.id to LatLng(lat, lng))
//                            } else {
//                                Log.w(
//                                    "LocateGarbageTruck",
//                                    "Missing lat/lng for truck ${doc.id}"
//                                )
//                            }
//                        }
//
//                        truckList = updatedList
//                        isLoadingTrucks = false
//                    } else {
//                        isLoadingTrucks = false
//                    }
//                }
//
//        onDispose {
//            registration.remove()
//        }
//    }
//
//    // --- Firestore listener / fetch for RESIDENT (current user) ---
//    DisposableEffect(Unit) {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        var registration: ListenerRegistration? = null
//
//        if (uid != null) {
//            registration = FirebaseFirestore.getInstance()
//                .collection("residents")          // <--- change if your collection name is different
//                .document(uid)                    // <--- assuming doc id = user uid
//                .addSnapshotListener { snapshot, e ->
//                    if (e != null) {
//                        Log.e("LocateGarbageTruck", "Error fetching resident", e)
//                        isLoadingResident = false
//                        return@addSnapshotListener
//                    }
//
//                    if (snapshot != null && snapshot.exists()) {
//                        val lat = snapshot.getDouble("latitude")
//                        val lng = snapshot.getDouble("longitude")
//
//                        if (lat != null && lng != null) {
//                            residentPosition = LatLng(lat, lng)
//                            Log.d("LocateGarbageTruck", "Resident at $lat,$lng")
//                        } else {
//                            Log.w("LocateGarbageTruck", "Missing resident lat/lng")
//                        }
//                    }
//                    isLoadingResident = false
//                }
//        } else {
//            Log.w("LocateGarbageTruck", "No logged-in user")
//            isLoadingResident = false
//        }
//
//        onDispose {
//            registration?.remove()
//        }
//    }
//
//    // Move camera: priority -> resident, then first truck
//    LaunchedEffect(residentPosition, truckList) {
//        when {
//            residentPosition != null -> {
//                cameraPositionState.animate(
//                    CameraUpdateFactory.newCameraPosition(
//                        CameraPosition.fromLatLngZoom(residentPosition!!, 15f)
//                    )
//                )
//            }
//            truckList.isNotEmpty() -> {
//                val firstTruckPos = truckList.first().second
//                cameraPositionState.animate(
//                    CameraUpdateFactory.newCameraPosition(
//                        CameraPosition.fromLatLngZoom(firstTruckPos, 15f)
//                    )
//                )
//            }
//        }
//    }
//
//    // Compute nearest truck to resident (if both are available)
//    val nearestTruckToResident: LatLng? =
//        if (residentPosition != null && truckList.isNotEmpty()) {
//            val res = residentPosition!!
//            truckList.minByOrNull { (_, truckPos) ->
//                val dx = truckPos.latitude - res.latitude
//                val dy = truckPos.longitude - res.longitude
//                dx * dx + dy * dy           // squared distance is enough
//            }?.second
//        } else null
//
//    // Layout with map + back button overlay
//    Box(modifier = Modifier.fillMaxSize()) {
//
//        GoogleMap(
//            modifier = Modifier.fillMaxSize(),
//            cameraPositionState = cameraPositionState
//        ) {
//            // Mark all trucks
//            truckList.forEach { (truckId, position) ->
//                Marker(
//                    state = MarkerState(position = position),
//                    title = "Garbage Truck: $truckId",
//                    snippet = "Live Location",
//                    icon = truckIcon
//                )
//            }
//
//            // Mark resident house (default marker)
//            residentPosition?.let { resPos ->
//                Marker(
//                    state = MarkerState(position = resPos),
//                    title = "Your House",
//                    snippet = "Resident"
//                )
//            }
//
//            // Draw line between resident and nearest truck
//            if (residentPosition != null && nearestTruckToResident != null) {
//                Polyline(
//                    points = listOf(residentPosition!!, nearestTruckToResident),
//                    color = Color.Blue,
//                    width = 8f
//                )
//            }
//        }
//
//        // Back button
//        IconButton(
//            onClick = { navController.popBackStack() },
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .padding(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.ArrowBack,
//                contentDescription = "Back",
//                tint = MaterialTheme.colorScheme.onSurface
//            )
//        }
//    }
//}
//
///**
// * Helper to convert a vector or png drawable to BitmapDescriptor with a given size.
// */
//fun bitmapDescriptorFromVector(
//    context: Context,
//    vectorResId: Int,
//    widthDp: Int = 32,
//    heightDp: Int = 32
//): BitmapDescriptor {
//
//    val drawable = ContextCompat.getDrawable(context, vectorResId)
//        ?: throw IllegalArgumentException("Resource $vectorResId not found")
//
//    val density = context.resources.displayMetrics.density
//    val widthPx = (widthDp * density).toInt()
//    val heightPx = (heightDp * density).toInt()
//
//    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
//    val canvas = Canvas(bitmap)
//
//    drawable.setBounds(0, 0, widthPx, heightPx)
//    drawable.draw(canvas)
//
//    return BitmapDescriptorFactory.fromBitmap(bitmap)
//}











package com.example.regenx.screens.residents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.regenx.R
import com.example.regenx.network.DirectionsApiService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

@Composable
fun LocateGarbageTruckScreen(navController: NavController) {

    val context = LocalContext.current

    // Resident + trucks
    var residentLatLng by remember { mutableStateOf<LatLng?>(null) }
    var truckList by remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }

    // Directions / route
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var etaText by remember { mutableStateOf("") }
    var hasRequestedRoute by remember { mutableStateOf(false) }

    val apiKey = LocalContext.current.getString(R.string.google_maps_key)

    // Truck icon (green truck); falls back to green marker if it fails
    val truckIcon: BitmapDescriptor? = remember {
        try {
            bitmapDescriptorFromResource(
                context = context,
                resId = R.drawable.ic_truck,   // your drawable
                widthDp = 64,
                heightDp = 64
            )
        } catch (e: Exception) {
            Log.e("LocateGarbageTruck", "Error creating truck icon", e)
            null
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 14f)
    }

    // 1) Fetch resident lat/lng from residents/{uid}
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("LocateGarbageTruck", "No logged-in user")
            return@LaunchedEffect
        }

        val uid = user.uid

        val snapshot = FirebaseFirestore.getInstance()
            .collection("residents")
            .document(uid)
            .get()
            .awaitOrNull()

        if (snapshot == null || !snapshot.exists()) {
            Log.w("LocateGarbageTruck", "No resident doc found for uid=$uid")
            return@LaunchedEffect
        }

        val lat = snapshot.getDouble("latitude")
        val lng = snapshot.getDouble("longitude")

        if (lat == null || lng == null) {
            Log.w("LocateGarbageTruck", "Resident lat/lng missing on document")
            return@LaunchedEffect
        }

        val pos = LatLng(lat, lng)
        residentLatLng = pos
        Log.d("LocateGarbageTruck", "Resident lat/lng from Firestore: $lat, $lng")

        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(pos, 15f)
        )
    }

    // 2) Listen to trucks collection
    DisposableEffect(Unit) {
        val registration: ListenerRegistration =
            FirebaseFirestore.getInstance()
                .collection("trucks")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("LocateGarbageTruck", "Error fetching trucks", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val updated = snapshot.documents.mapNotNull { doc ->
                            val lat = doc.getDouble("latitude")
                            val lng = doc.getDouble("longitude")
                            if (lat != null && lng != null) {
                                Log.d("LocateGarbageTruck", "Truck ${doc.id}: $lat, $lng")
                                doc.id to LatLng(lat, lng)
                            } else null
                        }
                        truckList = updated
                    }
                }

        onDispose { registration.remove() }
    }

    // 3) Once resident + at least 1 truck loaded, compute route (or straight line)
    LaunchedEffect(residentLatLng, truckList) {
        if (hasRequestedRoute) return@LaunchedEffect
        val res = residentLatLng ?: return@LaunchedEffect
        if (truckList.isEmpty()) return@LaunchedEffect

        val nearest = truckList.minByOrNull { (_, truckPos) ->
            val dx = truckPos.latitude - res.latitude
            val dy = truckPos.longitude - res.longitude
            dx * dx + dy * dy
        } ?: return@LaunchedEffect

        val truckPos = nearest.second
        Log.d("LocateGarbageTruck", "Nearest truck at ${truckPos.latitude},${truckPos.longitude}")

        // Show both locations
        try {
            val bounds = LatLngBounds.builder()
                .include(res)
                .include(truckPos)
                .build()
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(bounds, 120)
            )
        } catch (e: Exception) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(res, 14f)
            )
        }

        // Directions API call, fallback to straight line if no route
        try {
            val response: JSONObject? = DirectionsApiService.getRoute(
                origin = "${truckPos.latitude},${truckPos.longitude}",
                destination = "${res.latitude},${res.longitude}",
                apiKey = apiKey
            )

            if (response != null) {
                val status = response.optString("status")
                val errorMsg = response.optString("error_message")
                Log.d("LocateGarbageTruck", "Directions status = $status, error = $errorMsg")

                if (status == "OK") {
                    val routes = response.getJSONArray("routes")
                    val route0 = routes.getJSONObject(0)
                    val legs = route0.getJSONArray("legs").getJSONObject(0)

                    etaText = legs
                        .getJSONObject("duration")
                        .getString("text")

                    val polyline = route0
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    routePoints = decodePolyline(polyline)
                    Log.d(
                        "LocateGarbageTruck",
                        "Route points from Directions: ${routePoints.size}, ETA: $etaText"
                    )
                } else {
                    Log.w(
                        "LocateGarbageTruck",
                        "Directions not OK ($status). Using straight line."
                    )
                    routePoints = listOf(truckPos, res)
                }
            } else {
                Log.w("LocateGarbageTruck", "Directions response null, using straight line")
                routePoints = listOf(truckPos, res)
            }
        } catch (e: CancellationException) {
            Log.w("LocateGarbageTruck", "Directions coroutine cancelled")
        } catch (e: Exception) {
            Log.e("LocateGarbageTruck", "Directions API failed, using straight line", e)
            routePoints = listOf(truckPos, res)
        }

        hasRequestedRoute = true
    }

    // ---------- UI ----------
    Box(modifier = Modifier.fillMaxSize()) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            // Resident marker
            residentLatLng?.let {
                Marker(
                    state = MarkerState(it),
                    title = "Your house"
                )
            }

            // Truck markers
            truckList.forEach { (id, pos) ->
                Marker(
                    state = MarkerState(pos),
                    title = "Garbage Truck: $id",
                    icon = truckIcon
                        ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
            }

            // Route polyline (Directions or straight line)
            if (routePoints.isNotEmpty()) {
                Polyline(
                    points = routePoints,
                    color = Color.Blue,
                    width = 10f
                )
            }
        }

        // ETA label (only if we got it)
        if (etaText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp)
            ) {
                Text(
                    text = "ETA: $etaText",
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }


        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Truck icon from PNG/vector resource, scaled */
fun bitmapDescriptorFromResource(
    context: Context,
    resId: Int,
    widthDp: Int,
    heightDp: Int
): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val original = BitmapFactory.decodeResource(context.resources, resId)
        ?: throw IllegalArgumentException("Resource $resId not found")

    val scaled = Bitmap.createScaledBitmap(original, widthPx, heightPx, true)
    return BitmapDescriptorFactory.fromBitmap(scaled)
}

/** Generic awaitOrNull for any Task<T> */
suspend fun <T> Task<T>.awaitOrNull(): T? {
    return try {
        this.await()
    } catch (e: Exception) {
        null
    }
}

/** Polyline decoder for Directions API encoded path */
fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        poly.add(
            LatLng(
                lat / 1E5,
                lng / 1E5
            )
        )
    }
    return poly
}





