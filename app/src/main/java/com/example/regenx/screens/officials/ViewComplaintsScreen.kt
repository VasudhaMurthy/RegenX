package com.example.regenx.screens.officials

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class Complaint(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "Pending",
    val timestamp: Timestamp? = null
)

@Composable
fun ViewComplaintsScreen(navController: NavController) {
    val context = LocalContext.current
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val firestore = Firebase.firestore

    // Load complaints from Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("complaints")
                .orderBy("timestamp")
                .get()
                .await()
            complaints = snapshot.documents.map { doc ->
                Complaint(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    category = doc.getString("category") ?: "",
                    status = doc.getString("status") ?: "Pending",
                    timestamp = doc.getTimestamp("timestamp")
                )
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading complaints: ${e.message}", Toast.LENGTH_LONG)
                .show()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        // Show loading indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // ✅ ensures heading is below notch
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Heading
            Text(
                text = "Complaints",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(complaints) { complaint ->
                    ComplaintItem(complaint = complaint)
                }
                // Bottom space
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun ComplaintItem(complaint: Complaint) {
    var status by remember { mutableStateOf(complaint.status) }
    val statusOptions = listOf("Pending", "In Progress", "Resolved")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                complaint.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                complaint.description,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    complaint.category,
                    style = MaterialTheme.typography.labelMedium
                )

                // Simple status update buttons
                DropdownMenuBox(
                    status = status,
                    statusOptions = statusOptions,
                    onStatusChange = { newStatus ->
                        status = newStatus
                        Firebase.firestore.collection("complaints")
                            .document(complaint.id)
                            .update("status", newStatus)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(
    status: String,
    statusOptions: List<String>,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = status,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .width(150.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statusOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onStatusChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
