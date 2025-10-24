//package com.example.regenx.screens.officials
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavController
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
//import java.text.SimpleDateFormat
//import java.util.*
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ViewComplaintsScreen(navController: NavController) {
//    val firestore = Firebase.firestore
//    var residentComplaints by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
//    var collectorComplaints by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
//
//    LaunchedEffect(Unit) {
//        firestore.collection("complaints").get()
//            .addOnSuccessListener { snapshot ->
//                val all = snapshot.documents.mapNotNull { doc ->
//                    val data = doc.data
//                    if (data != null) doc.id to data else null
//                }
//                residentComplaints = all.filter { it.second["userType"] == "resident" }
//                collectorComplaints = all.filter { it.second["userType"] == "collector" }
//            }
//    }
//
//    Scaffold(
//        topBar = { TopAppBar(title = { Text("All Complaints") }) }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(padding)
//                .padding(16.dp)
//        ) {
//            item {
//                Text("Resident Complaints", style = MaterialTheme.typography.titleMedium)
//                Spacer(Modifier.height(8.dp))
//            }
//
//            if (residentComplaints.isEmpty()) {
//                item { Text("No resident complaints") }
//            } else {
//                items(residentComplaints) { (id, comp) ->
//                    ComplaintItem(id, comp) {
//                        navController.navigate("complaintDetails/$id")
//                    }
//                }
//            }
//
//            item {
//                Spacer(Modifier.height(24.dp))
//                Text("Collector Complaints", style = MaterialTheme.typography.titleMedium)
//                Spacer(Modifier.height(8.dp))
//            }
//
//            if (collectorComplaints.isEmpty()) {
//                item { Text("No collector complaints") }
//            } else {
//                items(collectorComplaints) { (id, comp) ->
//                    ComplaintItem(id, comp) {
//                        navController.navigate("complaintDetails/$id")
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ComplaintItem(
//    id: String,
//    complaint: Map<String, Any>,
//    onClick: () -> Unit
//) {
//    val subject = complaint["subject"] as? String ?: "No subject"
//    val timestamp = complaint["timestamp"] as? Long ?: 0L
//    val date = if (timestamp > 0)
//        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
//    else "Unknown date"
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp)
//            .clickable { onClick() },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text(subject, style = MaterialTheme.typography.titleMedium)
//            Spacer(Modifier.height(4.dp))
//            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//        }
//    }
//}

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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

    // Data Loading Logic (remains the same)
    LaunchedEffect(Unit) {
        firestore.collection("complaints").get()
            .addOnSuccessListener { snapshot ->
                val all = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) doc.id to data else null
                }
                residentComplaints = all.filter { it.second["userType"] == "resident" }
                collectorComplaints = all.filter { it.second["userType"] == "collector" }
            }
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
                    Text("No $title found.")
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
    val date = if (timestamp > 0)
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    else "Unknown date"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(subject, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
