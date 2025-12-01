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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.shape.RoundedCornerShape // Added for shapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, role: String) {
    val auth = FirebaseAuth.getInstance()
    // val firestore = FirebaseFirestore.getInstance() // Firestore instance is unused in the current logic

    var identifier by remember { mutableStateOf("") } // Email or Collector ID
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val identifierLabel = when(role.uppercase()) {
        "COLLECTOR" -> "Collector ID"
        "OFFICIAL" -> "Official Email"
        else -> "Email Address"
    }

    val displayName = role.replaceFirstChar { it.uppercase() }.replace("_", " ")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Login as $displayName",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp), // Increased horizontal padding
            verticalArrangement = Arrangement.Top, // Align content to the top
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Large space from the top bar
            Spacer(modifier = Modifier.height(64.dp))

            // --- Title/Instruction ---
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Please enter your credentials to access the $displayName portal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // --- Identifier Field ---
            OutlinedTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = { Text(identifierLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // Rounded corners for fields
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Field ---
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // Rounded corners for fields
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            // Apply tinting for theme compatibility
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Login Button ---
            Button(
                onClick = {
                    // Start of existing logic
                    val loginEmail = when (role.uppercase()) {
                        "COLLECTOR" -> "$identifier@collectors.regenx.com"
                        "OFFICIAL" -> "$identifier@officials.regenx.com" // Assuming structure for Official
                        else -> identifier
                    }

                    if (loginEmail.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please enter both credentials.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    auth.signInWithEmailAndPassword(loginEmail, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Navigate according to role
                                val destination = when (role.uppercase()) {
                                    "RESIDENT" -> "residentDashboard"
                                    "SCRAP_BUYER" -> "scrapDashboard"
                                    "COLLECTOR" -> "collectorDashboard"
                                    "OFFICIAL" -> "officialDashboard"
                                    else -> null
                                }

                                destination?.let {
                                    navController.navigate(it) {
                                        popUpTo("login/$role") { inclusive = true }
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    // End of existing logic
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp), // Rounded button
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Sign Up Link ---
            ClickableText(
                text = AnnotatedString("Not a user? Sign Up"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                ),
                onClick = { navController.navigate("signup/$role") }
            )
        }
    }
}