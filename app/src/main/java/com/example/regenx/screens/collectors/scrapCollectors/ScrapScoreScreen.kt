package com.example.regenx.screens.collectors.scrapCollectors

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.util.*

// ----------------------------------------------------------------------
// CORRECTED: UiState must use 'by mutableStateOf' to ensure inner property changes
// trigger a recomposition in Compose.
// ----------------------------------------------------------------------
class UiState(
    initialScore: Int = 5,
    initialComment: String = "",
    initialIsRated: Boolean = false
) {
    // Use 'by mutableStateOf' for observable state within the class
    var score by mutableStateOf(initialScore)
    var comment by mutableStateOf(TextFieldValue(initialComment))
    var submitting by mutableStateOf(false)
    var isRated by mutableStateOf(initialIsRated)
}

// Data class mapped from wasteForSale doc
data class SoldWaste(
    val id: String = "",
    val collectorId: String = "",
    val wasteType: String? = null,
    val quantity: Double? = null,
    val totalAmount: Double? = null,
    val status: String? = null,
    val timestamp: Long? = null,
    val purchaseId: String? = null,
    val scrapRating: Double? = null,
    val scrapRatingComment: String? = null,
)

// ----------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapScoreScreen(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    // --- Theme Setup ---
    val useDark = isSystemInDarkTheme()
    val darkColors = darkColorScheme(
        primary = Color(0xFF64B5F6),
        onPrimary = Color.Black,
        background = Color(0xFF1E1E1E),
        surface = Color(0xFF2C2C2C),
        onSurface = Color(0xFFE0E0E0),
        secondaryContainer = Color(0xFF555555),
        onSecondaryContainer = Color.White,
        error = Color(0xFFCF6679)
    )
    val lightColors = lightColorScheme(
        primary = Color(0xFF0D47A1),
        onPrimary = Color.White,
        background = Color(0xFFF0F2F5),
        surface = Color.White,
        onSurface = Color.Black,
        secondaryContainer = Color(0xFFE0E0E0),
        onSecondaryContainer = Color.Black,
        error = Color(0xFFB00020)
    )

    MaterialTheme(colorScheme = if (useDark) darkColors else lightColors) {
        ScrapScoreContent(firestore, auth)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScrapScoreContent(
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
) {
    val context = LocalContext.current
    val currentUser = auth.currentUser
    var loading by remember { mutableStateOf(true) }
    var items by remember { mutableStateOf(listOf<SoldWaste>()) }

    // Map to hold UI state for each waste document ID
    val uiStates = remember { mutableStateMapOf<String, UiState>() }

    val evidenceLocalPath = "/mnt/data/cbb20fc3-77dd-4010-819e-891e68ef7134.png"

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            items = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        try {
            val snaps = firestore.collection("wasteForSale")
                .whereEqualTo("purchasedBy", currentUser.uid)
                .get()
                .await()

            val all = snaps.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val status = data["status"]?.toString() ?: ""

                if (status.lowercase() !in listOf("sold", "collected", "purchased")) return@mapNotNull null

                val ts = when (val t = data["purchasedAt"]) {
                    is Long -> t
                    is com.google.firebase.Timestamp -> t.seconds * 1000L
                    else -> data["timestamp"] as? Long
                }

                val scrapRating = (data["scrapRating"] as? Number)?.toDouble()

                SoldWaste(
                    id = doc.id,
                    collectorId = data["collectorId"]?.toString() ?: "",
                    wasteType = data["wasteType"]?.toString() ?: data["scrapType"]?.toString(),
                    quantity = (data["quantity"] as? Number)?.toDouble(),
                    totalAmount = (data["totalAmount"] as? Number)?.toDouble(),
                    status = status,
                    timestamp = ts,
                    purchaseId = data["purchaseId"]?.toString(),
                    scrapRating = scrapRating,
                    scrapRatingComment = data["scrapRatingComment"]?.toString()
                )
            }

            items = all.sortedByDescending { it.timestamp ?: 0L }

            uiStates.clear()
            // Initialize UiStates using the new class constructor
            items.forEach { w ->
                uiStates[w.id] = UiState(
                    initialScore = w.scrapRating?.toInt() ?: 5,
                    initialComment = w.scrapRatingComment ?: "",
                    initialIsRated = w.scrapRating != null
                )
            }

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load purchases: ${e.message}", Toast.LENGTH_LONG).show()
            items = emptyList()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌟 ScrapScore") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Rate the quality of scrap you purchased",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                if (items.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            "No completed pickups available for rating.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        items(items) { w ->
                            val state = uiStates.getOrPut(w.id) {
                                // Default initial state if not already mapped
                                UiState(
                                    initialScore = w.scrapRating?.toInt() ?: 5,
                                    initialComment = w.scrapRatingComment ?: "",
                                    initialIsRated = w.scrapRating != null
                                )
                            }

                            RatingCard(w, state, currentUser, context, firestore, evidenceLocalPath)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatingCard(
    waste: SoldWaste,
    state: UiState,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    context: android.content.Context,
    firestore: FirebaseFirestore,
    evidenceLocalPath: String,
) {
    val isRated = state.isRated

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = buildString {
                    append(waste.wasteType ?: "Scrap")
                    append(" • ")
                    append(waste.quantity?.toString() ?: "—")
                    append(" kg")
                    waste.totalAmount?.let { append(" • ₹${String.format("%.2f", it)}") }
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Completed: ${waste.timestamp?.let { Date(it).toString() } ?: "—"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stars
            Row(verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "star$i",
                        tint = if (i <= state.score) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(enabled = !isRated && !state.submitting) {
                                // This update now works because 'score' is a MutableState delegate
                                state.score = i
                            }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "${state.score} / 5",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                // This is the current value, read from state.comment (a MutableState)
                value = state.comment,
                onValueChange = { newComment ->
                    if (!isRated && !state.submitting) {
                        // This assignment now works because 'comment' is a MutableState delegate
                        state.comment = newComment
                    }
                },
                label = { Text("Optional comment") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                enabled = !isRated && !state.submitting,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = isRated,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    "✅ Already rated THANK YOU",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            AnimatedVisibility(
                visible = !isRated,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = {
                        if (currentUser == null) {
                            Toast.makeText(context, "Please sign in to submit rating", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (state.submitting) return@Button

                        // Set submitting state to true (will trigger progress indicator)
                        state.submitting = true

                        submitRatingAndSetScrapRating(
                            firestore = firestore,
                            wasteDocId = waste.id,
                            buyerId = currentUser.uid,
                            purchaseId = waste.purchaseId ?: waste.id,
                            collectorId = waste.collectorId,
                            score = state.score,
                            comment = state.comment.text,
                            evidenceUrl = evidenceLocalPath,
                            onSuccess = {
                                // Revert states on success
                                state.submitting = false
                                state.isRated = true
                                Toast.makeText(context, "Thanks — rating saved!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { err ->
                                // Revert submitting state on failure
                                state.submitting = false
                                Toast.makeText(context, "Could not save rating: ${err?.message ?: "error"}", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !state.submitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (state.submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Submitting", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("Submit Rating", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Custom function to handle rating submission:
 * 1. Writes to 'ratings' collection.
 * 2. Atomically updates 'collectors' aggregate.
 * 3. Writes 'scrapRating' and 'scrapRatingComment' directly to the 'wasteForSale' document.
 */
fun submitRatingAndSetScrapRating(
    firestore: FirebaseFirestore,
    wasteDocId: String,
    buyerId: String,
    purchaseId: String,
    collectorId: String,
    score: Int,
    comment: String?,
    evidenceUrl: String?,
    onSuccess: () -> Unit,
    onFailure: (Exception?) -> Unit
) {
    if (score !in 1..5) {
        onFailure(IllegalArgumentException("Score must be 1..5"))
        return
    }

    val ratingRef = firestore.collection("ratings").document()
    val collectorRef = firestore.collection("collectors").document(collectorId)
    val wasteRef = firestore.collection("wasteForSale").document(wasteDocId)

    val ratingData = hashMapOf(
        "purchaseId" to purchaseId,
        "buyerId" to buyerId,
        "collectorId" to collectorId,
        "score" to score,
        "comment" to (comment ?: ""),
        "evidenceUrl" to (evidenceUrl ?: ""),
        "timestamp" to FieldValue.serverTimestamp()
    )

    val wasteUpdateData = hashMapOf<String, Any>(
        "scrapRating" to score.toDouble(),
        "scrapRatingComment" to (comment ?: "")
    )

    // Check for duplicate rating before running transaction
    firestore.collection("ratings")
        .whereEqualTo("purchaseId", purchaseId)
        .whereEqualTo("buyerId", buyerId)
        .get()
        .addOnSuccessListener { query ->
            if (!query.isEmpty) {
                onFailure(FirebaseFirestoreException("You have rated this purchase", FirebaseFirestoreException.Code.ABORTED))
                return@addOnSuccessListener
            }

            // Run the batch/transaction for all atomic updates
            firestore.runTransaction { transaction ->
                // 1. Write to ratings collection
                transaction.set(ratingRef, ratingData)

                // 2. Update wasteForSale document
                transaction.update(wasteRef, wasteUpdateData)

                // 3. Update collector aggregates
                val collectorSnap = try {
                    transaction.get(collectorRef)
                } catch (e: Exception) {
                    null
                }

                val currentAvg = collectorSnap?.getDouble("avgRating") ?: 0.0
                val currentCount = collectorSnap?.getLong("ratingCount") ?: 0L

                val newCount = currentCount + 1
                val newAvg = if (currentCount == 0L) score.toDouble() else (currentAvg * currentCount + score) / newCount

                val newCollectorData = hashMapOf<String, Any>(
                    "avgRating" to newAvg,
                    "ratingCount" to newCount,
                    "lastRatedAt" to FieldValue.serverTimestamp()
                )

                transaction.set(collectorRef, newCollectorData, SetOptions.merge())
                null
            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onFailure(e)
            }
        }
        .addOnFailureListener { e ->
            onFailure(e)
        }
}

