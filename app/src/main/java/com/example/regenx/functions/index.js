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

        const truckLat = newTruckData.latitude;
        const truckLng = newTruckData.longitude;
        const truckStatus = newTruckData.status;

        if (truckStatus !== 'En Route' || !truckLat || !truckLng) {
            return null;
        }

        const db = admin.firestore();
        const now = Date.now();

        const box = boundingBox(truckLat, truckLng, GEOFENCE_RADIUS_M);

        // ✅ FIXED QUERY (Admin SDK syntax)
        const residentsSnapshot = await db.collection('residents')
            .where('latitude', '>=', box.minLat)
            .where('latitude', '<=', box.maxLat)
            .get();

        const alertPromises = [];

        residentsSnapshot.forEach(doc => {
            const residentId = doc.id;
            const residentData = doc.data();
            const rLat = residentData.latitude;
            const rLng = residentData.longitude;

            if (!rLat || !rLng) return;

            const distance = haversineDistanceMeters(truckLat, truckLng, rLat, rLng);

            if (distance <= GEOFENCE_RADIUS_M) {
                const alertsRef = db
                    .collection('residents')
                    .doc(residentId)
                    .collection('alerts');

                const checkAndSendAlert = async () => {
                    const cutoff = now - DAY_MILLIS;

                    // ✅ FIXED QUERY (Admin SDK syntax)
                    const existingAlerts = await alertsRef
                        .where('truckId', '==', truckId)
                        .where('timestamp', '>', cutoff)
                        .limit(1)
                        .get();

                    if (existingAlerts.empty) {
                        await alertsRef.add({
                            truckId: truckId,
                            distanceMeters: distance,
                            message: "Garbage truck is nearby.",
                            timestamp: now,
                            seen: false
                        });
                        console.log(`Alert written for resident ${residentId}`);
                    }
                };

                alertPromises.push(checkAndSendAlert());
            }
        });

        await Promise.allSettled(alertPromises);
        return null;
    });

exports.sendNotificationOnAlertCreate = functions.firestore
    .document('residents/{uid}/alerts/{alertId}')
    .onCreate(async (snap, context) => {

        const uid = context.params.uid;
        const alertData = snap.data();

        const userDoc = await admin.firestore()
            .collection('residents')
            .doc(uid)
            .get();

        if (!userDoc.exists) return null;

        const fcmToken = userDoc.data().fcmToken;
        if (!fcmToken) return null;

        const message = {
            token: fcmToken,
            notification: {
                title: '🚛 Garbage Truck Nearby',
                body: 'The garbage truck is approaching your home. Please be ready.'
            },
            android: {
                priority: 'high',
                notification: { sound: 'default' }
            },
            data: {
                truckId: alertData.truckId,
                type: 'GEOFENCE_ENTER'
            }
        };

        return admin.messaging().send(message);
    });
