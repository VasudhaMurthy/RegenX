package com.example.regenx.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale

/**
 * Fetches the user's address, converts it to Lat/Lng, and stores the coordinates
 * and FCM token in the dedicated 'residents' collection.
 */
fun geocodeAndCreateResidentEntry(context: Context, residentUid: String) {
    val firestore = FirebaseFirestore.getInstance()

    // 1. Fetch the user's address from the 'users' collection
    firestore.collection("users").document(residentUid).get()
        .addOnSuccessListener { userDoc ->
            val address = userDoc.getString("address")

            if (address.isNullOrEmpty()) {
                Log.e("Geocoding", "User address is empty. Cannot geocode.")
                return@addOnSuccessListener
            }

            // 2. Get the FCM Token (Needed for notifications)
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->

                // 3. Perform Geocoding
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    // IMPORTANT: Geocoder can fail if the device lacks Google Play Services or has no network.
                    val addresses = geocoder.getFromLocationName(address, 1)

                    if (addresses.isNullOrEmpty()) {
                        Log.e("Geocoding", "Could not find coordinates for address: $address")
                        return@addOnSuccessListener
                    }

                    val lat = addresses[0].latitude
                    val lng = addresses[0].longitude

                    // 4. Create the document in the dedicated 'residents' collection
                    val residentData = hashMapOf(
                        "address" to address,
                        "lat" to lat,
                        "lng" to lng,
                        "userId" to residentUid,
                        "token" to token // CRITICAL for geofencing
                    )

                    firestore.collection("residents").document(residentUid).set(residentData)
                        .addOnSuccessListener {
                            Log.d("Geocoding", "✅ Resident entry created with coordinates: $lat, $lng")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Geocoding", "❌ Failed to save resident entry: ${e.message}")
                        }

                } catch (e: Exception) {
                    Log.e("Geocoding", "Geocoding failed due to exception: ${e.message}")
                }
            }
        }
        .addOnFailureListener { e ->
            Log.e("Geocoding", "Failed to fetch user document: ${e.message}")
        }
}