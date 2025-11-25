package com.example.regenx.screens.collectors

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostWasteScreen(navController: NavController) {
    // STATE VARIABLES
    var wasteType by remember { mutableStateOf(TextFieldValue("")) }
    var quantity by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) } // <-- NEW FIELD
    var notes by remember { mutableStateOf(TextFieldValue("")) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    // Categories for dropdown
    val categories = listOf(
        "Hard Plastic",
        "Glass - White",
        "Glass - Green",
        "Glass - Brown",
        "Paper",
        "E-waste",
        "Metal"
    )
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) } // default
    var submitting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📝 Post Waste for Sale") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Use the Material Theme background color
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp) // Increased horizontal padding
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Specify the details for pickup",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            // --- 1. Waste Type ---
            OutlinedTextField(
                value = wasteType,
                onValueChange = { wasteType = it },
                label = { Text("Waste Type (e.g., PET Bottles, Old Newspapers)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )


            // --- 2. Quantity ---
            OutlinedTextField(
                value = quantity,
                onValueChange = {
                    if (it.text.isEmpty() || it.text.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it
                },
                label = { Text("Quantity (e.g., 5.5 kg)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )


            // --- 3. Category Dropdown ---
            CategoryDropdown(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            // --- 4. Pickup Address (New Field) ---
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Pickup Address") },
                placeholder = { Text("Street, City, Postal Code") },
                trailingIcon = {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.clickable {
                            // In a real app, this would open a map picker.
                            // For this demo, we just show a toast.
                            Toast.makeText(context, "Location picker coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // --- 5. Additional Notes ---
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes (e.g., 'Behind the gate', 'Call before arrival')") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 6. Post Button ---
            Button(
                onClick = {
                    // Basic validation
                    if (wasteType.text.isBlank() || quantity.text.isBlank() || address.text.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields (Type, Quantity, Address)", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (quantity.text.toDoubleOrNull() == null || quantity.text.toDouble() <= 0) {
                        Toast.makeText(context, "Quantity must be a valid number greater than zero", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val collectorId = auth.currentUser?.uid ?: run {
                        Toast.makeText(context, "Authentication required. Please sign in.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (submitting) return@Button
                    submitting = true

                    try {
                        // Prepare data for waste doc
                        val wasteDocRef = db.collection("wasteForSale").document() // auto-id
                        val wasteData = hashMapOf(
                            "collectorId" to collectorId,
                            "wasteType" to wasteType.text.trim(),
                            "quantity" to quantity.text.trim(),
                            "notes" to notes.text.trim(),
                            "category" to selectedCategory,
                            "address" to address.text.trim(), // <-- NEW FIELD ADDED HERE
                            "status" to "available", // Initial status
                            "timestamp" to FieldValue.serverTimestamp()
                        )

                        // Prepare collector upsert data (merge)
                        val collectorRef = db.collection("collectors").document(collectorId)
                        val collectorUpsert = hashMapOf<String, Any>(
                            "displayName" to (auth.currentUser?.displayName ?: "Collector"),
                            "lastPostedAt" to FieldValue.serverTimestamp()
                        )

                        // Use a batch so both writes happen together atomically
                        val batch = db.batch()
                        batch.set(wasteDocRef, wasteData)
                        // Merge prevents overwriting existing profile data
                        batch.set(collectorRef, collectorUpsert, SetOptions.merge())

                        batch.commit()
                            .addOnSuccessListener {
                                submitting = false
                                Toast.makeText(context, "✅ Waste posted successfully! Listing is now available.", Toast.LENGTH_LONG).show()
                                // Clear fields and navigate back
                                wasteType = TextFieldValue("")
                                quantity = TextFieldValue("")
                                address = TextFieldValue("")
                                notes = TextFieldValue("")
                                navController.popBackStack()
                            }
                            .addOnFailureListener { ex ->
                                submitting = false
                                Toast.makeText(context, "❌ Failed to post waste: ${ex.message}", Toast.LENGTH_LONG).show()
                            }

                    } catch (e: Exception) {
                        submitting = false
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !submitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (submitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Posting...")
                } else {
                    Text(
                        "Post Waste for Sale",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp)) // Extra space at bottom
        }
    }
}

/**
 * Custom Composable for the Category Dropdown to improve visual appearance.
 */
@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f, label = "dropdown_icon_rotation")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Waste Category",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedCard(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true },
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Select Category",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            // Menu should align with the width of the card
            modifier = Modifier
                .width(IntrinsicSize.Max) // Ensures menu matches content width
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = {
                        onCategorySelected(cat)
                        expanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}