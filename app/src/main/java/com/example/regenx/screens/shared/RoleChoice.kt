//package com.example.regenx.screens.shared
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.regenx.R
//
//@Composable
//fun RoleChoice(navController: NavController) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Add the image on top, centered
//        Image(
//            painter = painterResource(id = R.drawable.roles), // Replace with your image resource name
//            contentDescription = "Role Choice Illustration",
//            modifier = Modifier
//                .size(180.dp)
//                .padding(bottom = 24.dp)
//        )
//
//        Text(
//            text = "Select Your Role",
//            fontSize = 24.sp,
//            fontWeight = FontWeight.Bold,
//            style = MaterialTheme.typography.headlineMedium
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        Button(
//            onClick = { navController.navigate("login/RESIDENT") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Resident", fontSize = 18.sp)
//        }
//
//        Button(
//            onClick = { navController.navigate("login/COLLECTOR") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Collector", fontSize = 18.sp)
//        }
//
//        Button(
//            onClick = { navController.navigate("login/OFFICIAL") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Official", fontSize = 18.sp)
//        }
//
//        // ✅ New Scrap Buyer button
//        Button(
//            onClick = { navController.navigate("login/SCRAP_BUYER") },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 8.dp),
//            shape = RoundedCornerShape(12.dp)
//        ) {
//            Text("Scrap Buyer", fontSize = 18.sp)
//        }
//    }
//}








package com.example.regenx.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.regenx.R

// Define adaptive Material 3 Color Schemes for Light and Dark modes
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A), // Green for primary action
    onPrimary = Color.Black,
    surface = Color(0xFF1E1E1E),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    // Secondary/Tertiary colors are now unused for buttons, but kept for general theme
    secondary = Color(0xFF9CCC65),
    tertiaryContainer = Color(0xFF3A3A3A),
    onTertiaryContainer = Color(0xFFE0E0E0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF43A047), // Deep Green for primary action
    onPrimary = Color.White,
    surface = Color.White,
    background = Color(0xFFF0F2F5),
    onBackground = Color.Black,
    secondary = Color(0xFF8BC34A),
    tertiaryContainer = Color(0xFFE0E0E0),
    onTertiaryContainer = Color(0xFF333333)
)

@Composable
fun RoleChoice(navController: NavController) {
    val useDarkTheme = isSystemInDarkTheme()

    MaterialTheme(colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Image ---
            Image(
                painter = painterResource(id = R.drawable.roles),
                contentDescription = "Role Choice Illustration",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 32.dp)
            )

            Text(
                text = "Welcome to RegenX",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Please Select Your Role to Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- Buttons ---
            // Increased vertical padding to 10.dp for more space between buttons
            val buttonSpacing = 10.dp
            val buttonColor = MaterialTheme.colorScheme.primary // Use primary color for all

            RoleButton(
                text = "Resident",
                onClick = { navController.navigate("login/RESIDENT") },
                color = buttonColor,
                verticalPadding = buttonSpacing // Apply spacing
            )

            RoleButton(
                text = "Collector",
                onClick = { navController.navigate("login/COLLECTOR") },
                color = buttonColor,
                verticalPadding = buttonSpacing // Apply spacing
            )

            RoleButton(
                text = "Official",
                onClick = { navController.navigate("login/OFFICIAL") },
                color = buttonColor,
                verticalPadding = buttonSpacing // Apply spacing
            )

            RoleButton(
                text = "Scrap Buyer",
                onClick = { navController.navigate("login/SCRAP_BUYER") },
                color = buttonColor,
                verticalPadding = buttonSpacing // Apply spacing
            )
        }
    }
}

@Composable
private fun RoleButton(text: String, onClick: () -> Unit, color: Color, verticalPadding: androidx.compose.ui.unit.Dp) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            // Use the provided verticalPadding for spacing
            .padding(vertical = verticalPadding),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        colors = ButtonDefaults.buttonColors(
            // Use the consistent color provided
            containerColor = color,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}