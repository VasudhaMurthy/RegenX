//package com.example.regenx
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.core.content.ContextCompat
//import com.example.regenx.ui.theme.RegenXTheme
//import com.google.firebase.messaging.FirebaseMessaging
//
//class MainActivity : ComponentActivity() {
//
//    // Register the permission launcher for Android 13+ notification permission
//    private val requestPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted: Boolean ->
//        if (isGranted) {
//            Log.d("Permission", "Notification permission granted")
//        } else {
//            Log.w("Permission", "Notification permission denied")
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        setContent {
//            RegenXTheme {
//                // Get Compose context for Toasts
//                val context = LocalContext.current
//
//                // Fetch and log FCM token after composition
//                LaunchedEffect(Unit) {
//                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val token = task.result
//                            Log.d("FCM token", token ?: "Token is null")
//                            Toast.makeText(context, "FCM token: $token", Toast.LENGTH_LONG).show()
//                        } else {
//                            Log.w("FCM token", "Fetching FCM registration token failed", task.exception)
//                            Toast.makeText(context, "Fetching FCM token failed", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//
//        // Request notification permission if needed (Android 13+)
//        askNotificationPermission()
//    }
//
//    private fun askNotificationPermission() {
//        // Only needed for Android 13+ (API 33+)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            when {
//                ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) == PackageManager.PERMISSION_GRANTED -> {
//                    // Permission already granted, do nothing
//                }
//                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
//                    // Optionally show rationale, then request permission
//                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                }
//                else -> {
//                    // Directly request permission
//                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    RegenXTheme {
//        Greeting("Android")
//    }
//}


package com.example.regenx

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.regenx.ui.theme.RegenXTheme
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) Log.d("Permission", "Notification permission granted")
        else Log.w("Permission", "Notification permission denied")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RegenXTheme {
                val context = LocalContext.current
                var currentScreen by remember { mutableStateOf(Screen.Resident) }

                // Notification initialization
                LaunchedEffect(Unit) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            Log.d("FCM token", token ?: "Token is null")
                            Toast.makeText(context, "FCM token: $token", Toast.LENGTH_LONG).show()
                        } else {
                            Log.w("FCM token", "Fetching FCM registration token failed", task.exception)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("RegenX Grievance System") },
                            actions = {
                                TextButton(onClick = {
                                    currentScreen = if (currentScreen == Screen.Resident) Screen.Official else Screen.Resident
                                }) {
                                    Text(if (currentScreen == Screen.Resident) "Officials" else "Residents")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentScreen) {
                            Screen.Resident -> ResidentGrievanceScreen()
                            Screen.Official -> OfficialDashboardScreen()
                        }
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            handleNotificationPermission()
        }
    }

    private fun handleNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {}
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

enum class Screen { Resident, Official }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentGrievanceScreen() {
    val context = LocalContext.current
    var description by remember { mutableStateOf(TextFieldValue()) }
    val auth = Firebase.auth
    val firestore = Firebase.firestore
    var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }
    var isSigningIn by remember { mutableStateOf(false) }

    // Handle authentication state
    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            isSigningIn = true
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    isSigningIn = false
                    if (task.isSuccessful) {
                        isSignedIn = true
                        Log.d("Auth", "Anonymous sign-in success: ${auth.currentUser?.uid}")
                    } else {
                        Log.e("Auth", "Sign-in failed", task.exception)
                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Enter grievance") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSigningIn
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val user = auth.currentUser
                if (user == null) {
                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (description.text.isBlank()) {
                    Toast.makeText(context, "Enter description", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                firestore.collection("grievances").add(
                    hashMapOf(
                        "type" to "resident",
                        "userId" to user.uid,
                        "description" to description.text,
                        "status" to "PENDING",
                        "timestamp" to Timestamp.now()
                    )
                ).addOnSuccessListener {
                    description = TextFieldValue("")
                    Toast.makeText(context, "Grievance submitted", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(context, "Submission failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = isSignedIn && !isSigningIn
        ) {
            Text(if (isSigningIn) "Signing in..." else "Submit")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialDashboardScreen() {
    val firestore = Firebase.firestore
    var grievances by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {
        firestore.collection("grievances")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed", error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply {
                        put("id", doc.id)
                    }
                } ?: emptyList()

                grievances = items
            }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(grievances) { grievance ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID: ${grievance["id"] ?: ""}")
                    Text("Description: ${grievance["description"] ?: ""}")
                    Text("Status: ${grievance["status"] ?: ""}")
                    Text("User: ${grievance["userId"] ?: ""}")
                }
            }
        }
    }
}
