//package com.example.regenx.network
//
//import android.util.Log
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import org.json.JSONObject
//
//object DirectionsApiService {
//
//    private val client = OkHttpClient()
//
//    /**
//     * Calls Google Directions API and returns the full JSON response
//     * or null if the HTTP request itself failed.
//     */
//    fun getRoute(
//        origin: String,        // "lat,lng"
//        destination: String,   // "lat,lng"
//        apiKey: String
//    ): JSONObject? {
//        val url =
//            "https://maps.googleapis.com/maps/api/directions/json" +
//                    "?origin=$origin" +
//                    "&destination=$destination" +
//                    "&mode=driving" +
//                    "&key=$apiKey"
//
//        Log.d("DirectionsApi", "Request URL: $url")
//
//        return try {
//            val request = Request.Builder()
//                .url(url)
//                .build()
//
//            client.newCall(request).execute().use { response ->
//                val body = response.body?.string()
//                Log.d("DirectionsApi", "HTTP ${response.code} body: $body")
//
//                if (!response.isSuccessful || body.isNullOrEmpty()) {
//                    Log.e("DirectionsApi", "Unsuccessful HTTP response: ${response.code}")
//                    return null
//                }
//
//                JSONObject(body)
//            }
//        } catch (e: Exception) {
//            Log.e("DirectionsApi", "Exception calling Directions API", e)
//            null
//        }
//    }
//}





package com.example.regenx.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object DirectionsApiService {

    private val client = OkHttpClient()

    /**
     * Calls Google Directions API and returns the JSON response.
     *
     * Returns null ONLY if:
     *  - the HTTP call throws an exception, or
     *  - the body is empty / not valid JSON.
     */
    suspend fun getRoute(
        origin: String,        // "lat,lng"
        destination: String,   // "lat,lng"
        apiKey: String
    ): JSONObject? = withContext(Dispatchers.IO) {

        // Build URL safely
        val url =
            "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=$origin" +
                    "&destination=$destination" +
                    "&mode=driving" +
                    "&key=$apiKey"

        Log.d("DirectionsApi", "FINAL URL = $url")

        try {
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()

                Log.d(
                    "DirectionsApi",
                    "HTTP code = ${response.code}, body = $body"
                )

                if (!response.isSuccessful) {
                    Log.e(
                        "DirectionsApi",
                        "Unsuccessful HTTP response: code=${response.code}"
                    )
                }

                if (body.isNullOrEmpty()) {
                    Log.e("DirectionsApi", "Empty body from Directions API")
                    return@withContext null
                }

                try {
                    JSONObject(body)
                } catch (e: Exception) {
                    Log.e(
                        "DirectionsApi",
                        "Failed to parse JSON from Directions API",
                        e
                    )
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DirectionsApi", "Exception calling Directions API", e)
            null
        }
    }
}



