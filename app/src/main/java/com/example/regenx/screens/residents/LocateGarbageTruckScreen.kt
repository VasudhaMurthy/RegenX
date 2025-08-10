package com.example.regenx.screens.residents

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color

@Composable
fun LocateGarbageTruckScreen() {
    var truckPosition by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var isLoading by remember { mutableStateOf(true) }

    // Listen to Firestore updates for truck location
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("collectors")
            .document("truck_01") // Use the same collectorId as in your collector service
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("LocateGarbageTruck", "Error fetching truck location", e)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val lat = snapshot.getDouble("lat")
                    val lng = snapshot.getDouble("lng")
                    if (lat != null && lng != null) {
                        truckPosition = LatLng(lat, lng)
                        isLoading = false
                    }
                }
            }
    }

    if (!isLoading) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(truckPosition, 16f)
        }

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
                radius = 100.0, // radius in meters for geofence
                strokeColor = Color.Green,
                fillColor = Color(0x2200FF00) // translucent fill
            )
        }
    }
}
