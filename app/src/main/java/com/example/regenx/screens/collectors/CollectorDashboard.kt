package com.example.regenx.screens.collectors

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDashboard(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collector Dashboard") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome, Collector!",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
