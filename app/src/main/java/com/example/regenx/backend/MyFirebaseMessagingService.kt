package com.example.regenx.backend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.regenx.R // Ensure R.drawable.ic_truck_alert or similar exists
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.Manifest
import android.content.pm.PackageManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "ResidentFCMService"

    // --- Configuration for Geofence Alerts ---
    private val ALERT_CHANNEL_ID = "regenx_geofence_alerts"
    private val ALERT_CHANNEL_NAME = "Truck Arrival Alerts"
    // -----------------------------------------

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Step 1: Crucial—Save the token to the current user's document in Firestore.
        sendTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Your Cloud Function sends the notification payload via the 'notification' field.
        remoteMessage.notification?.let {
            val title = it.title ?: "RegenX Alert"
            val body = it.body ?: "The garbage truck is nearby."

            Log.d(TAG, "Notification received. Title: $title, Body: $body")
            showNotification(title, body)
        }

        // Ensure you also handle data payload if your Cloud Function sends custom data (e.g., truckId)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
            // Optional: Process remoteMessage.data here, e.g., display a custom map notification
        }
    }

    /**
     * Saves the current FCM token to the resident's Firestore document.
     */
    private fun sendTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e(TAG, "User not authenticated. Cannot save FCM token.")
            return
        }

        val tokenMap = hashMapOf(
            "fcmToken" to token,
            "tokenLastUpdated" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("residents")
            .document(userId)
            .set(tokenMap as Map<String, Any>, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(TAG, "FCM token saved successfully for resident $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save FCM token for resident $userId", e)
            }
    }

    /**
     * Creates and displays the geofence alert notification using the dedicated channel.
     */
    private fun showNotification(title: String, message: String) {
        val channelId = "regenx_geofence_alerts" // Use your defined channel ID
        val notificationId = System.currentTimeMillis().toInt()

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Truck Arrival Alerts", // Use your defined channel name
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when the garbage truck is near your home."
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notification = builder.build()

        // --- FIX: Explicitly check for POST_NOTIFICATIONS permission ---
        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            // Check if the POST_NOTIFICATIONS permission is granted
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted.
                // Since this is a Service, we cannot request the permission directly.
                // We must exit gracefully without showing the notification.
                Log.w("ResidentFCMService", "Notification permission (POST_NOTIFICATIONS) denied. Cannot display alert.")
                return
            }
        }
        // --- END FIX ---

        // Display the push notification
        notificationManager.notify(notificationId, notification)
    }
}