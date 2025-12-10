package com.example.regenx.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.regenx.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring

@Composable
fun SplashScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Animation state
    var startAnim by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(1500) // Keep splash for 1.5 sec

        val currentUser = auth.currentUser

        if (currentUser == null) {
            // Not logged in → go to role choice
            navController.navigate("roleChoice") { popUpTo(0) }
            return@LaunchedEffect
        }

        val uid = currentUser.uid

        // User is logged in. Check role by finding the user's document in the role-specific collections.
        try {
            var destinationRoute = "roleChoice"

            // Check for document existence in role-specific collections

            // 1. Check for RESIDENT (Most common fallback, check first)
            val residentDoc = firestore.collection("residents").document(uid).get().await()

            // 2. Check for COLLECTOR
            val collectorDoc = firestore.collection("collectors").document(uid).get().await()

            // 3. Check for OFFICIAL
            val officialDoc = firestore.collection("officials").document(uid).get().await()

            when {
                officialDoc.exists() -> {
                    // Check for lastScreen preference in the OFFICIAL document
                    val lastScreen = officialDoc.getString("lastScreen")
                    destinationRoute = lastScreen ?: "officialDashboard"
                }
                collectorDoc.exists() -> {
                    // Check for lastScreen preference in the COLLECTOR document
                    val lastScreen = collectorDoc.getString("lastScreen")
                    destinationRoute = lastScreen ?: "collectorDashboard"
                }
                residentDoc.exists() -> {
                    // Check for lastScreen preference in the RESIDENT document
                    val lastScreen = residentDoc.getString("lastScreen")
                    destinationRoute = lastScreen ?: "residentDashboard"
                }
                // Default remains 'roleChoice' if document isn't found in any collection
            }

            navController.navigate(destinationRoute) { popUpTo(0) }

        } catch (e: Exception) {
            // If fetching fails (e.g., network error)
            navController.navigate("roleChoice") { popUpTo(0) }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.my_icon),
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
    }
}