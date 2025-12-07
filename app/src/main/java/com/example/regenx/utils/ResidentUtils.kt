package com.example.regenx.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

/**
 * Geocodes the given resident address and stores latitude/longitude
 * (and FCM token) on the SAME document in the 'residents' collection.
 *
 * Document: residents/{residentUid}
 */
fun geocodeAndCreateResidentEntry(
    context: Context,
    residentUid: String,
    address: String
) {
    val firestore = FirebaseFirestore.getInstance()

    if (address.isBlank()) {
        Log.e("Geocoding", "User address is empty. Cannot geocode.")
        return
    }

    // 1. Get the FCM Token (for notifications / geofencing)
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->

            // 2. Perform Geocoding on the provided address
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(address, 1)

                if (addresses.isNullOrEmpty()) {
                    Log.e("Geocoding", "Could not find coordinates for address: $address")
                    return@addOnSuccessListener
                }

                val lat = addresses[0].latitude
                val lng = addresses[0].longitude

                // 3. Update existing resident document with lat/lng + token
                val residentData: Map<String, Any> = mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "token" to token
                )

                firestore.collection("residents")
                    .document(residentUid)
                    .update(residentData)
                    .addOnSuccessListener {
                        Log.d(
                            "Geocoding",
                            "✅ Resident entry updated with coordinates: $lat, $lng"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "Geocoding",
                            "❌ Failed to update resident entry with lat/lng: ${e.message}"
                        )
                    }

            } catch (e: Exception) {
                Log.e("Geocoding", "Geocoding failed due to exception: ${e.message}", e)
            }
        }
        .addOnFailureListener { e ->
            Log.e("Geocoding", "Failed to fetch FCM token: ${e.message}", e)
        }
}