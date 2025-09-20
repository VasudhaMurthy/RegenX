//package com.example.regenx.screens.shared
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.text.ClickableText
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.regenx.R
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//
//enum class UserType { RESIDENT, COLLECTOR, OFFICIAL }
//
//@Composable
//fun SignupScreen(navController: NavController, role: String) {
//    val auth = FirebaseAuth.getInstance()
//    val firestore = FirebaseFirestore.getInstance()
//
//    var userType by remember { mutableStateOf(UserType.valueOf(role)) }
//
//    // Common fields
//    var firstName by remember { mutableStateOf("") }
//    var middleName by remember { mutableStateOf("") }
//    var lastName by remember { mutableStateOf("") }
//    var phone by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//    var confirmPasswordVisible by remember { mutableStateOf(false) }
//
//    // Role-specific fields
//    var aadhar by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var address by remember { mutableStateOf("") }  // resident-specific
//
//    var vehicleNo by remember { mutableStateOf("") }
//    var collectorId by remember { mutableStateOf("") }  // instead of area collecting
//    var licenseNo by remember { mutableStateOf("") }
//
//    var govtRole by remember { mutableStateOf("") }
//    var govtId by remember { mutableStateOf("") }
//    var areaLeading by remember { mutableStateOf("") }
//    var govtEmail by remember { mutableStateOf("") }
//
//    // Dropdown toggle
//    var expanded by remember { mutableStateOf(false) }
//
//    val scrollState = rememberScrollState()
//
//    // Error messages
//    var passwordError by remember { mutableStateOf("") }
//    var fieldErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(scrollState)
//            .padding(horizontal = 16.dp)
//            .padding(top = 80.dp),
//        verticalArrangement = Arrangement.Top,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // Role selection
//        Box {
//            Button(onClick = { expanded = !expanded }) {
//                Text("Role: ${userType.name}")
//                Icon(
//                    painter = painterResource(id = R.drawable.arrow_drop_down),
//                    contentDescription = "Toggle Role",
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//                UserType.values().forEach { type ->
//                    DropdownMenuItem(
//                        text = { Text(type.name) },
//                        onClick = {
//                            userType = type
//                            expanded = false
//                        }
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Common Fields
//        OutlinedTextField(
//            value = firstName,
//            onValueChange = { firstName = it },
//            label = { Text("First Name*") },
//            modifier = Modifier.fillMaxWidth(),
//            isError = fieldErrors.containsKey("firstName")
//        )
//        fieldErrors["firstName"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//        OutlinedTextField(
//            value = middleName,
//            onValueChange = { middleName = it },
//            label = { Text("Middle Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        OutlinedTextField(
//            value = lastName,
//            onValueChange = { lastName = it },
//            label = { Text("Last Name*") },
//            modifier = Modifier.fillMaxWidth(),
//            isError = fieldErrors.containsKey("lastName")
//        )
//        fieldErrors["lastName"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Role-specific Fields
//        when (userType) {
//            UserType.RESIDENT -> {
//                OutlinedTextField(
//                    value = aadhar,
//                    onValueChange = { aadhar = it },
//                    label = { Text("Aadhar Card No*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("aadhar")
//                )
//                fieldErrors["aadhar"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = address,
//                    onValueChange = { address = it },
//                    label = { Text("Address*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("address")
//                )
//                fieldErrors["address"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    label = { Text("Phone Number*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("phone")
//                )
//                fieldErrors["phone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = { email = it },
//                    label = { Text("Email ID*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("email")
//                )
//                fieldErrors["email"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//            }
//
//            UserType.COLLECTOR -> {
//                OutlinedTextField(
//                    value = vehicleNo,
//                    onValueChange = { vehicleNo = it },
//                    label = { Text("Vehicle Number*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("vehicleNo")
//                )
//                fieldErrors["vehicleNo"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = collectorId,
//                    onValueChange = { collectorId = it },
//                    label = { Text("Collector ID*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("collectorId")
//                )
//                fieldErrors["collectorId"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    label = { Text("Phone Number*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("phone")
//                )
//                fieldErrors["phone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = licenseNo,
//                    onValueChange = { licenseNo = it },
//                    label = { Text("License Number*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("licenseNo")
//                )
//                fieldErrors["licenseNo"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//            }
//
//            UserType.OFFICIAL -> {
//                OutlinedTextField(
//                    value = govtRole,
//                    onValueChange = { govtRole = it },
//                    label = { Text("Role in Govt*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("govtRole")
//                )
//                fieldErrors["govtRole"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = govtId,
//                    onValueChange = { govtId = it },
//                    label = { Text("Government ID*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("govtId")
//                )
//                fieldErrors["govtId"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = areaLeading,
//                    onValueChange = { areaLeading = it },
//                    label = { Text("Area Leading*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("areaLeading")
//                )
//                fieldErrors["areaLeading"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = govtEmail,
//                    onValueChange = { govtEmail = it },
//                    label = { Text("Government Email ID*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("govtEmail")
//                )
//                fieldErrors["govtEmail"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//                OutlinedTextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    label = { Text("Phone Number*") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = fieldErrors.containsKey("phone")
//                )
//                fieldErrors["phone"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Password
//        OutlinedTextField(
//            value = password,
//            onValueChange = {
//                password = it
//                passwordError = if (confirmPassword.isNotEmpty() && confirmPassword != password) {
//                    "Passwords do not match"
//                } else ""
//            },
//            label = { Text("Password*") },
//            modifier = Modifier.fillMaxWidth(),
//            isError = fieldErrors.containsKey("password"),
//            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            trailingIcon = {
//                IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                    Icon(
//                        painter = painterResource(id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off),
//                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
//                        modifier = Modifier.size(28.dp)
//                    )
//                }
//            }
//        )
//        fieldErrors["password"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
//
//        // Confirm Password
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = {
//                confirmPassword = it
//                passwordError = if (confirmPassword != password && confirmPassword.isNotEmpty()) {
//                    "Passwords do not match"
//                } else ""
//            },
//            label = { Text("Confirm Password*") },
//            modifier = Modifier.fillMaxWidth(),
//            isError = passwordError.isNotEmpty(),
//            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//            trailingIcon = {
//                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
//                    Icon(
//                        painter = painterResource(id = if (confirmPasswordVisible) R.drawable.visibility else R.drawable.visibility_off),
//                        contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password",
//                        modifier = Modifier.size(28.dp)
//                    )
//                }
//            }
//        )
//        if (passwordError.isNotEmpty()) {
//            Text(passwordError, color = MaterialTheme.colorScheme.error)
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Submit
//        Button(
//            onClick = {
//                val errors = mutableMapOf<String, String>()
//                passwordError = ""
//
//                // Validate required fields
//                if (firstName.isBlank()) errors["firstName"] = "First name is required"
//                if (lastName.isBlank()) errors["lastName"] = "Last name is required"
//                if (phone.isBlank()) errors["phone"] = "Phone number is required"
//                if (password.isBlank()) errors["password"] = "Password is required"
//                if (confirmPassword.isBlank()) passwordError = "Please confirm your password"
//                if (password != confirmPassword) passwordError = "Passwords do not match"
//
//                when (userType) {
//                    UserType.RESIDENT -> {
//                        if (aadhar.isBlank()) errors["aadhar"] = "Aadhar number is required"
//                        if (address.isBlank()) errors["address"] = "Address is required"
//                        if (email.isBlank()) errors["email"] = "Email is required"
//                    }
//                    UserType.COLLECTOR -> {
//                        if (vehicleNo.isBlank()) errors["vehicleNo"] = "Vehicle number is required"
//                        if (collectorId.isBlank()) errors["collectorId"] = "Collector ID is required"
//                        if (licenseNo.isBlank()) errors["licenseNo"] = "License number is required"
//                    }
//                    UserType.OFFICIAL -> {
//                        if (govtRole.isBlank()) errors["govtRole"] = "Government role is required"
//                        if (govtId.isBlank()) errors["govtId"] = "Government ID is required"
//                        if (areaLeading.isBlank()) errors["areaLeading"] = "Area leading is required"
//                        if (govtEmail.isBlank()) errors["govtEmail"] = "Government email is required"
//                    }
//                }
//
//                fieldErrors = errors
//
//                // ❌ If there are errors, don't submit, just show them in red
//                if (errors.isNotEmpty() || passwordError.isNotEmpty()) {
//                    return@Button
//                }
//
//                // ✅ If no errors, proceed with Firebase signup
//                val signupEmail = when (userType) {
//                    UserType.COLLECTOR -> "$collectorId@collectors.regenx.com"
//                    UserType.OFFICIAL -> govtEmail
//                    else -> email
//                }
//
//                auth.createUserWithEmailAndPassword(signupEmail, password)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
//                            val userData = hashMapOf(
//                                "firstName" to firstName,
//                                "middleName" to middleName,
//                                "lastName" to lastName,
//                                "phone" to phone,
//                                "role" to userType.name,
//                                "email" to signupEmail
//                            )
//
//                            when (userType) {
//                                UserType.RESIDENT -> {
//                                    userData["aadhar"] = aadhar
//                                    userData["address"] = address
//                                }
//                                UserType.COLLECTOR -> {
//                                    userData["vehicleNo"] = vehicleNo
//                                    userData["collectorId"] = collectorId
//                                    userData["licenseNo"] = licenseNo
//                                }
//                                UserType.OFFICIAL -> {
//                                    userData["govtRole"] = govtRole
//                                    userData["govtId"] = govtId
//                                    userData["areaLeading"] = areaLeading
//                                    userData["govtEmail"] = govtEmail
//                                }
//                            }
//
//                            firestore.collection("users").document(uid).set(userData)
//                                .addOnSuccessListener {
//                                    navController.navigate("login/${userType.name}")
//                                }
//                                .addOnFailureListener { e ->
//                                    passwordError = "Error saving user: ${e.message}"
//                                }
//
//                        } else {
//                            passwordError = task.exception?.localizedMessage ?: "Signup failed"
//                        }
//                    }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text("Submit")
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        ClickableText(
//            text = AnnotatedString("Already have an account? Login"),
//            style = LocalTextStyle.current.copy(
//                fontSize = 18.sp, // Bigger text
//                color = MaterialTheme.colorScheme.primary
//            ),
//            onClick = {
//                navController.navigate("login/${userType.name}")
//            }
//        )
//
//        Spacer(modifier = Modifier.height(24.dp))
//    }
//}








package com.example.regenx.screens.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// ✅ Added SCRAP_BUYER role
enum class UserType { RESIDENT, COLLECTOR, OFFICIAL, SCRAP_BUYER }

@Composable
fun SignupScreen(navController: NavController, role: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

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

    // Scrap Buyer (✅ New role)
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
                val signupEmail = when (userType) {
                    UserType.COLLECTOR -> "$collectorId@collectors.regenx.com"
                    UserType.OFFICIAL -> govtEmail
                    UserType.SCRAP_BUYER -> scrapEmail
                    else -> email
                }

                auth.createUserWithEmailAndPassword(signupEmail, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                            val userData = hashMapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "phone" to phone,
                                "role" to userType.name,
                                "email" to signupEmail
                            )

                            when (userType) {
                                UserType.RESIDENT -> {
                                    userData["aadhar"] = aadhar
                                    userData["address"] = address
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

                            firestore.collection("users").document(uid).set(userData)
                                .addOnSuccessListener {
                                    navController.navigate("login/${userType.name}")
                                }
                                .addOnFailureListener { e ->
                                    passwordError = "Error saving user: ${e.message}"
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
    }
}
