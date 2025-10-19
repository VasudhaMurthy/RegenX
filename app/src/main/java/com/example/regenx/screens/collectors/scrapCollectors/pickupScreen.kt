package com.example.regenx.screens.collectors.scrapCollectors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPickupRequestsScreen() {
    var wasteList by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    val db = FirebaseFirestore.getInstance()

    DisposableEffect(Unit) {
        val listener: ListenerRegistration = db.collection("wasteForSale")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    wasteList = snapshot.documents.mapNotNull { it.data }
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nearby Pickup Requests") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wasteList) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Type: ${item["wasteType"] ?: "N/A"}", style = MaterialTheme.typography.titleMedium)
                        Text("Quantity: ${item["quantity"] ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Notes: ${item["notes"] ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (wasteList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No nearby pickup requests yet.")
                    }
                }
            }
        }
    }
}
