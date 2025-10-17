package com.example.regenx.screens.officials

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplaintDetailsScreen(navController: NavController, complaintId: String) {
    val firestore = Firebase.firestore
    var complaint by remember { mutableStateOf<Map<String, Any>?>(null) }

    // ✅ Use DisposableEffect to manage Firestore listener lifecycle
    DisposableEffect(complaintId) {
        val listener: ListenerRegistration = firestore.collection("complaints").document(complaintId)
            .addSnapshotListener { doc, _ ->
                complaint = doc?.data
            }

        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complaint Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        complaint?.let { comp ->
            val subject = comp["subject"] as? String ?: "No subject"
            val description = comp["description"] as? String ?: "No description"
            val photoUrl = comp["photoUrl"] as? String ?: ""
            val userType = comp["userType"] as? String ?: "Unknown"
            val status = comp["status"] as? String ?: "pending"
            val timestamp = comp["timestamp"] as? Long ?: 0L
            val date = if (timestamp > 0)
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))
            else "Unknown date"

            var localStatus by remember { mutableStateOf(status) }
            LaunchedEffect(status) { localStatus = status }

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(subject, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text("Filed by: ${userType.replaceFirstChar { it.uppercase() }}")
                Text("Date: $date")
                Spacer(Modifier.height(12.dp))
                Text(description)
                Spacer(Modifier.height(12.dp))

                if (photoUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text("Status: ${localStatus.uppercase()}")

                Switch(
                    checked = localStatus == "resolved",
                    onCheckedChange = { isChecked ->
                        val newStatus = if (isChecked) "resolved" else "pending"
                        localStatus = newStatus
                        firestore.collection("complaints").document(complaintId)
                            .update("status", newStatus)
                    }
                )
            }
        } ?: Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
