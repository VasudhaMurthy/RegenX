package com.example.regenx.screens.scrapbuyer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapBuyerSettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scrap Buyer Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SettingOption("Edit Profile", Icons.Default.Person) {
                // TODO: Navigate to Edit Profile Screen
            }
            SettingOption("Purchase History", Icons.Default.ShoppingCart) {
                // TODO: Navigate to Purchase History Screen
            }
            SettingOption("Notifications", Icons.Default.Notifications) {
                // TODO: Manage notifications
            }
            SettingOption("Help & Support", Icons.Default.HelpOutline) {
                // TODO: Navigate or show contact info
            }
            SettingOption("Logout", Icons.Default.ExitToApp) {
                FirebaseAuth.getInstance().signOut()
                navController.navigate("splash") {
                    popUpTo(0)
                }
            }
        }
    }
}

@Composable
fun SettingOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}
