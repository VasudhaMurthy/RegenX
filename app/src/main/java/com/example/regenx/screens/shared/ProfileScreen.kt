package com.example.regenx.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.regenx.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Fetch user data from Firestore
    LaunchedEffect(uid) {
        if (uid != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()
                name = doc.getString("name") ?: ""
                email = doc.getString("email") ?: ""
                phone = doc.getString("phone") ?: ""
                loading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load profile: ${e.localizedMessage}"
                loading = false
            }
        } else {
            errorMessage = "User not logged in."
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile image
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )

                    // Editable fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (uid != null) {
                                saving = true
                                errorMessage = null
                                successMessage = null
                                // Save changes to Firestore
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update(
                                        mapOf(
                                            "name" to name,
                                            "email" to email,
                                            "phone" to phone
                                        )
                                    )
                                    .addOnSuccessListener {
                                        saving = false
                                        successMessage = "Profile updated successfully!"
                                    }
                                    .addOnFailureListener { e ->
                                        saving = false
                                        errorMessage = "Failed to update profile: ${e.localizedMessage}"
                                    }
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Save")
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (successMessage != null) {
                        Text(
                            text = successMessage!!,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
