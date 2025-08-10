package com.example.regenx.screens.collectors

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.regenx.backend.CollectorLocationService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectorDashboard(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity

    // 1. Set up permissions array
    val locationPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    // 2. Create the permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val allGranted = perms.values.all { it }
        if (allGranted) {
            Log.d("CollectorDashboard", "✅ Permissions granted, starting service…")
            val intent = Intent(context, CollectorLocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        } else {
            Log.e("CollectorDashboard", "❌ Location permissions denied")
        }
    }

    // 3. Only launch permission dialog when Composable first composes
    LaunchedEffect(Unit) {
        val missingPermissions = locationPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(locationPermissions)
        } else {
            Log.d("CollectorDashboard", "✅ Already have permissions, starting service…")
            val intent = Intent(context, CollectorLocationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }
    }

    // 4. UI
    Scaffold(
        topBar = { TopAppBar(title = { Text("Collector Dashboard") }) }
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
                text = "Welcome, Collector!",
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
