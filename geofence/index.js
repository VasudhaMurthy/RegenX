// --- FIX: Switch to V2 Imports ---
const admin = require("firebase-admin");
const {onDocumentUpdated} = require("firebase-functions/v2/firestore"); // V2 Firestore Trigger
const {setGlobalOptions} = require("firebase-functions/v2");
admin.initializeApp();

// Global options for V2 functions
setGlobalOptions({ region: "us-central1", maxInstances: 10 });

// --- Configuration Constants (Matching GeofenceUtils.kt) ---
const GEOFENCE_RADIUS_M = 100.0;
const DAY_MILLIS = 24 * 60 * 60 * 1000;
const EARTH_RADIUS_M = 6371000.0;

// --- Utility Functions (Keep these the same) ---
/**
 * Calculates the distance between two geographical points using the Haversine formula.
 * @param {number} lat1 Latitude of the first point.
 * @param {number} lon1 Longitude of the first point.
 * @param {number} lat2 Latitude of the second point.
 * @param {number} lon2 Longitude of the second point.
 * @return {number} Distance in meters.
 */
function haversineDistanceMeters(lat1, lon1, lat2, lon2) {
  const toRad = (angle) => (angle * Math.PI) / 180;
  const R = EARTH_RADIUS_M;

  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);

  const a = Math.pow(Math.sin(dLat / 2), 2) +
            Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
            Math.pow(Math.sin(dLon / 2), 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

/**
 * Approximates the bounding box for a given center and radius.
 * @param {number} lat Center latitude.
 * @param {number} lng Center longitude.
 * @param {number} radiusMeters Search radius in meters.
 * @return {object} Object containing minLat, maxLat, minLng, maxLng.
 */
function boundingBox(lat, lng, radiusMeters) {
  const latDeg = radiusMeters / 111320.0;
  const lngDeg = radiusMeters / (111320.0 * Math.cos(lat * Math.PI / 180));

  const minLat = lat - latDeg;
  const maxLat = lat + latDeg;
  const minLng = lng - lngDeg;
  const maxLng = lng + lngDeg;

  return {minLat, maxLat, minLng, maxLng};
}

/**
 * The CORE FUNCTION: Triggers every time a document in the 'trucks' collection is updated.
 */
// --- FIX: Use V2 Trigger Syntax ---
exports.checkTruckGeofence = onDocumentUpdated("trucks/{truckId}", async (event) => {

      // V2 triggers use event.data.after instead of change.after
      const newTruckData = event.data.after.data();

      // Context data is accessed via event.params
      const truckId = event.params.truckId;

      // 1. Get new location and status
      const truckLat = newTruckData.latitude;
      const truckLng = newTruckData.longitude;
      const truckStatus = newTruckData.status;

      // Skip if truck is not "En Route"
      if (truckStatus !== "En Route" || !truckLat || !truckLng) {
        console.log(`Truck ${truckId} is not En Route or missing location. Skipping check.`);
        return null;
      }

      const db = admin.firestore();
      const now = Date.now();

      // 2. Calculate Bounding Box and Query Residents
      const box = boundingBox(truckLat, truckLng, GEOFENCE_RADIUS_M);

      const residentsSnapshot = await db.collection("residents")
          .whereGreaterThanOrEqualTo("latitude", box.minLat)
          .whereLessThanOrEqualTo("latitude", box.maxLat)
          .get();

      const alertPromises = [];

      residentsSnapshot.forEach((doc) => {
        const residentId = doc.id;
        const residentData = doc.data();
        const rLat = residentData.latitude;
        const rLng = residentData.longitude;
        const residentToken = residentData.fcmToken;

        if (!rLat || !rLng || !residentToken) return;

        // 3. Precise Geofence Check (Haversine)
        const distance = haversineDistanceMeters(truckLat, truckLng, rLat, rLng);

        if (distance <= GEOFENCE_RADIUS_M) {
          const alertsRef = db.collection("residents").document(residentId).collection("alerts");

          const checkAndSendAlert = async () => {
            const cutoff = now - DAY_MILLIS;
            const existingAlerts = await alertsRef
                .whereEqualTo("truckId", truckId)
                .whereGreaterThan("timestamp", cutoff)
                .limit(1)
                .get();

            if (existingAlerts.empty) {
              const message = {
                notification: {
                  title: "RegenX Alert: Truck Approaching! 🚚",
                  body: "The garbage truck is within 100m.",
                },
                token: residentToken,
                data: {truckId: truckId, type: "GEOFENCE_ENTER"},
              };
              await admin.messaging().send(message);

              // Write alert document after successful FCM send
              await alertsRef.add({
                truckId: truckId,
                distanceMeters: distance,
                message: "Garbage truck is nearby.",
                timestamp: now,
                seen: false,
              });
              console.log(`Alert written and FCM sent for resident ${residentId}`);
            }
          };
          alertPromises.push(checkAndSendAlert());
        }
      });

      await Promise.allSettled(alertPromises);
      return null;
    });