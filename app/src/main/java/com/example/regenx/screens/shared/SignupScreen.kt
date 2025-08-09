package com.example.regenx.screens.shared

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.regenx.R
import com.example.regenx.ui.components.RoleButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val auth = Firebase.auth
    val firestore = Firebase.firestore

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "ReGenX",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.my_icon),
                contentDescription = "Signup Icon",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Sign Up as", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            Spacer(modifier = Modifier.height(32.dp))

            RoleButton("Resident", selectedRole) { selectedRole = "Resident" }
            Spacer(modifier = Modifier.height(16.dp))
            RoleButton("Garbage Collector", selectedRole) { selectedRole = "Garbage Collector" }
            Spacer(modifier = Modifier.height(16.dp))
            RoleButton("Official", selectedRole) { selectedRole = "Official" }

            selectedRole?.let { role ->
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isBlank() || email.isBlank() || password.length < 6) {
                            Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid ?: ""
                                    firestore.collection("users").document(userId)
                                        .set(
                                            mapOf(
                                                "name" to name,
                                                "email" to email,
                                                "role" to role
                                            )
                                        )
                                        .addOnSuccessListener {
                                            isLoading = false
                                            Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                            // Role-based navigation
                                            when (role) {
                                                "Resident" -> navController.navigate("residentDashboard") {
                                                    popUpTo("signup") { inclusive = true }
                                                }
                                                "Garbage Collector" -> navController.navigate("collectorDashboard") {
                                                    popUpTo("signup") { inclusive = true }
                                                }
                                                "Official" -> navController.navigate("officialDashboard") {
                                                    popUpTo("signup") { inclusive = true }
                                                }
                                                else -> navController.navigate("home")
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Error saving data: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (isLoading) "Creating account..." else "Sign Up")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SignupScreen(navController = rememberNavController())
}
