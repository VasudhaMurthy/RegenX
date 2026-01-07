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
