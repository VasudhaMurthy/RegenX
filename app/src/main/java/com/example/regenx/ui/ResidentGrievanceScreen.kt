//package com.example.regenx.ui
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun ResidentGrievanceScreen(
//    onSubmit: (String) -> Unit = {}
//) {
//    var description by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(text = "Submit Grievance", style = MaterialTheme.typography.headlineMedium)
//        Spacer(modifier = Modifier.height(24.dp))
//        OutlinedTextField(
//            value = description,
//            onValueChange = { description = it },
//            label = { Text("Describe issue...") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(
//            onClick = { onSubmit(description) },
//            modifier = Modifier.align(Alignment.End)
//        ) {
//            Text("Submit Complaint")
//        }
//    }
//}


package com.example.regenx.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun ResidentGrievanceScreen() {
    val context = LocalContext.current
    val auth = Firebase.auth
    var userId by remember { mutableStateOf(auth.currentUser?.uid ?: "") }
    var description by remember { mutableStateOf(TextFieldValue()) }

    // Attempt anonymous sign-in once
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            Log.d("AnonAuth", "Attempting anonymous sign-in")
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userId = auth.currentUser?.uid ?: ""
                        Log.d("AnonAuth", "Sign-in SUCCESS: $userId")
                        Toast.makeText(context, "Signed in: $userId", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("AnonAuth", "Sign-in FAILED", task.exception)
                        Toast.makeText(context, "Sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            userId = auth.currentUser?.uid ?: ""
            Log.d("AnonAuth", "Already signed in: $userId")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("UserID: $userId")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe issue...") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (userId.isBlank()) {
                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                Toast.makeText(context, "Would submit: ${description.text}", Toast.LENGTH_SHORT).show()
            },
            enabled = userId.isNotBlank()
        ) {
            Text("Submit Complaint")
        }
    }
}
