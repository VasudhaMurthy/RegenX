////package com.example.regenx
////
////import android.Manifest
////import android.content.pm.PackageManager
////import android.os.Build
////import android.os.Bundle
////import android.util.Log
////import android.widget.Toast
////import androidx.activity.ComponentActivity
////import androidx.activity.compose.setContent
////import androidx.activity.enableEdgeToEdge
////import androidx.activity.result.contract.ActivityResultContracts
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.lazy.LazyColumn
////import androidx.compose.foundation.lazy.items
////import androidx.compose.material3.*
////import androidx.compose.runtime.*
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.platform.LocalContext
////import androidx.compose.ui.text.input.TextFieldValue
////import androidx.compose.ui.unit.dp
////import androidx.core.content.ContextCompat
////import com.example.regenx.ui.theme.RegenXTheme
////import com.google.firebase.Timestamp
////import com.google.firebase.auth.ktx.auth
////import com.google.firebase.firestore.ktx.firestore
////import com.google.firebase.ktx.Firebase
////import com.google.firebase.messaging.FirebaseMessaging
////import androidx.compose.material3.ExperimentalMaterial3Api
////import androidx.compose.ui.Alignment
////
////class MainActivity : ComponentActivity() {
////
////    private val requestPermissionLauncher = registerForActivityResult(
////        ActivityResultContracts.RequestPermission()
////    ) { isGranted: Boolean ->
////        if (isGranted) Log.d("Permission", "Notification permission granted")
////        else Log.w("Permission", "Notification permission denied")
////    }
////
////    @OptIn(ExperimentalMaterial3Api::class)
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////
////        setContent {
////            RegenXTheme {
////                val context = LocalContext.current
////                var currentScreen by remember { mutableStateOf(Screen.Resident) }
////
////                // Notification initialization
////                LaunchedEffect(Unit) {
////                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
////                        if (task.isSuccessful) {
////                            val token = task.result
////                            Log.d("FCM token", token ?: "Token is null")
////                            Toast.makeText(context, "FCM token: $token", Toast.LENGTH_LONG).show()
////                        } else {
////                            Log.w("FCM token", "Fetching FCM registration token failed", task.exception)
////                        }
////                    }
////                }
////
////                Scaffold(
////                    modifier = Modifier.fillMaxSize(),
////                    topBar = {
////                        TopAppBar(
////                            title = { Text("RegenX Grievance System") },
////                            actions = {
////                                TextButton(onClick = {
////                                    currentScreen = if (currentScreen == Screen.Resident) Screen.Official else Screen.Resident
////                                }) {
////                                    Text(if (currentScreen == Screen.Resident) "Officials" else "Residents")
////                                }
////                            }
////                        )
////                    }
////                ) { innerPadding ->
////                    Box(modifier = Modifier.padding(innerPadding)) {
////                        when (currentScreen) {
////                            Screen.Resident -> ResidentGrievanceScreen()
////                            Screen.Official -> OfficialDashboardScreen()
////                        }
////                    }
////                }
////            }
////        }
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
////            handleNotificationPermission()
////        }
////    }
////
////    private fun handleNotificationPermission() {
////        when {
////            ContextCompat.checkSelfPermission(
////                this,
////                Manifest.permission.POST_NOTIFICATIONS
////            ) == PackageManager.PERMISSION_GRANTED -> {}
////            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
////                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
////            }
////            else -> {
////                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
////            }
////        }
////    }
////}
////
////enum class Screen { Resident, Official }
////
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun ResidentGrievanceScreen() {
////    val context = LocalContext.current
////    var description by remember { mutableStateOf(TextFieldValue()) }
////    val auth = Firebase.auth
////    val firestore = Firebase.firestore
////    var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }
////    var isSigningIn by remember { mutableStateOf(false) }
////
////    // Handle authentication state
////    LaunchedEffect(Unit) {
////        if (auth.currentUser == null) {
////            isSigningIn = true
////            auth.signInAnonymously()
////                .addOnCompleteListener { task ->
////                    isSigningIn = false
////                    if (task.isSuccessful) {
////                        isSignedIn = true
////                        Log.d("Auth", "Anonymous sign-in success: ${auth.currentUser?.uid}")
////                    } else {
////                        Log.e("Auth", "Sign-in failed", task.exception)
////                        Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
////                    }
////                }
////        }
////    }
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(16.dp),
////        verticalArrangement = Arrangement.Center
////    ) {
////        OutlinedTextField(
////            value = description,
////            onValueChange = { description = it },
////            label = { Text("Enter grievance") },
////            modifier = Modifier.fillMaxWidth(),
////            enabled = !isSigningIn
////        )
////        Spacer(modifier = Modifier.height(16.dp))
////        Button(
////            onClick = {
////                val user = auth.currentUser
////                if (user == null) {
////                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
////                    return@Button
////                }
////                if (description.text.isBlank()) {
////                    Toast.makeText(context, "Enter description", Toast.LENGTH_SHORT).show()
////                    return@Button
////                }
////
////                firestore.collection("grievances").add(
////                    hashMapOf(
////                        "type" to "resident",
////                        "userId" to user.uid,
////                        "description" to description.text,
////                        "status" to "PENDING",
////                        "timestamp" to Timestamp.now()
////                    )
////                ).addOnSuccessListener {
////                    description = TextFieldValue("")
////                    Toast.makeText(context, "Grievance submitted", Toast.LENGTH_SHORT).show()
////                }.addOnFailureListener {
////                    Toast.makeText(context, "Submission failed: ${it.message}", Toast.LENGTH_SHORT).show()
////                }
////            },
////            modifier = Modifier.align(Alignment.End),
////            enabled = isSignedIn && !isSigningIn
////        ) {
////            Text(if (isSigningIn) "Signing in..." else "Submit")
////        }
////    }
////}
////
////@OptIn(ExperimentalMaterial3Api::class)
////@Composable
////fun OfficialDashboardScreen() {
////    val firestore = Firebase.firestore
////    var grievances by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
////
////    LaunchedEffect(Unit) {
////        firestore.collection("grievances")
////            .addSnapshotListener { snapshot, error ->
////                if (error != null) {
////                    Log.e("Firestore", "Listen failed", error)
////                    return@addSnapshotListener
////                }
////
////                val items = snapshot?.documents?.mapNotNull { doc ->
////                    doc.data?.toMutableMap()?.apply {
////                        put("id", doc.id)
////                    }
////                } ?: emptyList()
////
////                grievances = items
////            }
////    }
////
////    LazyColumn(modifier = Modifier.padding(16.dp)) {
////        items(grievances) { grievance ->
////            Card(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(vertical = 4.dp),
////                elevation = CardDefaults.cardElevation(4.dp)
////            ) {
////                Column(modifier = Modifier.padding(16.dp)) {
////                    Text("ID: ${grievance["id"] ?: ""}")
////                    Text("Description: ${grievance["description"] ?: ""}")
////                    Text("Status: ${grievance["status"] ?: ""}")
////                    Text("User: ${grievance["userId"] ?: ""}")
////                }
////            }
////        }
////    }
////}
//
//
//
//package com.example.regenx
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.regenx.ui.theme.RegenXTheme
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.firestore.ktx.firestore
//import com.google.firebase.ktx.Firebase
////
////class MainActivity : ComponentActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContent {
////            val navController = rememberNavController()
////            MaterialTheme {
////                NavHost(navController = navController, startDestination = "signup") {
////                    composable("signup") { SignupScreen(navController) }
////                    composable("login") { LoginScreen(navController) }
////                    composable("home") { HomeScreen(navController) }
////                }
////            }
////        }
////    }
////}
////
////@Composable
////fun SignupScreen(navController: androidx.navigation.NavController) {
////    val context = LocalContext.current
////    var email by remember { mutableStateOf("") }
////    var password by remember { mutableStateOf("") }
////    val auth = Firebase.auth
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(24.dp),
////        verticalArrangement = Arrangement.Center
////    ) {
////        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)
////        Spacer(Modifier.height(24.dp))
////        OutlinedTextField(
////            value = email,
////            onValueChange = { email = it },
////            label = { Text("Email") },
////            modifier = Modifier.fillMaxWidth()
////        )
////        Spacer(Modifier.height(16.dp))
////        OutlinedTextField(
////            value = password,
////            onValueChange = { password = it },
////            label = { Text("Password") },
////            visualTransformation = PasswordVisualTransformation(),
////            modifier = Modifier.fillMaxWidth()
////        )
////        Spacer(Modifier.height(24.dp))
////        Button(
////            onClick = {
////                auth.createUserWithEmailAndPassword(email, password)
////                    .addOnCompleteListener { task ->
////                        if (task.isSuccessful) {
////                            Toast.makeText(context, "Signup successful! Please login.", Toast.LENGTH_SHORT).show()
////                            navController.navigate("login") {
////                                popUpTo("signup") { inclusive = true }
////                            }
////                        } else {
////                            Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
////                        }
////                    }
////            },
////            modifier = Modifier.fillMaxWidth()
////        ) { Text("Sign Up") }
////        Spacer(Modifier.height(8.dp))
////        TextButton(
////            onClick = { navController.navigate("login") }
////        ) { Text("Already have an account? Login") }
////    }
////}
////
////@Composable
////fun LoginScreen(navController: androidx.navigation.NavController) {
////    val context = LocalContext.current
////    var email by remember { mutableStateOf("") }
////    var password by remember { mutableStateOf("") }
////    val auth = Firebase.auth
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(24.dp),
////        verticalArrangement = Arrangement.Center
////    ) {
////        Text("Login", style = MaterialTheme.typography.headlineMedium)
////        Spacer(Modifier.height(24.dp))
////        OutlinedTextField(
////            value = email,
////            onValueChange = { email = it },
////            label = { Text("Email") },
////            modifier = Modifier.fillMaxWidth()
////        )
////        Spacer(Modifier.height(16.dp))
////        OutlinedTextField(
////            value = password,
////            onValueChange = { password = it },
////            label = { Text("Password") },
////            visualTransformation = PasswordVisualTransformation(),
////            modifier = Modifier.fillMaxWidth()
////        )
////        Spacer(Modifier.height(24.dp))
////        Button(
////            onClick = {
////                auth.signInWithEmailAndPassword(email, password)
////                    .addOnCompleteListener { task ->
////                        if (task.isSuccessful) {
////                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
////                            navController.navigate("home") {
////                                popUpTo("login") { inclusive = true }
////                            }
////                        } else {
////                            Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
////                        }
////                    }
////            },
////            modifier = Modifier.fillMaxWidth()
////        ) { Text("Login") }
////        Spacer(Modifier.height(8.dp))
////        TextButton(
////            onClick = { navController.navigate("signup") }
////        ) { Text("Don't have an account? Sign Up") }
////    }
////}
////
////@Composable
////fun HomeScreen(navController: androidx.navigation.NavController) {
////    val context = LocalContext.current
////    val auth = Firebase.auth
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .padding(24.dp),
////        verticalArrangement = Arrangement.Center
////    ) {
////        Text("Welcome to the Home Screen!", style = MaterialTheme.typography.headlineMedium)
////        Spacer(Modifier.height(24.dp))
////        Button(
////            onClick = {
////                auth.signOut()
////                navController.navigate("login") {
////                    popUpTo("home") { inclusive = true }
////                }
////            },
////            modifier = Modifier.fillMaxWidth()
////        ) { Text("Log Out") }
////    }
////}
//
//
//
//
//// MainActivity.kt
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            val navController = rememberNavController()
//            RegenXTheme {
//                NavHost(
//                    navController = navController,
//                    startDestination = "home"
//                ) {
//                    composable("home") { HomeScreen(navController) }
//                    composable("authChoice") { AuthChoiceScreen(navController) }
//                    composable("userType/{authType}") { backStackEntry ->
//                        val authType = backStackEntry.arguments?.getString("authType")
//                        UserTypeScreen(navController, authType ?: "login")
//                    }
//                    composable("login/{userType}") { backStackEntry ->
//                        val userType = backStackEntry.arguments?.getString("userType")
//                        LoginScreen(navController, userType ?: "resident")
//                    }
//                    composable("signup/{userType}") { backStackEntry ->
//                        val userType = backStackEntry.arguments?.getString("userType")
//                        SignupScreen(navController, userType ?: "resident")
//                    }
//                    composable("dashboard/{userType}") { backStackEntry ->
//                        val userType = backStackEntry.arguments?.getString("userType")
//                        DashboardScreen(userType ?: "resident")
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HomeScreen(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.app_logo),
//            contentDescription = "App Logo",
//            modifier = Modifier.size(200.dp)
//        )
//        Spacer(modifier = Modifier.height(40.dp))
//        Button(
//            onClick = { navController.navigate("authChoice") },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Get Started", fontSize = 18.sp)
//        }
//    }
//}
//
//@Composable
//fun AuthChoiceScreen(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Button(
//            onClick = { navController.navigate("userType/login") },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Login", fontSize = 18.sp)
//        }
//        Spacer(modifier = Modifier.height(16.dp))
//        OutlinedButton(
//            onClick = { navController.navigate("userType/signup") },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Sign Up", fontSize = 18.sp)
//        }
//    }
//}
//
//@Composable
//fun UserTypeScreen(navController: NavController, authType: String) {
//    val context = LocalContext.current
//    val userTypes = listOf("Resident", "Collector", "Official")
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = "Select Your Role",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 32.dp)
//        )
//
//        userTypes.forEach { userType ->
//            OutlinedButton(
//                onClick = {
//                    val route = when(authType) {
//                        "login" -> "login/${userType.lowercase()}"
//                        else -> "signup/${userType.lowercase()}"
//                    }
//                    navController.navigate(route)
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Text(userType, fontSize = 16.sp)
//            }
//        }
//    }
//}
//
//@Composable
//fun LoginScreen(navController: NavController, userType: String) {
//    val context = LocalContext.current
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    val auth = Firebase.auth
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = "Login as $userType",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 32.dp)
//        )
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(32.dp))
//        Button(
//            onClick = {
//                auth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            navController.navigate("dashboard/$userType") {
//                                popUpTo("home") { inclusive = true }
//                            }
//                        } else {
//                            Toast.makeText(
//                                context,
//                                "Login failed: ${task.exception?.message}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Login")
//        }
//    }
//}
//
//@Composable
//fun SignupScreen(navController: NavController, userType: String) {
//    val context = LocalContext.current
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    val auth = Firebase.auth
//    val firestore = Firebase.firestore
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center
//    ) {
//        Text(
//            text = "Sign Up as $userType",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(bottom = 32.dp)
//        )
//
//        OutlinedTextField(
//            value = email,
//            onValueChange = { email = it },
//            label = { Text("Email") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Password") },
//            visualTransformation = PasswordVisualTransformation(),
//            modifier = Modifier.fillMaxWidth()
//        )
//        Spacer(modifier = Modifier.height(32.dp))
//        Button(
//            onClick = {
//                auth.createUserWithEmailAndPassword(email, password)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            // Save user type to Firestore
//                            val userId = auth.currentUser?.uid ?: ""
//                            firestore.collection("users").document(userId)
//                                .set(mapOf(
//                                    "email" to email,
//                                    "userType" to userType
//                                ))
//                                .addOnSuccessListener {
//                                    navController.navigate("dashboard/$userType") {
//                                        popUpTo("home") { inclusive = true }
//                                    }
//                                }
//                        } else {
//                            Toast.makeText(
//                                context,
//                                "Signup failed: ${task.exception?.message}",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                    }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Sign Up")
//        }
//    }
//}
//
//@Composable
//fun DashboardScreen(userType: String) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = "Welcome $userType!",
//            style = MaterialTheme.typography.headlineLarge
//        )
//        // Add role-specific content here
//    }
//}


package com.example.regenx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.regenx.ui.theme.RegenXTheme
import com.example.regenx.navigation.AppNavGraph
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Ensure Firebase is initialized before any Firestore or Storage call
        try {
            FirebaseApp.initializeApp(this)

            // Optional but recommended for stability in debug
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            Firebase.firestore.firestoreSettings = settings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            RegenXTheme {
                AppNavGraph()
            }
        }
    }
}

