package com.example.regenx.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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


@Composable
fun LoginScreen(navController: NavController, role: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var identifier by remember { mutableStateOf("") } // Email or Collector ID
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login as $role", fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
                    painterResource(id = R.drawable.visibility) // eye icon visible
                else
                    painterResource(id = R.drawable.visibility_off) // eye icon hidden

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = image, contentDescription = if (passwordVisible) "Hide password" else "Show password", modifier = Modifier.size(28.dp))
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (role == "COLLECTOR") {
                    // Convert Collector ID to the fake email used in signup
                    val loginEmail = "$identifier@collectors.regenx.com"

                    auth.signInWithEmailAndPassword(loginEmail, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("collectorDashboard")
                            } else {
                                println("Login failed: ${task.exception?.message}")
                            }
                        }

                } else {
                    // Email-based login for Residents and Officials
                    auth.signInWithEmailAndPassword(identifier, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                when (role) {
                                    "RESIDENT" -> navController.navigate("residentDashboard")
                                    "OFFICIAL" -> navController.navigate("officialDashboard")
                                }
                            } else {
                                println("Login failed: ${task.exception?.message}")
                            }
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
                fontSize = 18.sp, // Bigger text
                color = MaterialTheme.colorScheme.primary
            ),
            onClick = { navController.navigate("signup/$role") }
        )
    }
}
