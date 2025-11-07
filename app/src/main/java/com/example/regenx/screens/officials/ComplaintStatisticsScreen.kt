package com.example.regenx.screens.officials

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Data class to hold the aggregated statistics
data class ComplaintStats(
    val totalComplaints: Int = 0,
    val resolvedComplaints: Int = 0,
    val pendingComplaints: Int = 0,
    val residentComplaints: Int = 0,
    val collectorComplaints: Int = 0,
    val complaintsBySubject: Map<String, Int> = emptyMap()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintStatisticsScreen(navController: NavController) {
    val firestore = Firebase.firestore
    var stats by remember { mutableStateOf(ComplaintStats()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        firestore.collection("complaints").get()
            .addOnSuccessListener { snapshot ->
                val allComplaints = snapshot.documents.mapNotNull { it.data }
                val total = allComplaints.size

                val resolved = allComplaints.count { (it["status"] as? String) == "Resolved" }
                val pending = total - resolved
                val resident = allComplaints.count { (it["userType"] as? String) == "resident" }
                val collector = allComplaints.count { (it["userType"] as? String) == "collector" }

                val complaintsBySubjectMap = allComplaints
                    .mapNotNull { it["subject"] as? String }
                    .groupingBy { it }
                    .eachCount()

                stats = ComplaintStats(
                    totalComplaints = total,
                    resolvedComplaints = resolved,
                    pendingComplaints = pending,
                    residentComplaints = resident,
                    collectorComplaints = collector,
                    complaintsBySubject = complaintsBySubjectMap
                )
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Statistics") },
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Overall Performance", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    // Key Metrics: 3 cards, weighted 1/3 each
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(label = "Total", value = stats.totalComplaints, color = MaterialTheme.colorScheme.primary)
                        StatCard(label = "Resolved", value = stats.resolvedComplaints, color = Color(0xFF4CAF50))
                        StatCard(label = "Pending", value = stats.pendingComplaints, color = Color(0xFFFF9800))
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Complaints by Source", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))

                    // Source Metrics: 2 cards, weighted 1/2 each
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard(label = "From Residents", value = stats.residentComplaints, color = MaterialTheme.colorScheme.secondary)
                        StatCard(label = "From Collectors", value = stats.collectorComplaints, color = MaterialTheme.colorScheme.tertiary)
                        // 🌟 REMOVED THE UNBALANCED SPACER HERE 🌟
                    }
                }

                // 🌟 NEW SECTION: SUBJECT BREAKDOWN 🌟
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Breakdown by Complaint Subject", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))

                    SubjectBreakdownPlaceholder(stats.complaintsBySubject)
                }
            }
        }
    }
}

// Reusable composable for displaying a single statistic
@Composable
fun RowScope.StatCard(label: String, value: Int, color: Color) {
    Card(
        // The .weight(1f) ensures the card takes equal space in the parent Row.
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Placeholder for the Subject Breakdown chart/list
@Composable
fun SubjectBreakdownPlaceholder(data: Map<String, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Complaint Subjects Count", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (data.isEmpty()) {
                Text("No categorized complaint data available.", style = MaterialTheme.typography.bodyMedium)
            } else {
                data.entries.sortedByDescending { it.value }.forEach { (subject, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(subject, style = MaterialTheme.typography.bodyLarge)
                        Text(count.toString(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Divider()
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp).padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    Text("Visualization requires charting library", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}