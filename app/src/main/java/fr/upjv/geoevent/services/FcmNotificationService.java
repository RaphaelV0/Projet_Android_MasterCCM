package fr.upjv.geoevent.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implémentation du service de notification utilisant Firebase Cloud Messaging (FCM V1).
 * Cette classe gère l'envoi de messages via des appels HTTP vers des Cloud Functions Firebase
 * et assure la gestion des abonnements aux thématiques (topics) côté client.
 * 
 * L'architecture privilégie des appels REST directs pour s'affranchir des contraintes
 * spécifiques au SDK Firebase Callable.
 */
public class FcmNotificationService implements INotificationService {

    private static final String TAG     = "FcmNotificationService";
    private static final String API_KEY = "geoevent-admin-2026";

    /** URL de base des Firebase Cloud Functions déployées */
    private static final String BASE_URL =
            "https://us-central1-ccmandroidgp2026.cloudfunctions.net/";

    /** Identifiant du canal de diffusion global */
    private static final String TOPIC_ALL = "all_users";

    /** Exécuteur pour les tâches réseau en arrière-plan */
    private final ExecutorService executor   = Executors.newSingleThreadExecutor();
    
    /** Handler pour retourner les résultats sur le thread UI principal */
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Envoie une notification push à l'ensemble des utilisateurs abonnés au canal global.
     * 
     * @param title Le titre de la notification.
     * @param body Le corps du message.
     * @param callback Interface de retour pour le suivi de l'opération.
     */
    @Override
    public void sendToAllUsers(String title, String body, NotificationCallback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("apiKey", API_KEY);
            payload.put("topic",  TOPIC_ALL);
            payload.put("title",  title);
            payload.put("body",   body);
        } catch (Exception e) {
            callback.onFailure("Erreur construction payload : " + e.getMessage());
            return;
        }
        postJson(BASE_URL + "sendToTopic", payload, callback);
    }

    /**
     * Envoie une notification push ciblée aux participants d'un événement spécifique.
     * 
     * @param eventTitre Le titre de l'événement servant de clé de routage.
     * @param title Le titre de la notification.
     * @param body Le corps du message.
     * @param callback Interface de retour pour le suivi de l'opération.
     */
    @Override
    public void sendToEventSubscribers(String eventTitre, String title, String body,
                                       NotificationCallback callback) {
        JSONObject payload = new JSONObject();
        try {
            payload.put("apiKey",     API_KEY);
            payload.put("eventTitre", eventTitre);
            payload.put("title",      title);
            payload.put("body",       body);
        } catch (Exception e) {
            callback.onFailure("Erreur construction payload : " + e.getMessage());
            return;
        }
        postJson(BASE_URL + "sendToEventSubscribers", payload, callback);
    }

    /**
     * Réalise une requête HTTP POST asynchrone pour transmettre le payload JSON aux Cloud Functions.
     * 
     * @param endpoint L'URL complète de la fonction cible.
     * @param payload L'objet JSON contenant les données de la notification.
     * @param callback Interface de retour pour notifier la réussite ou l'échec sur le thread UI.
     */
    private void postJson(String endpoint, JSONObject payload,
                          NotificationCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                byte[] bytes = payload.toString().getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                conn.disconnect();
                Log.d(TAG, endpoint + " → HTTP " + code);

                if (code == 200) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    mainHandler.post(() ->
                            callback.onFailure("Erreur serveur HTTP " + code
                                    + ". Vérifiez les Cloud Functions Firebase."));
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur HTTP", e);
                mainHandler.post(() -> callback.onFailure("Erreur réseau : " + e.getMessage()));
            }
        });
    }

    /**
     * Enregistre l'appareil courant auprès du topic global pour recevoir les annonces générales.
     */
    @Override
    public void subscribeCurrentUserToAllUsers() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(TOPIC_ALL)
                .addOnSuccessListener(v -> Log.d(TAG, "Abonné à all_users"))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur abonnement all_users", e));
    }

    /**
     * Abonne l'utilisateur aux notifications spécifiques liées à un événement.
     * 
     * @param eventId L'identifiant (ou titre) de l'événement cible.
     */
    @Override
    public void subscribeCurrentUserToEvent(String eventId) {
        String topic = "event_" + eventId;
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(topic)
                .addOnSuccessListener(v -> Log.d(TAG, "Abonné à " + topic))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur abonnement " + topic, e));
    }

    /**
     * Supprime l'abonnement aux notifications liées à un événement spécifique.
     * 
     * @param eventId L'identifiant (ou titre) de l'événement concerné.
     */
    @Override
    public void unsubscribeCurrentUserFromEvent(String eventId) {
        String topic = "event_" + eventId;
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(topic)
                .addOnSuccessListener(v -> Log.d(TAG, "Désabonné de " + topic))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur désabonnement " + topic, e));
    }
}
