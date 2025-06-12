package com.example.regenx.screens.officials

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class Complaint(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val status: String = "Pending",
    val timestamp: com.google.firebase.Timestamp? = null
)

@Composable
fun ViewComplaintsScreen(navController: NavController) {
    val context = LocalContext.current
    var complaints by remember { mutableStateOf<List<Complaint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val firestore = Firebase.firestore

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
            isLoading = false
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading complaints: ${e.message}", Toast.LENGTH_LONG).show()
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(complaints) { complaint ->
                ComplaintItem(complaint = complaint)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintItem(complaint: Complaint) {
    var status by remember { mutableStateOf(complaint.status) }
    val statusOptions = listOf("Pending", "In Progress", "Resolved")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(complaint.title, style = MaterialTheme.typography.titleLarge)
            Text(complaint.description, modifier = Modifier.padding(vertical = 8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(complaint.category, style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.width(150.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
                    )
                    ExposedDropdownMenu(
                        expanded = false,
                        onDismissRequest = {}
                    ) {
                        statusOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    status = option
                                    Firebase.firestore.collection("complaints")
                                        .document(complaint.id)
                                        .update("status", option)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
