package com.example.regenx.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.regenx.screens.shared.HomeScreen
import com.example.regenx.screens.shared.AuthChoiceScreen
import com.example.regenx.screens.shared.LoginScreen
import com.example.regenx.screens.shared.SignupScreen
import com.example.regenx.screens.shared.DashboardScreen
import com.example.regenx.screens.officials.ViewComplaintsScreen
import com.example.regenx.screens.shared.RaiseComplaintScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("authChoice") { AuthChoiceScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("login/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "resident"
            LoginScreen(navController)
        }
        composable("signup/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "resident"
            SignupScreen(navController)
        }
        composable("dashboard/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "resident"
            DashboardScreen(userType)
        }
        composable("residentDashboard") { com.example.regenx.screens.residents.ResidentDashboard(navController) }
        composable("collectorDashboard") { com.example.regenx.screens.collectors.CollectorDashboard(navController) }
        composable("officialDashboard") { com.example.regenx.screens.officials.OfficialDashboard(navController) }
        composable("raiseComplaint") { RaiseComplaintScreen(navController) }
        composable("viewComplaints") { ViewComplaintsScreen(navController) }
        composable("settings") { com.example.regenx.screens.shared.SettingsScreen(navController) } // ✅ Add this line
        composable("profileScreen") { com.example.regenx.screens.shared.ProfileScreen(navController) }

    }


}
