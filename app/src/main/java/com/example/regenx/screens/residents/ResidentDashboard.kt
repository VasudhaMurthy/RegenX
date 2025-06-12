package com.example.regenx.screens.residents

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
fun ResidentDashboard(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resident Dashboard") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome, Resident!",
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("raiseComplaint") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SwaBhaVi (Raise Complaint)")
            }
        }
    }
}
