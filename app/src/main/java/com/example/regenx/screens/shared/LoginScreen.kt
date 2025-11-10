package com.example.regenx.screens.shared

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.regenx.R
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, role: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var identifier by remember { mutableStateOf("") } // Email or Collector ID
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Use Scaffold to implement the TopAppBar with a navigation button
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login as $role") },
                navigationIcon = {
                    // Back button functionality
                    IconButton(onClick = {
                        // Navigate back using the NavController
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Main content column, applied padding from the Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from TopAppBar
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title is now in the TopAppBar, but we can keep a spacer for visual layout
            Spacer(modifier = Modifier.height(16.dp))

            val identifierLabel = when(role) {
                "COLLECTOR" -> "Collector ID"
                else -> "Email"
            }

            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text(identifierLabel) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.visibility)
                    else
                        painterResource(id = R.drawable.visibility_off)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = if (passwordVisible) "Hide password" else "Show password", modifier = Modifier.size(28.dp))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Determine email based on role
                    val loginEmail = if (role.uppercase() == "COLLECTOR") "$identifier@collectors.regenx.com" else identifier

                    auth.signInWithEmailAndPassword(loginEmail, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Navigate according to role
                                when (role.uppercase()) {
                                    "RESIDENT" -> navController.navigate("residentDashboard") {
                                        popUpTo("login/$role") { inclusive = true }
                                    }
                                    "SCRAP_BUYER" -> navController.navigate("scrapDashboard") {
                                        popUpTo("login/$role") { inclusive = true }
                                    }
                                    "COLLECTOR" -> navController.navigate("collectorDashboard") {
                                        popUpTo("login/$role") { inclusive = true }
                                    }
                                    "OFFICIAL" -> navController.navigate("officialDashboard") {
                                        popUpTo("login/$role") { inclusive = true }
                                    }
                                    else -> {}
                                }
                            } else {
                                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }




            Spacer(modifier = Modifier.height(12.dp))

            ClickableText(
                text = AnnotatedString("Not a user? Sign Up"),
                style = LocalTextStyle.current.copy(
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                onClick = { navController.navigate("signup/$role") }
            )
        }
    }
}