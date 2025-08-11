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
        if (currentUser != null) {
            try {
                val doc = firestore.collection("users").document(currentUser.uid).get().await()
                val role = doc.getString("role") ?: "RESIDENT"

                when (role) {
                    "RESIDENT" -> navController.navigate("residentDashboard") { popUpTo(0) }
                    "COLLECTOR" -> navController.navigate("collectorDashboard") { popUpTo(0) }
                    "OFFICIAL" -> navController.navigate("officialDashboard") { popUpTo(0) }
                    else -> navController.navigate("roleChoice") { popUpTo(0) }
                }
            } catch (e: Exception) {
                // If fetching role fails, go to home
                navController.navigate("roleChoice") { popUpTo(0) }
            }
        } else {
            // Not logged in → go to role choice or home
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
