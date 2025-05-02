//package com.example.regenx
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Looper
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.gms.location.*
//
//class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        setContent {
//            var permissionGranted by remember { mutableStateOf(false) }
//
//            // Correct import and usage of rememberLauncherForActivityResult
//            val permissionLauncher = rememberLauncherForActivityResult(
//                contract = ActivityResultContracts.RequestPermission()
//            ) { isGranted ->
//                permissionGranted = isGranted
//            }
//
//            // Request permission on first composition
//            LaunchedEffect(Unit) {
//                if (ContextCompat.checkSelfPermission(
//                        this@MainActivity,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//                } else {
//                    permissionGranted = true
//                }
//            }
//
//            MaterialTheme {
//                if (permissionGranted) {
//                    RealTimeLocationFetcher(fusedLocationClient)
//                } else {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("Location permission is required.")
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RealTimeLocationFetcher(fusedLocationClient: FusedLocationProviderClient) {
//    var latitude by remember { mutableStateOf<Double?>(null) }
//    var longitude by remember { mutableStateOf<Double?>(null) }
//    var error by remember { mutableStateOf<String?>(null) }
//
//    val context = LocalContext.current
//
//    // LocationCallback remembers the latest location updates
//    val locationCallback = remember {
//        object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.lastLocation?.let { location ->
//                    latitude = location.latitude
//                    longitude = location.longitude
//                }
//            }
//        }
//    }
//
//    DisposableEffect(Unit) {
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            5000L
//        ).build()
//
//        if (ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            fusedLocationClient.requestLocationUpdates(
//                locationRequest,
//                locationCallback,
//                Looper.getMainLooper()
//            )
//        } else {
//            error = "Location permission not granted."
//        }
//
//        onDispose {
//            fusedLocationClient.removeLocationUpdates(locationCallback)
//        }
//    }
//
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        when {
//            error != null -> Text(error!!)
//            latitude != null && longitude != null -> Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("Garbage Truck Location (Live):")
//                Text("Latitude: $latitude")
//                Text("Longitude: $longitude")
//            }
//            else -> Text("Fetching location...")
//        }
//    }
//}

package com.example.regenx

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            val context = LocalContext.current

            var locationPermissionGranted by remember { mutableStateOf(false) }
            var notificationPermissionGranted by remember { mutableStateOf(false) }

            // Launcher for location permission
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                locationPermissionGranted = isGranted
            }

            // Launcher for notification permission (Android 13+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                notificationPermissionGranted = isGranted
            }

            // Request location permission on first composition
            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                } else {
                    locationPermissionGranted = true
                }
            }


            // Request notification permission on first composition (Android 13+)
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        notificationPermissionGranted = true
                    }
                } else {
                    // Permission not needed below Android 13
                    notificationPermissionGranted = true
                }
            }

            LaunchedEffect(Unit) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("FCM token", "Fetching FCM registration token failed", task.exception)
                        Toast.makeText(context, "Fetching FCM token failed", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }
                    val token = task.result
                    Log.d("FCM token", token ?: "Token is null")
                    Toast.makeText(context, "FCM token: $token", Toast.LENGTH_LONG).show()
                }
            }

            MaterialTheme {
                when {
                    !locationPermissionGranted -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Location permission is required.")
                        }
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Notification permission is required.")
                        }
                    }
                    else -> {
                        RealTimeLocationFetcher(fusedLocationClient)
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimeLocationFetcher(fusedLocationClient: FusedLocationProviderClient) {
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    // LocationCallback remembers the latest location updates
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            error = "Location permission not granted."
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            error != null -> Text(error!!)
            latitude != null && longitude != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Garbage Truck Location (Live):")
                Text("Latitude: $latitude")
                Text("Longitude: $longitude")
            }
            else -> Text("Fetching location...")
        }
    }
}
