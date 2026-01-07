package com.example.regenx.screens.officials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// -------------------- DATA MODEL --------------------
data class GarbageStats(
    val totalTrips: Int = 0,
    val totalGarbageKg: Double = 0.0,
    val activeTrucks: Int = 0,
    val completedTrips: Int = 0
)

// -------------------- SCREEN --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarbageAnalyticsScreen(navController: NavController) {

    val firestore = Firebase.firestore
    var stats by remember { mutableStateOf(GarbageStats()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true

        firestore.collection("trip_reports")
            .get()
            .addOnSuccessListener { snapshot ->

                val reports = snapshot.documents.mapNotNull { it.data }

                val totalTrips = reports.size
                val completedTrips = reports.count { it["status"] == "completed" }

                val totalKg = reports.sumOf {
                    (it["garbageKg"] as? Number)?.toDouble() ?: 0.0
                }

                firestore.collection("trucks")
                    .get()
                    .addOnSuccessListener { truckSnap ->
                        val active = truckSnap.documents.count {
                            it.getString("status") == "En Route"
                        }

                        stats = GarbageStats(
                            totalTrips = totalTrips,
                            totalGarbageKg = totalKg,
                            activeTrucks = active,
                            completedTrips = completedTrips
                        )
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Garbage Analytics & Reports") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    Text(
                        "Overall Collection Metrics",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Total Trips",
                            value = stats.totalTrips.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            label = "Completed Trips",
                            value = stats.completedTrips.toString(),
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            label = "Active Trucks",
                            value = stats.activeTrucks.toString(),
                            color = Color(0xFF2196F3)
                        )
                        StatCard(
                            label = "Garbage Collected (kg)",
                            value = String.format("%.2f", stats.totalGarbageKg),
                            color = Color(0xFF9C27B0)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalyticsNote()
                }
            }
        }
    }
}

// -------------------- STAT CARD (SINGLE VERSION) --------------------
@Composable
fun RowScope.StatCard(
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------- INFO SECTION --------------------
@Composable
fun AnalyticsNote() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "These analytics are derived from completed trip reports and live truck statuses. Advanced charts will be added after data stabilization.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
