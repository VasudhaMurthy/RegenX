package com.example.regenx.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.regenx.screens.collectors.CollectorDashboard
import com.example.regenx.screens.collectors.CollectorSettingsScreen
import com.example.regenx.screens.collectors.PostWasteScreen
import com.example.regenx.screens.collectors.scrapCollectors.NearbyPickupRequestsScreen
import com.example.regenx.screens.collectors.scrapCollectors.ScrapCollectorDashboard
import com.example.regenx.screens.officials.ComplaintDetailsScreen
import com.example.regenx.screens.officials.ComplaintStatisticsScreen
import com.example.regenx.screens.officials.FleetManagementScreen
import com.example.regenx.screens.officials.OfficialDashboard
import com.example.regenx.screens.officials.OfficialSettingsScreen
import com.example.regenx.screens.officials.ViewComplaintsScreen
import com.example.regenx.screens.residents.LocateGarbageTruckScreen
import com.example.regenx.screens.residents.ResidentDashboard
import com.example.regenx.screens.residents.ResidentSettingsScreen
import com.example.regenx.screens.scrapbuyer.ScrapBuyerSettingsScreen
import com.example.regenx.screens.collectors.scrapCollectors.ScrapLogScreen   // ✅ ADDED
import com.example.regenx.screens.collectors.scrapCollectors.ScrapScoreScreen
import com.example.regenx.screens.shared.*
import com.example.regenx.screens.residents.AskAiScreen
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {

        // 🔹 Common Screens
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("roleChoice") { RoleChoice(navController) }

        // 🔹 Authentication
        composable("login/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "RESIDENT"
            LoginScreen(navController, userType)
        }

        composable("signup/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "RESIDENT"
            SignupScreen(navController, userType)
        }

        // 🔹 Resident Routes
        composable("residentDashboard") { ResidentDashboard(navController) }
        composable("locateGarbageTruck") { LocateGarbageTruckScreen(navController) }
        composable("resident_settings") { ResidentSettingsScreen(navController) }

        composable("askAiScreen") { AskAiScreen(navController) }

        // 🔹 Collector Routes
        composable("collectorDashboard") { CollectorDashboard(navController) }
        composable("postwasteScreen") { PostWasteScreen(navController) }
        composable("collector_settings") { CollectorSettingsScreen(navController) }

        // 🔹 Scrap Collector / Scrap Buyer Routes
        composable("scrapDashboard") { ScrapCollectorDashboard(navController) }
        composable("pickupRequests") { NearbyPickupRequestsScreen() }
        composable("scrapbuyer_settings") { ScrapBuyerSettingsScreen(navController) }

        // ✅ NEW: Collected Scrap Log (graphs)
        composable("scrapLog") { ScrapLogScreen(navController) }

        composable("scrap_score") { ScrapScoreScreen() }

        // 🔹 Official Routes
        composable("officialDashboard") { OfficialDashboard(navController) }
        composable("official_settings") { OfficialSettingsScreen(navController) }
        composable("fleetManagement") { FleetManagementScreen(navController) }

        // 🔹 Complaint Management
        composable("complaint/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "resident"
            RaiseComplaintScreen(navController, role)
        }

        composable("complaintDetails/{complaintId}") { backStackEntry ->
            val complaintId = backStackEntry.arguments?.getString("complaintId") ?: ""
            ComplaintDetailsScreen(navController, complaintId)
        }

        composable("viewComplaints") { ViewComplaintsScreen(navController) }

        // 🌟 Statistics
        composable("complaintStatistics") { ComplaintStatisticsScreen(navController) }

        // 🔹 Shared Profile / Utilities
        composable("profileScreen") { ProfileScreen(navController) }
    }
}