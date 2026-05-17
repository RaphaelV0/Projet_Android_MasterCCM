"use strict";

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const ALLOWED_KEY = "geoevent-admin-2026";

/**
 * Envoi d'une notification à un topic FCM.
 * Endpoint HTTP POST — pas de SDK callable, pas d'App Check.
 *
 * Body JSON attendu :
 * { "apiKey": "...", "topic": "all_users", "title": "...", "body": "..." }
 */
exports.sendToTopic = functions.https.onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");

  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    res.status(204).send("");
    return;
  }

  if (req.method !== "POST") {
    res.status(405).json({error: "Methode non autorisee."});
    return;
  }

  const {apiKey, topic, title, body} = req.body;

  if (apiKey !== ALLOWED_KEY) {
    res.status(403).json({error: "Cle API invalide."});
    return;
  }

  if (!topic || !title || !body) {
    res.status(400).json({error: "topic, title et body sont requis."});
    return;
  }

  try {
    const response = await admin.messaging().send({
      notification: {title, body},
      android: {
        priority: "high",
        notification: {sound: "default", channelId: "geoevent_channel"},
      },
      topic: topic,
    });
    res.status(200).json({success: true, messageId: response});
  } catch (error) {
    res.status(500).json({error: error.message});
  }
});

/**
 * Envoi d'une notification aux inscrits d'un événement.
 * Endpoint HTTP POST.
 *
 * Body JSON attendu :
 * { "apiKey": "...", "eventTitre": "...", "title": "...", "body": "..." }
 */
exports.sendToEventSubscribers = functions.https.onRequest(async (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");

  if (req.method === "OPTIONS") {
    res.set("Access-Control-Allow-Methods", "POST");
    res.set("Access-Control-Allow-Headers", "Content-Type");
    res.status(204).send("");
    return;
  }

  if (req.method !== "POST") {
    res.status(405).json({error: "Methode non autorisee."});
    return;
  }

  const {apiKey, eventTitre, title, body} = req.body;

  if (apiKey !== ALLOWED_KEY) {
    res.status(403).json({error: "Cle API invalide."});
    return;
  }

  if (!eventTitre || !title || !body) {
    res.status(400).json({error: "eventTitre, title et body sont requis."});
    return;
  }

  try {
    const inscriptions = await db
        .collection("inscriptionevent")
        .where("eventTitre", "==", eventTitre)
        .get();

    if (inscriptions.empty) {
      res.status(200).json({success: true, sent: 0, message: "Aucun inscrit."});
      return;
    }

    const uids = inscriptions.docs
        .map((doc) => doc.data().uid)
        .filter(Boolean);

    const tokens = [];
    for (const uid of uids) {
      const snap = await db.collection("users").doc(uid).get();
      if (snap.exists && snap.data().fcmToken) {
        tokens.push(snap.data().fcmToken);
      }
    }

    if (tokens.length === 0) {
      res.status(200).json({
        success: true,
        sent: 0,
        message: "Aucun token FCM disponible.",
      });
      return;
    }

    const result = await admin.messaging().sendEachForMulticast({
      notification: {title, body},
      android: {
        priority: "high",
        notification: {sound: "default", channelId: "geoevent_channel"},
      },
      tokens: tokens,
    });

    res.status(200).json({
      success: true,
      sent: result.successCount,
      failed: result.failureCount,
      total: tokens.length,
    });
  } catch (error) {
    res.status(500).json({error: error.message});
  }
});
