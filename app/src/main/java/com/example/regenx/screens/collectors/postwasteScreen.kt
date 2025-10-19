package com.example.regenx.screens.collectors

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostWasteScreen(navController: NavController) {
    var wasteType by remember { mutableStateOf(TextFieldValue("")) }
    var quantity by remember { mutableStateOf(TextFieldValue("")) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Post Waste for Sale") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Enter waste details",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = wasteType,
                onValueChange = { wasteType = it },
                label = { Text("Type of waste (e.g. Plastic, Paper, Metal)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity (kg)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val collectorId = auth.currentUser?.uid ?: "unknown"
                    val postData = hashMapOf(
                        "collectorId" to collectorId,
                        "wasteType" to wasteType.text,
                        "quantity" to quantity.text,
                        "notes" to notes.text,
                        "timestamp" to FieldValue.serverTimestamp()
                    )

                    db.collection("wasteForSale")
                        .add(postData)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Waste posted successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack() // Go back after posting
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to post waste: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post Waste for Sale")
            }
        }
    }
}
