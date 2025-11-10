package com.example.regenx.screens.officials

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Needed for Color(0xFFE8F5E9)
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

// Enum to define the two tabs/views
enum class ComplaintTab { RESIDENT, COLLECTOR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewComplaintsScreen(navController: NavController) {
    val firestore = Firebase.firestore
    var residentComplaints by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
    var collectorComplaints by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
    var selectedTab by remember { mutableStateOf(ComplaintTab.RESIDENT) } // State for tab selection

    // 🌟 FIX: Use addSnapshotListener for real-time updates and correct filtering 🌟
    DisposableEffect(Unit) {
        val listener: ListenerRegistration = firestore.collection("complaints")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error logging here
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val all = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        if (data != null) doc.id to data else null
                    }

                    // 🌟 FIX: Filter based on the 'role' field, ensuring it matches RESIDENT/COLLECTOR 🌟
                    residentComplaints = all.filter {
                        (it.second["role"] as? String)?.uppercase() == "RESIDENT"
                    }
                    collectorComplaints = all.filter {
                        (it.second["role"] as? String)?.uppercase() == "COLLECTOR"
                    }
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Complaints") },
                // 1. Add Go Back button
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
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
            // 2. TabRow for switching between lists
            ComplaintTabRow(selectedTab) { tab ->
                selectedTab = tab
            }

            // 3. Conditional content based on the selected tab
            when (selectedTab) {
                ComplaintTab.RESIDENT -> ComplaintList(
                    complaints = residentComplaints,
                    title = "Resident Complaints",
                    navController = navController
                )
                ComplaintTab.COLLECTOR -> ComplaintList(
                    complaints = collectorComplaints,
                    title = "Collector Complaints",
                    navController = navController
                )
            }
        }
    }
}

// -------------------------------------------------------------------------
// COMPOSABLES FOR TABS AND LISTS
// -------------------------------------------------------------------------

@Composable
fun ComplaintTabRow(
    selectedTab: ComplaintTab,
    onTabSelected: (ComplaintTab) -> Unit
) {
    val titles = mapOf(
        ComplaintTab.RESIDENT to "Residents",
        ComplaintTab.COLLECTOR to "Collectors"
    )
    val selectedIndex = ComplaintTab.entries.indexOf(selectedTab)

    TabRow(selectedTabIndex = selectedIndex) {
        ComplaintTab.entries.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(tab) },
                text = { Text(titles[tab]!!) }
            )
        }
    }
}

@Composable
fun ComplaintList(
    complaints: List<Pair<String, Map<String, Any>>>,
    title: String,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        // Optional: Keep the title as a visual indicator, though the tab already states it
        item {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        if (complaints.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Check if the overall list is empty or just the filtered one
                    Text("No ${title.lowercase()} found.")
                }
            }
        } else {
            items(complaints, key = { it.first }) { (id, comp) ->
                ComplaintItem(id, comp) {
                    navController.navigate("complaintDetails/$id")
                }
            }
        }
    }
}

@Composable
fun ComplaintItem(
    id: String,
    complaint: Map<String, Any>,
    onClick: () -> Unit
) {
    val subject = complaint["subject"] as? String ?: "No subject"
    val timestamp = complaint["timestamp"] as? Long ?: 0L
    val status = complaint["status"] as? String ?: "pending" // Get status
    val date = if (timestamp > 0)
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    else "Unknown date"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        // Visual cue for status
        colors = CardDefaults.cardColors(
            containerColor = if (status == "resolved") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                // Display status next to the subject
                Text(
                    text = status.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    // 🌟 FIX: Use MaterialTheme.colorScheme.error 🌟
                    color = if (status == "resolved") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}