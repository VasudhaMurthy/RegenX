package com.example.regenx.screens.residents

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.coroutines.launch

@Composable
fun LocateGarbageTruckScreen(
    truckDocIdToShow: String = "truck_01" // change when you want to show a different truck
) {
    val context = LocalContext.current

    // Start resident alert listener while this screen is active
    DisposableEffect(Unit) {
        val listener = ResidentAlertListener(context)
        onDispose {
            // If you later add an unsubscribe method to ResidentAlertListener, call it here.
        }
    }

    var truckPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var isLoading by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(truckPosition, 16f)
    }
    val scope = rememberCoroutineScope()

    // Listen to Firestore updates for truck location (trucks collection)
    LaunchedEffect(truckDocIdToShow) {
        FirebaseFirestore.getInstance()
            .collection("trucks")
            .document(truckDocIdToShow)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("LocateGarbageTruck", "Error fetching truck location", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val lat = snapshot.getDouble("latitude")
                    val lng = snapshot.getDouble("longitude")
                    if (lat != null && lng != null) {
                        val newPos = LatLng(lat, lng)
                        truckPosition = newPos
                        isLoading = false

                        // animate camera to new truck position
                        scope.launch {
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newPos, 16f))
                        }
                    }
                }
            }
    }

    if (!isLoading) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = truckPosition),
                title = "Garbage Truck",
                snippet = "Live Location"
            )
            Circle(
                center = truckPosition,
                radius = 150.0, // radius in meters for geofence (updated to 150m)
                strokeColor = Color.Green,
                fillColor = Color(0x2200FF00) // translucent fill
            )
        }
    }
}
