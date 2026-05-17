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
 * Implémentation FCM V1 via appel HTTP direct aux Cloud Functions Firebase.
 * On évite le SDK Firebase callable (qui impose App Check) au profit d'un
 * simple POST JSON — plus simple, plus prévisible.
 *
 * Les URLs sont celles des fonctions déployées sur us-central1.
 * Remplacer "ccmandroidgp2026" si le project_id change.
 */
public class FcmNotificationService implements INotificationService {

    private static final String TAG     = "FcmNotificationService";
    private static final String API_KEY = "geoevent-admin-2026";

    private static final String BASE_URL =
            "https://us-central1-ccmandroidgp2026.cloudfunctions.net/";

    private static final String TOPIC_ALL = "all_users";

    private final ExecutorService executor   = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

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
     * Exécute un POST JSON dans un thread background et retourne le résultat
     * sur le thread principal via le callback.
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

    // ===== Abonnements locaux (côté client, pas besoin de Cloud Functions) =====

    @Override
    public void subscribeCurrentUserToAllUsers() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(TOPIC_ALL)
                .addOnSuccessListener(v -> Log.d(TAG, "Abonné à all_users"))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur abonnement all_users", e));
    }

    @Override
    public void subscribeCurrentUserToEvent(String eventId) {
        String topic = "event_" + eventId;
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic(topic)
                .addOnSuccessListener(v -> Log.d(TAG, "Abonné à " + topic))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur abonnement " + topic, e));
    }

    @Override
    public void unsubscribeCurrentUserFromEvent(String eventId) {
        String topic = "event_" + eventId;
        com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(topic)
                .addOnSuccessListener(v -> Log.d(TAG, "Désabonné de " + topic))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur désabonnement " + topic, e));
    }
}