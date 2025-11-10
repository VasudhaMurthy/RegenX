package com.example.regenx.screens.shared

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaiseComplaintScreen(navController: NavController, role: String) { // Assuming role parameter is passed
    val context = LocalContext.current
    val auth = Firebase.auth
    val firestore = Firebase.firestore
    val storage = Firebase.storage

    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Error message for missing required fields
    var submissionError by remember { mutableStateOf<String?>(null) }

    // Function to create image file
    fun createImageFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // NOTE: Corrected getExternalFilesDir to getExternalFilesFilesDir (if that was the intention) or just getExternalFilesDir
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    // Take picture launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imageUri = null
        else submissionError = null // Clear error if photo is successfully taken
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // If granted, launch camera
            val photoFile = createImageFile(context)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            imageUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Raise Complaint") })
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subject
            OutlinedTextField(
                value = subject,
                onValueChange = {
                    subject = it
                    submissionError = null // Clear error on input change
                },
                label = { Text("Subject* (e.g., Missed Pickup)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    submissionError = null // Clear error on input change
                },
                label = { Text("Description*") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Status Indicator (Shows it's optional)
            if (imageUri != null) {
                Text("Photo Captured ✅ (Optional)", color = Color(0xFF4CAF50))
            } else {
                Text("Photo is optional.", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))


            // Take Photo button with permission check
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val photoFile = createImageFile(context)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        imageUri = uri
                        takePictureLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Take Photo (Optional)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Preview Image
            imageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Complaint Photo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Validation and Submit Logic
            submissionError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    // --- REVISED VALIDATION: Only subject and description are required ---
                    if (subject.isBlank() || description.isBlank()) {
                        submissionError = "Subject and Description are required to file a complaint."
                        return@Button
                    }
                    // ---------------------------------

                    val uid = auth.currentUser?.uid ?: "anonymous"

                    // Logic branch: Check if photo was taken
                    if (imageUri != null) {
                        // --- PATH A: UPLOAD PHOTO ---
                        isUploading = true
                        val fileName = "complaints/${uid}_${System.currentTimeMillis()}.jpg"
                        val ref = storage.reference.child(fileName)
                        val uploadTask = ref.putFile(imageUri!!)

                        uploadTask.addOnSuccessListener {
                            ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                                saveComplaint(uid, subject, description, downloadUrl.toString(), role, context, navController)
                            }
                        }.addOnFailureListener {
                            isUploading = false
                            Toast.makeText(context, "Image upload failed. Submitting text only.", Toast.LENGTH_LONG).show()
                            // Fallback to save without photo if upload fails
                            saveComplaint(uid, subject, description, null, role, context, navController)
                        }
                    } else {
                        // --- PATH B: NO PHOTO ---
                        saveComplaint(uid, subject, description, null, role, context, navController)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isUploading) "Uploading Photo..." else "Submit Complaint")
            }
        }
    }
}

// Helper function to save complaint data (used for both photo and no-photo paths)
private fun saveComplaint(
    uid: String,
    subject: String,
    description: String,
    photoUrl: String?, // Now nullable
    role: String,
    context: Context,
    navController: NavController
) {
    val firestore = Firebase.firestore

    val complaintData = hashMapOf<String, Any>(
        "userId" to uid,
        "subject" to subject,
        "description" to description,
        "timestamp" to System.currentTimeMillis(),
        "status" to "pending",
        "role" to role,
        // photoUrl is added below if it exists
    )

    photoUrl?.let { url ->
        complaintData["photoUrl"] = url
    }

    firestore.collection("complaints")
        .add(complaintData)
        .addOnSuccessListener {
            // Reset state and navigate back
            Toast.makeText(context, "Complaint filed successfully!", Toast.LENGTH_LONG).show()
            // Resetting isUploading state happens in the calling branch if upload was successful
            navController.popBackStack()
        }
        .addOnFailureListener { e ->
            // In a real app, you'd reset the isUploading state here too, but since
            // this is a private function, we rely on the main onClick block for state reset.
            Toast.makeText(context, "Error saving complaint: ${e.message}", Toast.LENGTH_LONG).show()
        }
}