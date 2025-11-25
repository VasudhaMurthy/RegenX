/**
 * File: [Project Root]/functions/index.js
 * Purpose: Callable function to securely send FCM notifications to nearby residents.
 * It is triggered by the CollectorLocationService running on the Android device.
 * * To deploy: run 'firebase deploy --only functions' from the project root.
 */
const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Initialize Firebase Admin SDK (required to send FCM notifications securely)
admin.initializeApp();

/**
 * Callable function to send an arrival notification to a specific resident token.
 * * @param {object} data - Contains the necessary data passed from the Android app.
 * @param {string} data.token - The FCM registration token of the resident's device.
 * @param {object} context - The context object provided by Firebase Functions.
 * @returns {object} A confirmation object.
 */
exports.notifyResident = functions.https.onCall(async (data, context) => {
    // --- 1. Validation ---
    const token = data.token;

    // Ensure the user is authenticated if you want to restrict who can trigger this function.
    // However, since the collector's app is already authenticated, we only validate the token.
    if (!token) {
        throw new functions.https.HttpsError('invalid-argument', 'FCM token is required to send the notification.');
    }

    // --- 2. Define Payload ---
    const payload = {
        // 'notification' key handles the notification displayed in the system tray
        notification: {
            title: "Garbage Truck Nearby 🚛",
            body: "Your collection truck is within 200m and about to arrive! Please keep your waste ready.",
            sound: "default"
        },
        // 'data' key allows for custom handling by the app (e.g., refreshing a map)
        data: {
            notificationType: "collector_arrival",
            // Use the collector's UID from the context (if authenticated)
            collectorId: context.auth && context.auth.uid ? context.auth.uid : "unknown_collector"
        }
    };

    // --- 3. Send Notification ---
    try {
        const response = await admin.messaging().sendToDevice(token, payload);

        // Log the response for auditing/debugging
        console.log("Successfully sent message:", response);

        return {
            success: true,
            message: "Notification sent successfully.",
            messageId: response.results[0].messageId
        };
    } catch (error) {
        console.error("Error sending message:", error);
        throw new functions.https.HttpsError('internal', 'FCM send failed.', error.toString());
    }
});