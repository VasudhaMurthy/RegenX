package com.example.regenx.screens.residents

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.regenx.R

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.regenx.ui.ScannerActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentDashboard(navController: NavController) {
    // State to hold the resident's name, defaulted to "Resident"
    var residentName by remember { mutableStateOf("Resident") }
    val context = LocalContext.current

    // --- Name Fetching Logic ---
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            FirebaseFirestore.getInstance().collection("users")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (!name.isNullOrEmpty()) {
                        residentName = name.split(" ")[0]
                    }
                }
                .addOnFailureListener {
                    // Optional: Log error or show a silent message if fetching fails
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("resident_settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { navController.navigate("profileScreen") }
                            .padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },

        // 👉 Floating circular "Ask AI" button
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: change route if needed
                    navController.navigate("askAiScreen")
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp) // nice circular size
            ) {
                Text(
                    text = "Ask AI",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Greeting Section (Displays fetched name) ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Hey, $residentName 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Here’s what you can do today 🚀",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Feature Cards ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FeatureCard(
                        title = "Locate Garbage Truck",
                        description = "Track the live location of the garbage collection truck",
                        icon = Icons.Default.LocalShipping,
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFAB91),
                                Color(0xFFFF7043)
                            )
                        ),
                        onClick = { navController.navigate("locateGarbageTruck") }
                    )

//                    FeatureCard(
//                        title = "Waste Segregation (AI)",
//                        description = "Scan your waste to get instant segregation tips",
//                        icon = Icons.Default.CameraAlt,
//                        gradient = Brush.linearGradient(colors = listOf(Color(0xFFA5D6A7), Color(0xFF66BB6A))),
//                        onClick = {
//                            val intent = Intent(context, ScannerActivity::class.java)
//                            context.startActivity(intent)
//                        }
//                    )

                    FeatureCard(
                        title = "SwaBhaVi (Raise Complaint)",
                        description = "Report missed pickups or illegal dumping",
                        icon = Icons.Default.ReportProblem,
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFCC80),
                                Color(0xFFFFA726)
                            )
                        ),
                        onClick = { navController.navigate("complaint/RESIDENT") }
                    )
                }
            }

            // --- Tip of the day ---
            item {
                TipOfTheDayCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


// --- Tip of the Day Card ---
@Composable
fun TipOfTheDayCard() {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Tip",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tip of the Day",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Separate wet and dry waste daily to keep your neighborhood clean and green!",
                fontSize = 14.sp,
                color = contentColor.copy(alpha = 0.9f)
            )
        }
    }
}

// --- Feature Card (Using colorful gradients) ---
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