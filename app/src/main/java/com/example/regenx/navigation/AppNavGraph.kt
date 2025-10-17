////package com.example.regenx.navigation
////
////import androidx.compose.runtime.Composable
////import androidx.navigation.compose.NavHost
////import androidx.navigation.compose.composable
////import androidx.navigation.compose.rememberNavController
////import com.example.regenx.screens.shared.HomeScreen
////import com.example.regenx.screens.shared.AuthChoiceScreen
////import com.example.regenx.screens.shared.LoginScreen
////import com.example.regenx.screens.shared.SignupScreen
////import com.example.regenx.screens.shared.DashboardScreen
////import com.example.regenx.screens.officials.ViewComplaintsScreen
////import com.example.regenx.screens.shared.RaiseComplaintScreen
////
////@Composable
////fun AppNavGraph() {
////    val navController = rememberNavController()
////    NavHost(navController, startDestination = "home") {
////        composable("home") { HomeScreen(navController) }
////        composable("authChoice") { AuthChoiceScreen(navController) }
////        composable("login") { LoginScreen(navController) }
////        composable("signup") { SignupScreen(navController) }
////        composable("login/{userType}") { backStackEntry ->
////            val userType = backStackEntry.arguments?.getString("userType") ?: "resident"
////            LoginScreen(navController)
////        }
////        composable("signup/{userType}") { backStackEntry ->
////            val userType = backStackEntry.arguments?.getString("userType") ?: "resident"
////            SignupScreen(navController)
////        }
////        composable("dashboard/{userType}") { backStackEntry ->
////            val userType = backStackEntry.arguments?.getString("userType") ?: "resident"
////            DashboardScreen(userType)
////        }
////
////        // 🔹 Resident routes
////        composable("residentDashboard") { com.example.regenx.screens.residents.ResidentDashboard(navController) }
////        composable("locateGarbageTruck") { com.example.regenx.screens.residents.LocateGarbageTruckScreen() }
////
////        // 🔹 Collector & official routes
////        composable("collectorDashboard") { com.example.regenx.screens.collectors.CollectorDashboard(navController) }
////        composable("officialDashboard") { com.example.regenx.screens.officials.OfficialDashboard(navController) }
////
////        // 🔹 Shared features
////        composable("raiseComplaint") { RaiseComplaintScreen(navController) }
////        composable("viewComplaints") { ViewComplaintsScreen(navController) }
////        composable("settings") { com.example.regenx.screens.shared.SettingsScreen(navController) }
////        composable("profileScreen") { com.example.regenx.screens.shared.ProfileScreen(navController) }
////    }
////}
//
//
//package com.example.regenx.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.regenx.screens.shared.HomeScreen
//import com.example.regenx.screens.shared.RoleChoice
//import com.example.regenx.screens.shared.LoginScreen
//import com.example.regenx.screens.shared.SignupScreen
//import com.example.regenx.screens.officials.ViewComplaintsScreen
//import com.example.regenx.screens.shared.RaiseComplaintScreen
//import com.example.regenx.screens.shared.SplashScreen
//
//@Composable
//fun AppNavGraph() {
//    val navController = rememberNavController()
//
//    NavHost(navController, startDestination = "splash") {
//
//        composable("splash") { SplashScreen(navController) }
//
//        // Landing page
//        composable("home") { HomeScreen(navController) }
//
//        // Role selection
//        composable("roleChoice") { RoleChoice(navController) }
//
//        // Login with role parameter
//        composable("login/{userType}") { backStackEntry ->
//            val userType = backStackEntry.arguments?.getString("userType") ?: "RESIDENT"
//            LoginScreen(navController, userType)
//        }
//
//        // Signup with role parameter
//        composable("signup/{userType}") { backStackEntry ->
//            val userType = backStackEntry.arguments?.getString("userType") ?: "RESIDENT"
//            SignupScreen(navController, userType)
//        }
//
//
//        // 🔹 Resident routes
//        composable("residentDashboard") {
//            com.example.regenx.screens.residents.ResidentDashboard(navController)
//        }
//        composable("locateGarbageTruck") {
//            com.example.regenx.screens.residents.LocateGarbageTruckScreen()
//        }
//
//        // 🔹 Collector & official routes
//        composable("collectorDashboard") {
//            com.example.regenx.screens.collectors.CollectorDashboard(navController)
//        }
//        composable("officialDashboard") {
//            com.example.regenx.screens.officials.OfficialDashboard(navController)
//        }
////        composable("scrapBuyerDashboard") {
////            com.example.regenx.screens.collectors.scrap.ScrapBuyerDashboard(navController)
////        }
//
//        // 🔹 Shared features
//        composable("raiseComplaint") { RaiseComplaintScreen(navController) }
//        composable("viewComplaints") { ViewComplaintsScreen(navController) }
//        composable("settings") { com.example.regenx.screens.shared.SettingsScreen(navController) }
//        composable("profileScreen") { com.example.regenx.screens.shared.ProfileScreen(navController) }
//    }
//}
//




package com.example.regenx.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.regenx.screens.shared.HomeScreen
import com.example.regenx.screens.shared.RoleChoice
import com.example.regenx.screens.shared.LoginScreen
import com.example.regenx.screens.shared.SignupScreen
import com.example.regenx.screens.officials.ViewComplaintsScreen
import com.example.regenx.screens.shared.RaiseComplaintScreen
import com.example.regenx.screens.shared.SplashScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "splash") {

        // Splash
        composable("splash") { SplashScreen(navController) }

        // Landing page
        composable("home") { HomeScreen(navController) }

        // Role selection
        composable("roleChoice") { RoleChoice(navController) }

        // Login with role
        composable("login/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "RESIDENT"
            LoginScreen(navController, userType)
        }

        // Signup with role
        composable("signup/{userType}") { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "RESIDENT"
            SignupScreen(navController, userType)
        }

        // 🔹 Resident routes
        composable("residentDashboard") {
            com.example.regenx.screens.residents.ResidentDashboard(navController)
        }
        composable("locateGarbageTruck") {
            com.example.regenx.screens.residents.LocateGarbageTruckScreen()
        }

        // 🔹 Collector & official routes
        composable("collectorDashboard") {
            com.example.regenx.screens.collectors.CollectorDashboard(navController)
        }
        composable("officialDashboard") {
            com.example.regenx.screens.officials.OfficialDashboard(navController)
        }
        composable("scrapDashboard") {
            com.example.regenx.screens.collectors.scrapCollectors.ScrapCollectorDashboard(navController)
        }

        composable("complaint/{role}") { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "resident"
            com.example.regenx.screens.shared.RaiseComplaintScreen(navController)
        }


        // 🔹 Shared features
        composable("viewComplaints") { ViewComplaintsScreen(navController) }
        composable("settings") { com.example.regenx.screens.shared.SettingsScreen(navController) }
        composable("profileScreen") { com.example.regenx.screens.shared.ProfileScreen(navController) }
    }
}
