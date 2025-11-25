//package com.example.regenx.screens.collectors.scrapCollectors
//
//import android.widget.Toast
//import androidx.compose.animation.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import com.google.firebase.Timestamp
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FieldValue
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NearbyPickupRequestsScreen() {
//    // Theme: pick color scheme based on system dark mode so screen follows device setting
//    val useDark = isSystemInDarkTheme()
//
//    // Enhanced Color Schemes for a modern Material 3 look
//    val darkColors = darkColorScheme(
//        primary = Color(0xFF64B5F6), // Light Blue
//        onPrimary = Color.Black,
//        background = Color(0xFF1E1E1E), // Darker background
//        surface = Color(0xFF2C2C2C), // Slightly lighter surface for cards
//        onSurface = Color(0xFFE0E0E0),
//        secondaryContainer = Color(0xFF555555),
//        onSecondaryContainer = Color.White,
//    )
//    val lightColors = lightColorScheme(
//        primary = Color(0xFF0D47A1), // Dark Blue
//        onPrimary = Color.White,
//        background = Color(0xFFF0F2F5), // Light gray background
//        surface = Color.White, // Pure white for cards
//        onSurface = Color.Black,
//        secondaryContainer = Color(0xFFE0E0E0),
//        onSecondaryContainer = Color.Black,
//    )
//
//    MaterialTheme(colorScheme = if (useDark) darkColors else lightColors) {
//        NearbyPickupRequestsContent()
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun NearbyPickupRequestsContent() {
//    // store list as pairs of (docId, data map) so we can update the doc when purchased
//    var wasteList by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
//    val db = FirebaseFirestore.getInstance()
//    val auth = FirebaseAuth.getInstance()
//    val context = LocalContext.current
//    var expandedId by remember { mutableStateOf<String?>(null) } // which item is expanded
//    var loadingPurchaseForId by remember { mutableStateOf<String?>(null) } // show spinner per item
//
//    // Listen to wasteForSale collection (real-time)
//    DisposableEffect(Unit) {
//        val listener: ListenerRegistration = db.collection("wasteForSale")
//            .addSnapshotListener { snapshot, _ ->
//                if (snapshot != null) {
//                    wasteList = snapshot.documents.mapNotNull { doc ->
//                        val data = doc.data
//                        if (data != null) Pair(doc.id, data) else null
//                    }
//                }
//            }
//
//        onDispose {
//            listener.remove()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("♻️ Nearby Pickup Requests") },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    titleContentColor = MaterialTheme.colorScheme.onSurface,
//                    // Added shadow/elevation for a cleaner look
//                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
//                )
//            )
//        },
//        containerColor = MaterialTheme.colorScheme.background
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colorScheme.background)
//                .padding(innerPadding)
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            items(wasteList) { (docId, item) ->
//                // read common fields safely
//                val wasteType = item["wasteType"]?.toString() ?: "N/A"
//                val quantity = item["quantity"]?.toString() ?: "N/A"
//                val notes = item["notes"]?.toString() ?: ""
//                val category = item["category"]?.toString() ?: ""
//                val collectorId = item["collectorId"]?.toString() ?: ""
//                val status = item["status"]?.toString() ?: "available"
//
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            // toggle expand
//                            expandedId = if (expandedId == docId) null else docId
//                        },
//                    // Used surface color for consistency
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//                    // Increased elevation for a more prominent card
//                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
//                    shape = RoundedCornerShape(12.dp) // Added rounded corners
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            Column(modifier = Modifier.weight(1f)) {
//                                Text(
//                                    // Bolder and larger for main item type
//                                    wasteType,
//                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                                    color = MaterialTheme.colorScheme.primary
//                                )
//                                Spacer(modifier = Modifier.height(4.dp))
//                                Text(
//                                    "Quantity: ${quantity}",
//                                    style = MaterialTheme.typography.bodyLarge,
//                                    color = MaterialTheme.colorScheme.onSurface
//                                )
//                                Text(
//                                    "Category: $category",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                            // Status Badge
//                            StatusBadge(status = status)
//
//                            // Clean Expansion Indicator Icon
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Icon(
//                                imageVector = if (expandedId == docId) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                                contentDescription = "Toggle Details",
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                                modifier = Modifier.size(24.dp)
//                            )
//                        }
//
//                        // Expanded area with animation
//                        AnimatedVisibility(
//                            visible = expandedId == docId,
//                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
//                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
//                        ) {
//                            Column(modifier = Modifier.padding(top = 12.dp)) {
//                                Divider(
//                                    modifier = Modifier.padding(vertical = 8.dp),
//                                    color = MaterialTheme.colorScheme.secondaryContainer
//                                )
//
//                                // Details section
//                                DetailRow(label = "Notes", value = if (notes.isBlank()) "—" else notes)
//                                DetailRow(label = "Collector ID", value = collectorId)
//                                Spacer(modifier = Modifier.height(8.dp))
//
//                                // Purchased Info
//                                if (status == "purchased") {
//                                    val purchasedBy = item["purchasedBy"]?.toString() ?: "N/A"
//                                    val purchasedByName = item["purchasedByName"]?.toString() ?: ""
//                                    val purchasedAt = (item["purchasedAt"] as? Timestamp)?.toDate()?.toString() ?: item["purchasedAt"]?.toString()
//
//                                    Text(
//                                        "Purchase Details",
//                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
//                                        color = MaterialTheme.colorScheme.onSurface
//                                    )
//                                    DetailRow(
//                                        label = "Purchased by",
//                                        value = if (purchasedByName.isNotBlank()) "$purchasedByName ($purchasedBy)" else purchasedBy
//                                    )
//                                    DetailRow(label = "At", value = purchasedAt ?: "—")
//                                } else {
//                                    // Purchase button
//                                    Button(
//                                        // --- inside your Purchase Button onClick ---
//                                        // inside your Purchase Button onClick (NearbyPickupRequestsScreen)
//                                        onClick = {
//                                            val currentUser = auth.currentUser
//                                            if (currentUser == null) {
//                                                Toast.makeText(context, "Please sign in to purchase", Toast.LENGTH_SHORT).show()
//                                                return@Button
//                                            }
//                                            if (loadingPurchaseForId != null) return@Button
//                                            loadingPurchaseForId = docId
//
//                                            val buyerId = currentUser.uid
//                                            val buyerName = currentUser.displayName ?: ""
//
//                                            val wasteRef = db.collection("wasteForSale").document(docId)
//                                            val purchaseRef = db.collection("purchases").document() // will use this id in waste doc
//
//                                            // Use uploaded path if needed (pipeline will convert to URL)
//                                            val confirmationProofUrl = "/mnt/data/cbb20fc3-77dd-4010-819e-891e68ef7134.png"
//
//                                            db.runTransaction { transaction ->
//                                                // 1) read the latest waste doc
//                                                val wasteSnap = transaction.get(wasteRef)
//                                                val currentStatus = wasteSnap.getString("status") ?: "available"
//
//                                                // 2) only allow purchase when status is "available"
//                                                if (currentStatus != "available") {
//                                                    // throw to abort transaction
//                                                    throw Exception("not_available")
//                                                }
//
//                                                // 3) create purchase doc
//                                                val purchaseData = hashMapOf<String, Any?>(
//                                                    "wasteId" to docId,
//                                                    "wasteType" to wasteType,
//                                                    "quantity" to quantity,
//                                                    "category" to category,
//                                                    "collectorId" to collectorId,
//                                                    "buyerId" to buyerId,
//                                                    "buyerName" to buyerName,
//                                                    "status" to "pending",               // buyer-side status
//                                                    "confirmationProofUrl" to confirmationProofUrl,
//                                                    "timestamp" to FieldValue.serverTimestamp()
//                                                )
//                                                transaction.set(purchaseRef, purchaseData)
//
//                                                // 4) update wasteForSale doc atomically
//                                                val wasteUpdate = hashMapOf<String, Any>(
//                                                    "status" to "pending",
//                                                    "purchasedBy" to buyerId,
//                                                    "purchasedByName" to buyerName,
//                                                    "purchasedAt" to FieldValue.serverTimestamp(),
//                                                    "purchaseId" to purchaseRef.id
//                                                )
//                                                transaction.update(wasteRef, wasteUpdate)
//                                                // transaction returns null
//                                                null
//                                            }
//                                                .addOnSuccessListener {
//                                                    loadingPurchaseForId = null
//                                                    Toast.makeText(context, "Purchase requested — waiting for collector to accept.", Toast.LENGTH_SHORT).show()
//                                                }
//                                                .addOnFailureListener { ex ->
//                                                    loadingPurchaseForId = null
//                                                    // detect our sentinel exception
//                                                    if (ex.message?.contains("not_available") == true) {
//                                                        Toast.makeText(context, "Sorry — this item was already requested or sold.", Toast.LENGTH_LONG).show()
//                                                    } else {
//                                                        Toast.makeText(context, "Purchase failed: ${ex.message}", Toast.LENGTH_LONG).show()
//                                                    }
//                                                }
//                                        }
//
//                                        ,
//                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
//                                        shape = RoundedCornerShape(8.dp), // Sharper corners for a button
//                                        modifier = Modifier.fillMaxWidth(),
//                                        enabled = (loadingPurchaseForId != docId)
//                                    ) {
//                                        if (loadingPurchaseForId == docId) {
//                                            CircularProgressIndicator(
//                                                modifier = Modifier.size(20.dp),
//                                                strokeWidth = 3.dp,
//                                                color = MaterialTheme.colorScheme.onPrimary
//                                            )
//                                            Spacer(modifier = Modifier.width(12.dp))
//                                            Text("Purchasing...", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
//                                        } else {
//                                            Text("Acknowledge Pickup", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            // show placeholder if empty
//            if (wasteList.isEmpty()) {
//                item {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .height(200.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "No nearby pickup requests yet. Check back soon!",
//                            style = MaterialTheme.typography.titleMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
///**
// * A reusable composable for displaying a detail row.
// */
//@Composable
//private fun DetailRow(label: String, value: String) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 2.dp)
//    ) {
//        Text(
//            text = "$label: ",
//            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
//            color = MaterialTheme.colorScheme.onSurface
//        )
//        Text(
//            text = value,
//            style = MaterialTheme.typography.bodySmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}
//
///**
// * A visually distinct badge for the status.
// */
//@Composable
//private fun StatusBadge(status: String) {
//    val (color, text) = when (status.lowercase()) {
//        "purchased" -> Pair(Color(0xFFE57373), "Purchased") // Light Red/Orange
//        "available" -> Pair(Color(0xFF81C784), "Available") // Light Green
//        else -> Pair(MaterialTheme.colorScheme.secondaryContainer, status.replaceFirstChar { it.uppercase() })
//    }
//
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(8.dp))
//            .background(color.copy(alpha = 0.2f)) // Light background tint
//            .padding(horizontal = 8.dp, vertical = 4.dp)
//    ) {
//        Text(
//            text = text,
//            color = color, // Stronger color for text
//            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
//        )
//    }
//}







package com.example.regenx.screens.collectors.scrapCollectors

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NearbyPickupRequestsScreen() {
    // pick color scheme based on system dark mode so screen follows device setting
    val useDark = isSystemInDarkTheme()
    val darkColors = darkColorScheme(
        primary = Color(0xFF64B5F6),
        onPrimary = Color.Black,
        background = Color(0xFF1E1E1E),
        surface = Color(0xFF2C2C2C),
        onSurface = Color(0xFFE0E0E0),
        outline = Color(0xFF333333)
    )
    val lightColors = lightColorScheme(
        primary = Color(0xFF0D47A1),
        onPrimary = Color.White,
        background = Color(0xFFF0F2F5),
        surface = Color.White,
        onSurface = Color.Black,
        outline = Color(0xFFBDBDBD)
    )

    MaterialTheme(colorScheme = if (useDark) darkColors else lightColors) {
        NearbyPickupRequestsContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
private fun NearbyPickupRequestsContent() {
    // pair of (docId, dataMap)
    var wasteList by remember { mutableStateOf(listOf<Pair<String, Map<String, Any>>>()) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<String?>(null) } // which item is expanded
    var loadingPurchaseForId by remember { mutableStateOf<String?>(null) } // per-item loading
    var loadingAcceptForId by remember { mutableStateOf<String?>(null) } // collector accept loading
    var loadingFinalizeForId by remember { mutableStateOf<String?>(null) } // buyer finalize loading

    // Use the uploaded file path as placeholder proof (pipeline will transform to URL)
    val uploadedProofPath = "/mnt/data/33573eeb-f344-4c94-b6f7-4a292de25f32.png"

    // Realtime listener for wasteForSale
    DisposableEffect(Unit) {
        val listener: ListenerRegistration = db.collection("wasteForSale")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // optional: show/log error
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    wasteList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        if (data != null) Pair(doc.id, data) else null
                    }
                }
            }
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("♻️ Nearby Pickup Requests") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(wasteList) { (docId, item) ->
                // safely read fields
                val wasteType = item["wasteType"]?.toString() ?: "N/A"
                val quantity = item["quantity"]?.toString() ?: "N/A"
                val notes = item["notes"]?.toString() ?: ""
                val category = item["category"]?.toString() ?: ""
                val collectorId = item["collectorId"]?.toString() ?: ""
                val status = item["status"]?.toString() ?: "available"
                val purchasedBy = item["purchasedBy"]?.toString()
                val purchasedByName = item["purchasedByName"]?.toString() ?: ""
                val purchaseId = item["purchaseId"]?.toString()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedId = if (expandedId == docId) null else docId
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    wasteType,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("Quantity: $quantity", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                Text("Category: $category", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text("Status: ${status.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                            }

                            // Status badge simplified inline (small)
                            when (status) {
                                "available" -> Text("Available", color = Color(0xFF81C784))
                                "pending" -> Text("Pending", color = Color(0xFFFFA726))
                                "accepted" -> Text("Accepted", color = Color(0xFF64B5F6))
                                "sold", "collected" -> Text("Sold", color = Color(0xFFE57373))
                                else -> Text(status.replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = if (expandedId == docId) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Expanded content
                        AnimatedVisibility(
                            visible = expandedId == docId,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Divider(color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(8.dp))
                                Text("Notes: ${if (notes.isBlank()) "—" else notes}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(8.dp))
                                Text("Collector ID: $collectorId", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                Spacer(Modifier.height(12.dp))

                                val currentUser = auth.currentUser
                                val currentUid = currentUser?.uid

                                // UI logic based on status and current user
                                when (status) {
                                    "available" -> {
                                        // Show Purchase button to everyone (if signed in)
                                        Button(
                                            onClick = {
                                                // Transactional purchase: only if status is still "available"
                                                if (currentUser == null) {
                                                    Toast.makeText(context, "Please sign in to purchase", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                if (loadingPurchaseForId != null) return@Button
                                                loadingPurchaseForId = docId

                                                val buyerId = currentUser.uid
                                                val buyerName = currentUser.displayName ?: ""

                                                val wasteRef = db.collection("wasteForSale").document(docId)
                                                val purchaseRef = db.collection("purchases").document()

                                                val confirmationProofUrl = uploadedProofPath

                                                db.runTransaction { transaction ->
                                                    val wasteSnap = transaction.get(wasteRef)
                                                    val currentStatus = wasteSnap.getString("status") ?: "available"
                                                    if (currentStatus != "available") {
                                                        throw Exception("not_available")
                                                    }

                                                    val purchaseData = hashMapOf<String, Any?>(
                                                        "wasteId" to docId,
                                                        "wasteType" to wasteType,
                                                        "quantity" to quantity,
                                                        "category" to category,
                                                        "collectorId" to collectorId,
                                                        "buyerId" to buyerId,
                                                        "buyerName" to buyerName,
                                                        "status" to "pending",
                                                        "confirmationProofUrl" to confirmationProofUrl,
                                                        "timestamp" to FieldValue.serverTimestamp()
                                                    )
                                                    transaction.set(purchaseRef, purchaseData)

                                                    val wasteUpdate = hashMapOf<String, Any>(
                                                        "status" to "pending",
                                                        "purchasedBy" to buyerId,
                                                        "purchasedByName" to buyerName,
                                                        "purchasedAt" to FieldValue.serverTimestamp(),
                                                        "purchaseId" to purchaseRef.id
                                                    )
                                                    transaction.update(wasteRef, wasteUpdate)
                                                    null
                                                }.addOnSuccessListener {
                                                    loadingPurchaseForId = null
                                                    Toast.makeText(context, "Purchase requested — waiting for collector to accept.", Toast.LENGTH_SHORT).show()
                                                }.addOnFailureListener { ex ->
                                                    loadingPurchaseForId = null
                                                    if (ex.message?.contains("not_available") == true) {
                                                        Toast.makeText(context, "Sorry — this item was already requested or sold.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "Purchase failed: ${ex.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            enabled = (loadingPurchaseForId != docId),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (loadingPurchaseForId == docId) {
                                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Requesting...", color = MaterialTheme.colorScheme.onPrimary)
                                            } else {
                                                Text("Purchase", color = MaterialTheme.colorScheme.onPrimary)
                                            }
                                        }
                                    }

                                    "pending" -> {
                                        // If current user is the buyer who requested -> show waiting state; else show disabled note
                                        if (currentUid != null && currentUid == purchasedBy) {
                                            Text("Request pending — waiting for collector to accept.", color = MaterialTheme.colorScheme.onSurface)
                                            Spacer(Modifier.height(8.dp))
                                            // Allow cancel (optional). Not implemented here automatically.
                                        } else {
                                            Text("Already requested by another buyer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    "accepted" -> {
                                        // Collector accepted the request. If current user is buyer -> show acknowledge/finalize button.
                                        if (currentUid != null && currentUid == purchasedBy) {
                                            // Acknowledge / Finalise pickup (transactional)
                                            Button(
                                                onClick = {
                                                    if (loadingFinalizeForId != null) return@Button
                                                    loadingFinalizeForId = docId

                                                    val buyerId = currentUid
                                                    val wasteRef = db.collection("wasteForSale").document(docId)
                                                    val purchaseRefId = purchaseId
                                                    if (purchaseRefId.isNullOrBlank()) {
                                                        Toast.makeText(context, "No purchase record found.", Toast.LENGTH_SHORT).show()
                                                        loadingFinalizeForId = null
                                                        return@Button
                                                    }
                                                    val purchaseRef = db.collection("purchases").document(purchaseRefId)
                                                    val buyerConfirmationUrl = uploadedProofPath

                                                    db.runTransaction { transaction ->
                                                        val wasteSnap = transaction.get(wasteRef)
                                                        val currentStatus = wasteSnap.getString("status") ?: "available"
                                                        val currentPurchasedBy = wasteSnap.getString("purchasedBy") ?: ""
                                                        if (currentStatus != "accepted" || currentPurchasedBy != buyerId) {
                                                            throw Exception("cannot_finalize")
                                                        }

                                                        // set terminal state on waste doc
                                                        transaction.update(wasteRef, mapOf(
                                                            "status" to "sold",
                                                            "soldAt" to FieldValue.serverTimestamp(),
                                                            "buyerConfirmationUrl" to buyerConfirmationUrl
                                                        ))

                                                        // update purchase to collected
                                                        transaction.update(purchaseRef, mapOf(
                                                            "status" to "collected",
                                                            "collectedAt" to FieldValue.serverTimestamp(),
                                                            "buyerConfirmationUrl" to buyerConfirmationUrl
                                                        ))

                                                        null
                                                    }.addOnSuccessListener {
                                                        loadingFinalizeForId = null
                                                        Toast.makeText(context, "Pickup acknowledged — thank you!", Toast.LENGTH_SHORT).show()
                                                    }.addOnFailureListener { ex ->
                                                        loadingFinalizeForId = null
                                                        if (ex.message?.contains("cannot_finalize") == true) {
                                                            Toast.makeText(context, "This pickup cannot be finalised (status changed).", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, "Could not finalise pickup: ${ex.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                },
                                                enabled = (loadingFinalizeForId != docId),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                if (loadingFinalizeForId == docId) {
                                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Finalising...", color = MaterialTheme.colorScheme.onPrimary)
                                                } else {
                                                    Text("Acknowledge Pickup", color = MaterialTheme.colorScheme.onPrimary)
                                                }
                                            }
                                        } else {
                                            // Not the buyer — waiting for buyer to acknowledge
                                            Text("Accepted — waiting for buyer to acknowledge pickup.", color = MaterialTheme.colorScheme.onSurface)
                                        }
                                    }

                                    "sold", "collected" -> {
                                        Text("Completed — item already collected.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    else -> {
                                        Text("Status: ${status}", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Collector quick info & accept action only visible to collectors (optional)
                                // If the signed-in user is the collector, show Accept when status == pending
                                val signedInUid = currentUid
                                if (signedInUid != null && signedInUid == collectorId && status == "pending") {
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (loadingAcceptForId != null) return@Button
                                            loadingAcceptForId = docId

                                            val wasteRef = db.collection("wasteForSale").document(docId)

                                            db.runTransaction { transaction ->
                                                val wasteSnap = transaction.get(wasteRef)
                                                val currentStatus = wasteSnap.getString("status") ?: "available"
                                                val existingPurchaseId = wasteSnap.getString("purchaseId")
                                                if (currentStatus != "pending" || existingPurchaseId.isNullOrBlank()) {
                                                    throw Exception("cannot_accept")
                                                }

                                                // update wasteForSale to accepted
                                                transaction.update(wasteRef, mapOf(
                                                    "status" to "accepted",
                                                    "acceptedAt" to FieldValue.serverTimestamp(),
                                                    "acceptedByCollectorId" to signedInUid
                                                ))

                                                // update purchase doc to accepted
                                                val purchaseRefId = existingPurchaseId
                                                val purchaseRef = db.collection("purchases").document(purchaseRefId)
                                                transaction.update(purchaseRef, mapOf(
                                                    "status" to "accepted",
                                                    "acceptedAt" to FieldValue.serverTimestamp()
                                                ))

                                                null
                                            }.addOnSuccessListener {
                                                loadingAcceptForId = null
                                                Toast.makeText(context, "Request accepted. Buyer will be notified.", Toast.LENGTH_SHORT).show()
                                            }.addOnFailureListener { ex ->
                                                loadingAcceptForId = null
                                                if (ex.message?.contains("cannot_accept") == true) {
                                                    Toast.makeText(context, "This request cannot be accepted (status changed).", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, "Could not accept: ${ex.message}", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (loadingAcceptForId == docId) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Accepting...", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        } else {
                                            Text("Accept", color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // placeholder when empty
            if (wasteList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No nearby pickup requests yet. Check back soon!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
