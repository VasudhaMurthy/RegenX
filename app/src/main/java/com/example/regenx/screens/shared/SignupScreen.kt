package com.example.regenx.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Needed for Context
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.regenx.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.regenx.utils.geocodeAndCreateResidentEntry // Geocoding Import

// ✅ Added SCRAP_BUYER role
enum class UserType { RESIDENT, COLLECTOR, OFFICIAL, SCRAP_BUYER }

@Composable
fun SignupScreen(navController: NavController, role: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current // Context for Geocoding

    var userType by remember { mutableStateOf(UserType.valueOf(role)) }

    // Common fields
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Resident
    var aadhar by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Collector
    var vehicleNo by remember { mutableStateOf("") }
    var collectorId by remember { mutableStateOf("") }
    var licenseNo by remember { mutableStateOf("") }

    // Official
    var govtRole by remember { mutableStateOf("") }
    var govtId by remember { mutableStateOf("") }
    var areaLeading by remember { mutableStateOf("") }
    var govtEmail by remember { mutableStateOf("") }

    // Scrap Buyer
    var businessName by remember { mutableStateOf("") }
    var scrapLicense by remember { mutableStateOf("") }
    var scrapEmail by remember { mutableStateOf("") }

    // Errors
    var passwordError by remember { mutableStateOf("") }
    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 80.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sign Up as ${userType.name}", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Common fields
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name*") },
            modifier = Modifier.fillMaxWidth(),
            isError = fieldErrors.containsKey("firstName")
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name*") },
            modifier = Modifier.fillMaxWidth(),
            isError = fieldErrors.containsKey("lastName")
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number*") },
            modifier = Modifier.fillMaxWidth(),
            isError = fieldErrors.containsKey("phone")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role-specific fields
        when (userType) {
            UserType.RESIDENT -> {
                OutlinedTextField(value = aadhar, onValueChange = { aadhar = it }, label = { Text("Aadhar No*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email*") }, modifier = Modifier.fillMaxWidth())
            }
            UserType.COLLECTOR -> {
                OutlinedTextField(value = vehicleNo, onValueChange = { vehicleNo = it }, label = { Text("Vehicle No*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = collectorId, onValueChange = { collectorId = it }, label = { Text("Collector ID*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = licenseNo, onValueChange = { licenseNo = it }, label = { Text("License No*") }, modifier = Modifier.fillMaxWidth())
            }
            UserType.OFFICIAL -> {
                OutlinedTextField(value = govtRole, onValueChange = { govtRole = it }, label = { Text("Government Role*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = govtId, onValueChange = { govtId = it }, label = { Text("Government ID*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = areaLeading, onValueChange = { areaLeading = it }, label = { Text("Area Leading*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = govtEmail, onValueChange = { govtEmail = it }, label = { Text("Government Email*") }, modifier = Modifier.fillMaxWidth())
            }
            UserType.SCRAP_BUYER -> {
                OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business Name*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = scrapLicense, onValueChange = { scrapLicense = it }, label = { Text("Scrap License No*") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = scrapEmail, onValueChange = { scrapEmail = it }, label = { Text("Business Email*") }, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Password + Confirm Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (confirmPassword.isNotEmpty() && confirmPassword != password) {
                    passwordError = "Passwords do not match"
                } else passwordError = ""
            },
            label = { Text("Password*") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off),
                        contentDescription = null
                    )
                }
            }
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (confirmPassword != password) passwordError = "Passwords do not match"
                else passwordError = ""
            },
            label = { Text("Confirm Password*") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = if (confirmPasswordVisible) R.drawable.visibility else R.drawable.visibility_off),
                        contentDescription = null
                    )
                }
            }
        )
        if (passwordError.isNotEmpty()) {
            Text(passwordError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // 1. Basic validation
                if (password.length < 6 || password != confirmPassword) {
                    passwordError = "Password must be 6+ chars and match."
                    return@Button
                }

                // 2. Determine final sign-up email based on role
                val signupEmail = when (userType) {
                    UserType.COLLECTOR -> "$collectorId@collectors.regenx.com"
                    UserType.OFFICIAL -> govtEmail
                    UserType.SCRAP_BUYER -> scrapEmail
                    else -> email
                }

                // 3. Create user in Firebase Auth
                auth.createUserWithEmailAndPassword(signupEmail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid == null) {
                                passwordError = "User created but UID not found."
                                return@addOnCompleteListener
                            }

                            // 4. Prepare common user data
                            val userData = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "phone" to phone,
                                "role" to userType.name,
                                "email" to signupEmail
                            )

                            // 5. Add role-specific data
                            when (userType) {
                                UserType.RESIDENT -> {
                                    userData["aadhar"] = aadhar
                                    userData["address"] = address // Address is saved here first
                                }
                                UserType.COLLECTOR -> {
                                    userData["vehicleNo"] = vehicleNo
                                    userData["collectorId"] = collectorId
                                    userData["licenseNo"] = licenseNo
                                }
                                UserType.OFFICIAL -> {
                                    userData["govtRole"] = govtRole
                                    userData["govtId"] = govtId
                                    userData["areaLeading"] = areaLeading
                                    userData["govtEmail"] = govtEmail
                                }
                                UserType.SCRAP_BUYER -> {
                                    userData["businessName"] = businessName
                                    userData["scrapLicense"] = scrapLicense
                                }
                            }

                            // 6. 🎯 DETERMINE COLLECTION NAME FOR SEPARATE STORAGE 🎯
                            val collectionName = when (userType) {
                                UserType.RESIDENT -> "residents"
                                UserType.COLLECTOR -> "collectors"
                                UserType.OFFICIAL -> "officials"
                                UserType.SCRAP_BUYER -> "scrap_buyers"
                            }

                            // 7. 💾 SAVE USER DATA TO THE ROLE-SPECIFIC COLLECTION 💾
                            firestore.collection(collectionName).document(uid).set(userData)
                                .addOnSuccessListener {
                                    // 8. 🌟 GEOCoding for RESIDENTS 🌟 (Only needed for Resident)
                                    if (userType == UserType.RESIDENT) {
                                        geocodeAndCreateResidentEntry(context, uid, address)
                                    }

                                    // 9. Navigate to login screen
                                    navController.navigate("login/${userType.name}")
                                }
                                .addOnFailureListener { e ->
                                    // ⚠️ CRITICAL: Delete the Auth user if Firestore save fails
                                    auth.currentUser?.delete()
                                    passwordError = "Error saving user data: ${e.message}"
                                }
                        } else {
                            passwordError = task.exception?.localizedMessage ?: "Signup failed"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }

        Spacer(modifier = Modifier.height(12.dp))

        ClickableText(
            text = AnnotatedString("Already have an account? Login"),
            style = LocalTextStyle.current.copy(
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            ),
            onClick = { navController.navigate("login/${userType.name}") }
        )

        Spacer(modifier = Modifier.height(80.dp)) // Add padding at bottom for scrolling
    }
}