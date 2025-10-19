package com.example.regenx.screens.collectors.scrapCollectors


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapCollectorDashboard(navController: NavController) {
    var collectorName by remember { mutableStateOf("Scrap Buyer") }

    // Fetch scrap collector name from Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            FirebaseFirestore.getInstance().collection("collectors")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (!name.isNullOrEmpty()) {
                        collectorName = name
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scrap Buyer Dashboard", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("profileScreen") }
                            .padding(end = 8.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Greeting Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hey, $collectorName 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "In the mood to Recycle, take it! 🚚",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.navigate("scrapbuyer_settings") }
                )
            }

            // Feature Cards
            FeatureCard(
                title = "Nearby Pickup Requests",
                description = "See pending requests from residents nearby",
                icon = Icons.Default.List,
                gradient = Brush.linearGradient(listOf(Color(0xFF90CAF9), Color(0xFF42A5F5))),
                onClick = { navController.navigate("pickupRequests") }
            )

            FeatureCard(
                title = "Route Optimization",
                description = "Plan the best route for pickups today",
                icon = Icons.Default.Map,
                gradient = Brush.linearGradient(listOf(Color(0xFFA5D6A7), Color(0xFF66BB6A))),
                onClick = { navController.navigate("routeOptimization") }
            )

            FeatureCard(
                title = "Collected Scrap Log",
                description = "View history of collected scrap items",
                icon = Icons.Default.History,
                gradient = Brush.linearGradient(listOf(Color(0xFFFFAB91), Color(0xFFFF7043))),
                onClick = { navController.navigate("scrapLog") }
            )

            FeatureCard(
                title = "Report Issue",
                description = "Report any problem on your collection route",
                icon = Icons.Default.ReportProblem,
                gradient = Brush.linearGradient(listOf(Color(0xFFFFCC80), Color(0xFFFFA726))),
                onClick = { navController.navigate("reportIssue") }
            )

            // Tip of the Day
            // Inside your Composable
            val tips = listOf(
                "♻️ Segregate waste at source – keep dry and wet waste separate.",
                "🛍️ Say no to single-use plastics – carry your own cloth bag.",
                "🍌 Compost kitchen scraps – turn waste into fertilizer.",
                "💧 Reuse glass jars – perfect for storage instead of throwing away.",
                "📦 Flatten cardboard boxes – saves space and makes recycling easier.",
                "🛠️ Repair before you replace – extend product life.",
                "📰 Reuse old newspapers – for cleaning, wrapping, or crafts.",
                "⚡ E-waste is toxic – always drop it at certified collection centers.",
                "👕 Donate old clothes – give them a second life.",
                "🧴 Buy in bulk – reduces packaging waste."
            )

// Pick a random tip once per login
            val randomTip by remember { mutableStateOf(tips[Random.nextInt(tips.size)]) }

// UI Card
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "♻️ Tip of the Day",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = randomTip,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
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
                .background(brush = gradient)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
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
