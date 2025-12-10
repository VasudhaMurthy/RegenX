const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// --- Configuration Constants (Matching GeofenceUtils.kt) ---
const GEOFENCE_RADIUS_M = 100.0;
const DAY_MILLIS = 24 * 60 * 60 * 1000;
const EARTH_RADIUS_M = 6371000.0;

// --- Utility Function: Haversine Distance (JavaScript implementation) ---
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

// --- Utility Function: Bounding Box Calculation ---
function boundingBox(lat, lng, radiusMeters) {
    const latDeg = radiusMeters / 111320.0;
    const lngDeg = radiusMeters / (111320.0 * Math.cos(lat * Math.PI / 180));

    const minLat = lat - latDeg;
    const maxLat = lat + latDeg;
    const minLng = lng - lngDeg;
    const maxLng = lng + lngDeg;

    return { minLat, maxLat, minLng, maxLng };
}

/**
 * The CORE FUNCTION: Triggers every time a document in the 'trucks' collection is updated.
 */
exports.checkTruckGeofence = functions.firestore
    .document('trucks/{truckId}')
    .onUpdate(async (change, context) => {

        const truckId = context.params.truckId;
        const newTruckData = change.after.data();

        // 1. Get new location and status
        const truckLat = newTruckData.latitude;
        const truckLng = newTruckData.longitude;
        const truckStatus = newTruckData.status;

        // Skip if truck is not "En Route"
        if (truckStatus !== 'En Route' || !truckLat || !truckLng) {
            console.log(`Truck ${truckId} is not En Route or missing location. Skipping check.`);
            return null;
        }

        const db = admin.firestore();
        const now = Date.now();

        // 2. Calculate Bounding Box and Query Residents
        const box = boundingBox(truckLat, truckLng, GEOFENCE_RADIUS_M);

        const residentsSnapshot = await db.collection('residents')
            .whereGreaterThanOrEqualTo('latitude', box.minLat)
            .whereLessThanOrEqualTo('latitude', box.maxLat)
            .get();

        const alertPromises = [];

        residentsSnapshot.forEach(doc => {
            // ... (rest of the logic for alert writing and FCM sending)
            const residentId = doc.id;
            const residentData = doc.data();
            const rLat = residentData.latitude;
            const rLng = residentData.longitude;
            const residentToken = residentData.fcmToken;

            if (!rLat || !rLng || !residentToken) return;

            // 3. Precise Geofence Check (Haversine)
            const distance = haversineDistanceMeters(truckLat, truckLng, rLat, rLng);

            if (distance <= GEOFENCE_RADIUS_M) {
                // ... (Logic for de-duplication and sending FCM)
                const alertsRef = db.collection('residents').doc(residentId).collection('alerts');

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
                               title: 'RegenX Alert: Truck Approaching! 🚚',
                               body: `The garbage truck is within 100m.`,
                           },
                           token: residentToken,
                           data: { truckId: truckId, type: 'GEOFENCE_ENTER' }
                       };
                       await admin.messaging().send(message);

                       // Write alert document after successful FCM send
                       await alertsRef.add({
                            truckId: truckId,
                            distanceMeters: distance,
                            message: "Garbage truck is nearby.",
                            timestamp: now,
                            seen: false
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