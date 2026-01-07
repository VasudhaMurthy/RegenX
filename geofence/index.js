// --- V2 Imports ---
const admin = require("firebase-admin");
const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { setGlobalOptions } = require("firebase-functions/v2");

admin.initializeApp();

// Global options
setGlobalOptions({ region: "us-central1", maxInstances: 10 });

// --- Configuration Constants ---
const GEOFENCE_RADIUS_M = 100.0;
const DAY_MILLIS = 24 * 60 * 60 * 1000;
const EARTH_RADIUS_M = 6371000.0;

// --- Utility Functions ---
function haversineDistanceMeters(lat1, lon1, lat2, lon2) {
  const toRad = (angle) => (angle * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) *
      Math.cos(toRad(lat2)) *
      Math.sin(dLon / 2) ** 2;

  return 2 * EARTH_RADIUS_M * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function boundingBox(lat, lng, radiusMeters) {
  const latDeg = radiusMeters / 111320;
  const lngDeg = radiusMeters / (111320 * Math.cos(lat * Math.PI / 180));
  return {
    minLat: lat - latDeg,
    maxLat: lat + latDeg,
    minLng: lng - lngDeg,
    maxLng: lng + lngDeg,
  };
}

// --- CORE FUNCTION ---
exports.checkTruckGeofence = onDocumentUpdated(
  "trucks/{truckId}",
  async (event) => {
    if (!event.data?.after) return null;

    const newTruckData = event.data.after.data();
    const truckId = event.params.truckId;

    const { latitude: truckLat, longitude: truckLng, status } = newTruckData;

    console.log("CHECKING GEOFENCE", {
      truckId,
      truckLat,
      truckLng,
      status,
    });

    if (
      status !== "En Route" ||
      typeof truckLat !== "number" ||
      typeof truckLng !== "number"
    ) {
      return null;
    }

    const db = admin.firestore();
    const now = Date.now();
    const box = boundingBox(truckLat, truckLng, GEOFENCE_RADIUS_M);

    const residentsSnapshot = await db
      .collection("residents")
      .where("latitude", ">=", box.minLat)
      .where("latitude", "<=", box.maxLat)
      .get();

    const alertPromises = [];

    residentsSnapshot.forEach((docSnap) => {
      const residentId = docSnap.id;
      const { latitude, longitude, fcmToken } = docSnap.data();

      if (
        typeof latitude !== "number" ||
        typeof longitude !== "number" ||
        !fcmToken
      )
        return;

      const distance = haversineDistanceMeters(
        truckLat,
        truckLng,
        latitude,
        longitude
      );

      if (distance > GEOFENCE_RADIUS_M) return;

      const alertsRef = db
        .collection("residents")
        .doc(residentId)
        .collection("alerts");

      alertPromises.push(
        (async () => {
          const cutoff = now - DAY_MILLIS;

          const existing = await alertsRef
            .where("truckId", "==", truckId)
            .where("timestamp", ">", cutoff)
            .limit(1)
            .get();

          if (!existing.empty) return;

          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: "🚛 Garbage Truck Nearby",
              body: "The garbage truck is within 100 meters.",
            },
            data: { truckId, type: "GEOFENCE_ENTER" },
          });

          await alertsRef.add({
            truckId,
            distanceMeters: distance,
            message: "Garbage truck is nearby.",
            timestamp: now,
            seen: false,
          });

          console.log(`Alert sent to resident ${residentId}`);
        })()
      );
    });

    await Promise.allSettled(alertPromises);
    return null;
  }
);
