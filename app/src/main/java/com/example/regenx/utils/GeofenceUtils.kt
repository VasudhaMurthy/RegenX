// GeofenceUtils.kt
package com.example.regenx.util

import kotlin.math.*

object GeofenceUtils {
    // meters
    const val GEOFENCE_RADIUS_M = 100.0

    // Haversine distance in meters
    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    // Returns bounding box (minLat, maxLat, minLng, maxLng) for radiusMeters around lat/lng
    fun boundingBox(lat: Double, lng: Double, radiusMeters: Double): Quadruple {
        // 1 deg latitude ~ 111_320 meters (approx)
        val latDeg = radiusMeters / 111320.0
        // longitude degrees vary by latitude
        val lngDeg = radiusMeters / (111320.0 * cos(Math.toRadians(lat)).coerceAtLeast(0.000001))
        val minLat = lat - latDeg
        val maxLat = lat + latDeg
        val minLng = lng - lngDeg
        val maxLng = lng + lngDeg
        return Quadruple(minLat, maxLat, minLng, maxLng)
    }

    data class Quadruple(val minLat: Double, val maxLat: Double, val minLng: Double, val maxLng: Double)
}
