package com.example.regenx.screens.officials

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.regenx.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat // Added for date formatting
import java.util.Date           // Added for date retrieval
import java.util.Locale         // Added for locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialDashboard(navController: NavController) {
    var officialName by remember { mutableStateOf("Official") }

    // 🌟 FIX: Replace java.time.LocalDate.now() with SimpleDateFormat 🌟
    val todayDate = remember {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.format(Date())
    }

    // Fetch official name from Firestore
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            FirebaseFirestore.getInstance().collection("officials")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName")
                    val lastName = document.getString("lastName")
                    if (!firstName.isNullOrEmpty() && !lastName.isNullOrEmpty()) {
                        officialName = "$firstName $lastName"
                    }
                }
        }
    }

    // Function to create Feature Card
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

    // Composable for Waste Collection Summary
    @Composable
    fun WasteSummaryCard(date: String, totalWeight: String, routesCompleted: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Collection Summary: $date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Collected Weight", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(totalWeight, style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp), fontWeight = FontWeight.SemiBold)
                    }
                    // 🌟 FIX: Removed deprecated `Divider` usage that caused the warning 🌟
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Routes Completed", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(routesCompleted, style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Official Dashboard",
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
                    IconButton(onClick = { navController.navigate("official_settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            item {
                // Welcome message section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hey, $officialName 👋",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage municipal operations 📊",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // WASTE COLLECTION SUMMARY SECTION
            item {
                Text(
                    text = "Daily Operations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                WasteSummaryCard(
                    date = todayDate,
                    totalWeight = "12.5 MT",
                    routesCompleted = "8 / 10"
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }


            // FEATURE CARDS
            item {
                // 1. View Complaints (Existing)
                FeatureCard(
                    title = "SwaBhaVi (View Complaints)",
                    description = "View and manage all resident complaints and reports.",
                    iconId = R.drawable.ic_launcher_foreground, // Using existing placeholder R.drawable.ic_launcher_foreground
                    gradientColors = listOf(Color(0xFFFFCC80), Color(0xFFFFA726)),
                    onClick = { navController.navigate("viewComplaints") }
                )
            }

            item {
                // 2. Manage Fleet & Routes (New Card)
                FeatureCard(
                    title = "Manage Fleet & Routes",
                    description = "Monitor live locations and optimize collection routes.",
                    iconId = R.drawable.ic_launcher_foreground, // Placeholder
                    gradientColors = listOf(Color(0xFF90CAF9), Color(0xFF42A5F5)),
                    onClick = { navController.navigate("fleetManagement") } // 🌟 UPDATED 🌟
                )
            }

            item {
                // 3. Analytics & Reporting (New Card)
                FeatureCard(
                    title = "Analytics & Reports",
                    description = "Access historical data on waste collection efficiency.",
                    iconId = R.drawable.ic_launcher_foreground, // Using existing placeholder R.drawable.ic_launcher_foreground
                    gradientColors = listOf(Color(0xFFA5D6A7), Color(0xFF66BB6A)),
                    onClick = { navController.navigate("analyticsManagement")}
                )
            }
        }
    }
}