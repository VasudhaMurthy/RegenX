
package com.example.regenx.screens.residents

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.regenx.R // Ensure this is your actual package for resources

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDashboard(navController: NavController) {
    var residentName by remember { mutableStateOf("Resident") }

    // Fetch resident name from Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            FirebaseFirestore.getInstance().collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (!name.isNullOrEmpty()) {
                        residentName = name
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Resident Dashboard",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile Picture",
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

            // Profile Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_profile), // Replace with your placeholder/profile image
//                        contentDescription = "Profile Picture",
//                        modifier = Modifier
//                            .size(48.dp)
//                            .clip(CircleShape)
//                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hey, $residentName 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Here’s what you can do today 🚀",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.navigate("resident_settings") }
                )
            }

            // Feature Cards

            FeatureCard(
                title = "Locate Garbage Truck",
                description = "Track the live location of the garbage collection truck",
                icon = Icons.Default.LocalShipping,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFFFAB91), Color(0xFFFF7043))),
                onClick = { navController.navigate("locateGarbageTruck") }
            )

            FeatureCard(
                title = "Waste Segregation (AI)",
                description = "Scan your waste to get instant segregation tips",
                icon = Icons.Default.CameraAlt,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFA5D6A7), Color(0xFF66BB6A))),
                onClick = { navController.navigate("wasteSegregation") }
            )

            FeatureCard(
                title = "Nearby Scrap Collectors",
                description = "Find the nearest verified collectors on the map",
                icon = Icons.Default.LocationOn,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFF90CAF9), Color(0xFF42A5F5))),
                onClick = { navController.navigate("scrapCollectorMap") }
            )

            FeatureCard(
                title = "SwaBhaVi (Raise Complaint)",
                description = "Report missed pickups or illegal dumping",
                icon = Icons.Default.ReportProblem,
                gradient = Brush.linearGradient(colors = listOf(Color(0xFFFFCC80), Color(0xFFFFA726))),
                onClick = { navController.navigate("complaint/RESIDENT") }
            )

            // Tip of the day
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
                        text = "Separate wet and dry waste daily to keep your neighborhood clean and green!",
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
