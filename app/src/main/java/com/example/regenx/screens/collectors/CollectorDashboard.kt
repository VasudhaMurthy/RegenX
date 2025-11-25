//package com.example.regenx.screens.collectors
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.util.Log
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import androidx.navigation.NavController
//import com.example.regenx.R
//import com.example.regenx.backend.CollectorLocationService
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CollectorDashboard(navController: NavController) {
//    val context = LocalContext.current
//
//    // 🌟 STATE: Controls the button text and service state
//    var isJourneyActive by remember { mutableStateOf(false) }
//
//    // Fetch collector's name
//    var collectorName by remember { mutableStateOf("Collector") }
//    LaunchedEffect(Unit) {
//        val uid = FirebaseAuth.getInstance().currentUser?.uid
//        uid?.let {
//            FirebaseFirestore.getInstance().collection("users")
//                .document(it)
//                .get()
//                .addOnSuccessListener { document ->
//                    val firstName = document.getString("firstName")
//                    val lastName = document.getString("lastName")
//                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
//                        collectorName = "$firstName $lastName"
//                    }
//                }
//        }
//        // NOTE: No auto-start logic here. It's clean.
//    }
//
//    // Location permissions
//    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//        arrayOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_BACKGROUND_LOCATION
//        )
//    } else {
//        arrayOf(
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION
//        )
//    }
//
//    // Function to START the location service
//    fun startLocationService() {
//        val intent = Intent(context, CollectorLocationService::class.java)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ContextCompat.startForegroundService(context, intent)
//        } else {
//            context.startService(intent)
//        }
//        isJourneyActive = true
//        Log.d("CollectorDashboard", "Service started. Journey is active.")
//    }
//
//    // Function to STOP the location service
//    fun stopLocationService() {
//        val intent = Intent(context, CollectorLocationService::class.java)
//        context.stopService(intent)
//        isJourneyActive = false
//        Log.d("CollectorDashboard", "Service stopped. Journey is inactive.")
//    }
//
//    // Permission request launcher
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions()
//    ) { perms ->
//        val allGranted = perms.values.all { it }
//        if (allGranted) {
//            Log.d("CollectorDashboard", "✅ Permissions granted.")
//            startLocationService() // Start service if permissions are granted
//        } else {
//            Log.e("CollectorDashboard", "❌ Location permissions denied")
//        }
//    }
//
//    // 🌟 CORE LOGIC: Toggles service and handles permission requests
//    fun handleJourneyToggle() {
//        if (isJourneyActive) {
//            stopLocationService()
//            return
//        }
//
//        // Check if permissions are granted
//        val missingPermissions = locationPermissions.filter {
//            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
//        }
//
//        if (missingPermissions.isEmpty()) {
//            // Permissions are already granted, start the service immediately
//            startLocationService()
//        } else {
//            // Permissions are missing, request them via the launcher
//            permissionLauncher.launch(locationPermissions)
//        }
//    }
//
//
//    // Reusable FeatureCard composable
//    @Composable
//    fun FeatureCard(
//        title: String,
//        description: String,
//        iconId: Int,
//        gradientColors: List<Color>,
//        onClick: () -> Unit
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(130.dp)
//                .clickable { onClick() },
//            shape = RoundedCornerShape(16.dp),
//            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .background(brush = Brush.linearGradient(colors = gradientColors))
//                    .fillMaxSize()
//                    .padding(16.dp)
//            ) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        painter = painterResource(iconId),
//                        contentDescription = null,
//                        tint = Color.White,
//                        modifier = Modifier.size(40.dp)
//                    )
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column {
//                        Text(
//                            text = title,
//                            style = MaterialTheme.typography.titleMedium,
//                            color = Color.White,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        Text(
//                            text = description,
//                            style = MaterialTheme.typography.bodySmall,
//                            color = Color.White.copy(alpha = 0.9f)
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        text = "Collector Dashboard",
//                        style = MaterialTheme.typography.titleLarge
//                    )
//                },
//                actions = {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_profile),
//                        contentDescription = "Profile Picture",
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(CircleShape)
//                            .clickable { navController.navigate("profileScreen") }
//                            .padding(end = 8.dp)
//                    )
//                    IconButton(onClick = { navController.navigate("collector_settings") }) {
//                        Icon(
//                            imageVector = Icons.Default.Settings,
//                            contentDescription = "Settings"
//                        )
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Welcome message
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Start,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Spacer(modifier = Modifier.width(12.dp))
//                Column {
//                    Text(
//                        text = "Hey, $collectorName 👋",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = "Here’s what you can do today 🚀",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            FeatureCard(
//                title = "Report Issue",
//                description = "Log collection difficulties or route problems",
//                iconId = R.drawable.ic_launcher_foreground,
//                gradientColors = listOf(Color(0xFF81D4FA), Color(0xFF29B6F6)),
//                onClick = { navController.navigate("complaint/COLLECTOR") }
//            )
//
//            FeatureCard(
//                title = "Post Waste for Sale",
//                description = "List your collected waste for scrap buyers to purchase",
//                iconId = R.drawable.ic_post,
//                gradientColors = listOf(Color(0xFF90CAF9), Color(0xFF42A5F5)),
//                onClick = { navController.navigate("postWasteScreen") }
//            )
//
//            // 🌟 START/STOP JOURNEY BUTTON 🌟
//            Spacer(modifier = Modifier.height(16.dp))
//            Button(
//                onClick = { handleJourneyToggle() },
//                modifier = Modifier.fillMaxWidth().height(56.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (isJourneyActive) Color(0xFFEF5350) else Color(0xFF4CAF50) // Red for End, Green for Start
//                )
//            ) {
//                Text(
//                    text = if (isJourneyActive) "End Journey" else "Start Journey",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}







package com.example.regenx.screens.collectors

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.regenx.R
import com.example.regenx.backend.CollectorLocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDashboard(navController: NavController) {
    val context = LocalContext.current

    // 🌟 STATE: Controls the button text and service state
    var isJourneyActive by remember { mutableStateOf(false) }

    // Fetch collector's name
    var collectorName by remember { mutableStateOf("Collector") }

    // Pending purchases for this collector (real-time)
    var pendingList by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>() ) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUid = auth.currentUser?.uid

    // Local file path to be transformed into a URL by your pipeline (included per instruction)
    val localConfirmationPath = "/mnt/data/cbb20fc3-77dd-4010-819e-891e68ef7134.png"

    // Load collector display name
    LaunchedEffect(currentUid) {
        currentUid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")
                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
                        collectorName = "$firstName $lastName"
                    } else if (!firstName.isNullOrEmpty()) {
                        collectorName = firstName
                    }
                }
        }
    }

    // Listen for pending purchases for this collector
    DisposableEffect(currentUid) {
        val listenerRegistration: ListenerRegistration? = currentUid?.let { uid ->
            db.collection("wasteForSale")
                .whereEqualTo("collectorId", uid)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("CollectorDashboard", "Error listening pending: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        pendingList = snapshot.documents.mapNotNull { doc ->
                            val data = doc.data
                            if (data != null) Pair(doc.id, data) else null
                        }
                    }
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    // Location permissions
    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // Function to START the location service
    fun startLocationService() {
        val intent = Intent(context, CollectorLocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
        isJourneyActive = true
        Log.d("CollectorDashboard", "Service started. Journey is active.")
    }

    // Function to STOP the location service
    fun stopLocationService() {
        val intent = Intent(context, CollectorLocationService::class.java)
        context.stopService(intent)
        isJourneyActive = false
        Log.d("CollectorDashboard", "Service stopped. Journey is inactive.")
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        if (allGranted) {
            Log.d("CollectorDashboard", "✅ Permissions granted.")
            startLocationService() // Start service if permissions are granted
        } else {
            Log.e("CollectorDashboard", "❌ Location permissions denied")
        }
    }

    // CORE LOGIC: Toggles service and handles permission requests
    fun handleJourneyToggle() {
        if (isJourneyActive) {
            stopLocationService()
            return
        }

        // Check if permissions are granted
        val missingPermissions = locationPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // Permissions are already granted, start the service immediately
            startLocationService()
        } else {
            // Permissions are missing, request them via the launcher
            permissionLauncher.launch(locationPermissions)
        }
    }

    // Reusable FeatureCard composable
    @Composable
    fun FeatureCard(
        title: String,
        description: String,
        iconId: Int,
        gradientColors: List<Color>,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(brush = Brush.linearGradient(colors = gradientColors))
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(iconId),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Collector Dashboard",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("profileScreen") }
                            .padding(end = 8.dp)
                    )
                    IconButton(onClick = { navController.navigate("collector_settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Welcome message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hey, $collectorName 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Here’s what you can do today 🚀",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Pending Purchases Notification Section ---
            if (pendingList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pending Requests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // show up to 3 pending items
                    pendingList.take(3).forEach { (docId, data) ->
                        val wasteType = data["wasteType"]?.toString() ?: "Unknown"
                        val qty = data["quantity"]?.toString() ?: "-"
                        val buyerName = data["purchasedByName"]?.toString() ?: data["buyerName"]?.toString() ?: "Buyer"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("$wasteType • $qty kg", fontWeight = FontWeight.SemiBold)
                                    Text("Requested by: $buyerName", style = MaterialTheme.typography.bodySmall)
                                }

                                // Accept button
                                Button(
                                    // inside CollectorDashboard Accept button onClick
                                    onClick = {
                                        val wasteRef = db.collection("wasteForSale").document(docId)

                                        // We expect this doc to have purchaseId & status == "pending"
                                        db.runTransaction { transaction ->
                                            val wasteSnap = transaction.get(wasteRef)
                                            val currentStatus = wasteSnap.getString("status") ?: "available"
                                            val existingPurchaseId = wasteSnap.getString("purchaseId") // buyer's purchase doc id
                                            val purchasedBy = wasteSnap.getString("purchasedBy")

                                            // Only allow accepting a pending request
                                            if (currentStatus != "pending" || existingPurchaseId.isNullOrBlank()) {
                                                throw Exception("cannot_accept")
                                            }

                                            // Update wasteForSale to accepted
                                            val wasteUpdate = hashMapOf<String, Any>(
                                                "status" to "accepted",
                                                "acceptedAt" to FieldValue.serverTimestamp(),
                                                ("acceptedByCollectorId" to currentUid) as Pair<String, Any> // optional
                                            )
                                            transaction.update(wasteRef, wasteUpdate)

                                            // Update corresponding purchases/{existingPurchaseId} to accepted
                                            val purchaseRef = db.collection("purchases").document(existingPurchaseId)
                                            val purchaseUpdate = hashMapOf<String, Any>(
                                                "status" to "accepted",
                                                "acceptedAt" to FieldValue.serverTimestamp()
                                            )
                                            transaction.update(purchaseRef, purchaseUpdate)

                                            null
                                        }
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Request accepted. Buyer will be notified.", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { ex ->
                                                if (ex.message?.contains("cannot_accept") == true) {
                                                    Toast.makeText(context, "This request cannot be accepted (status changed).", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Could not accept: ${ex.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                    }
                                    ,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Accept")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Feature cards
            FeatureCard(
                title = "Report Issue",
                description = "Log collection difficulties or route problems",
                iconId = R.drawable.ic_launcher_foreground,
                gradientColors = listOf(Color(0xFF81D4FA), Color(0xFF29B6F6))
            ) {
                navController.navigate("complaint/COLLECTOR")
            }

            FeatureCard(
                title = "Post Waste for Sale",
                description = "List your collected waste for scrap buyers to purchase",
                iconId = R.drawable.ic_post,
                gradientColors = listOf(Color(0xFF90CAF9), Color(0xFF42A5F5))
            ) {
                navController.navigate("postWasteScreen")
            }

            // START/STOP JOURNEY BUTTON
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { handleJourneyToggle() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isJourneyActive) Color(0xFFEF5350) else Color(0xFF4CAF50) // Red for End, Green for Start
                )
            ) {
                Text(
                    text = if (isJourneyActive) "End Journey" else "Start Journey",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

