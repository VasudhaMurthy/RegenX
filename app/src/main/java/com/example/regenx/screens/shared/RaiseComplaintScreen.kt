package com.example.regenx.screens.shared

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseComplaintScreen(navController: NavController) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Waste Collection") }
    val categories = listOf("Waste Collection", "Illegal Dumping", "Recycling Issues", "Other")
    var isLoading by remember { mutableStateOf(false) }
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Report a Complaint", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Complaint Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 5
        )
        Spacer(Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = false,
            onExpandedChange = {}
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) }
            )
            ExposedDropdownMenu(
                expanded = false,
                onDismissRequest = {}
            ) {
                categories.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = { category = item }
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isBlank() || description.isBlank()) {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                val complaint = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "status" to "Pending",
                    "userId" to auth.currentUser?.uid,
                    "timestamp" to Timestamp.now(),
                    "userRole" to "Resident/Collector" // Update based on actual user role
                )
                firestore.collection("complaints")
                    .add(complaint)
                    .addOnSuccessListener {
                        isLoading = false
                        Toast.makeText(context, "Complaint submitted!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Submitting..." else "Submit Complaint")
        }
    }
}
