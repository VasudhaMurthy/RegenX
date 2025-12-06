package com.example.regenx.screens.residents

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class ResidentAlertListener(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val residentId = auth.currentUser?.uid

    private val channelId = "resident_alerts_channel"

    init {
        createNotificationChannel()

        if (residentId == null) {
            Log.w("ResidentAlertListener", "Resident not logged in. Listener not started.")
        } else {
            listenForAlerts()
        }
    }

    private fun listenForAlerts() {
        firestore.collection("residents")
            .document(residentId!!)
            .collection("alerts")
            .orderBy("timestamp")
            .addSnapshotListener { snaps, e ->
                if (e != null) {
                    Log.e("ResidentAlertListener", "Error in alert listener", e)
                    return@addSnapshotListener
                }
                if (snaps == null) return@addSnapshotListener

                for (dc in snaps.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val data = dc.document.data
                        showNotification(data)
                    }
                }
            }
    }

    private fun showNotification(data: Map<String, Any>) {
        try {
            val message = data["message"] as? String ?: "Garbage truck nearby"
            val distance = (data["distanceMeters"] as? Number)?.toDouble() ?: 0.0

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Truck arrived")
                .setContentText("$message — ${"%.0f".format(distance)} m away")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            nm.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            Log.e("ResidentAlertListener", "showNotification failed", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Resident Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
