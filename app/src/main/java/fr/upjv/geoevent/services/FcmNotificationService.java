package fr.upjv.geoevent.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implémentation FCM de INotificationService.
 *
 * Pour la réception : FirebaseMessaging.subscribeToTopic() — fonctionne nativement.
 *
 * Pour l'envoi depuis l'admin :
 *   - On appelle l'API FCM HTTP Legacy (format JSON).
 *   - Le SERVER_KEY est à renseigner depuis :
 *     Firebase Console > Paramètres du projet > Cloud Messaging > Clé du serveur
 *
 * NOTE : L'API Legacy FCM est dépréciée depuis juin 2023. Pour un projet de production,
 * migrer vers FCM HTTP v1 avec Firebase Cloud Functions.
 * Pour ce projet éducatif, cette implémentation est fonctionnelle.
 */
public class FcmNotificationService implements INotificationService {

    private static final String TAG = "FcmNotificationService";

    // TODO : Remplacer par votre Server Key depuis Firebase Console
    // Firebase Console > Paramètres du projet > Cloud Messaging > Clé du serveur
    private static final String FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY_HERE";
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    private static final String TOPIC_ALL_USERS = "all_users";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void sendToAllUsers(String title, String body, NotificationCallback callback) {
        sendToTopic(TOPIC_ALL_USERS, title, body, callback);
    }

    @Override
    public void sendToEventSubscribers(String eventId, String title, String body, NotificationCallback callback) {
        String topic = "event_" + eventId;
        sendToTopic(topic, title, body, callback);
    }

    @Override
    public void subscribeCurrentUserToAllUsers() {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(TOPIC_ALL_USERS)
                .addOnSuccessListener(v -> Log.d(TAG, "Abonné au topic all_users"))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur abonnement all_users", e));
    }

    @Override
    public void subscribeCurrentUserToEvent(String eventId) {
        String topic = "event_" + eventId;
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topic)
                .addOnSuccessListener(v -> Log.d(TAG, "Abonné au topic " + topic))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur abonnement " + topic, e));
    }

    @Override
    public void unsubscribeCurrentUserFromEvent(String eventId) {
        String topic = "event_" + eventId;
        FirebaseMessaging.getInstance()
                .unsubscribeFromTopic(topic)
                .addOnSuccessListener(v -> Log.d(TAG, "Désabonné du topic " + topic))
                .addOnFailureListener(e -> Log.e(TAG, "Erreur désabonnement " + topic, e));
    }

    private void sendToTopic(String topic, String title, String body, NotificationCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(FCM_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + FCM_SERVER_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);
                notification.put("sound", "default");
                notification.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

                JSONObject payload = new JSONObject();
                payload.put("to", "/topics/" + topic);
                payload.put("notification", notification);
                payload.put("priority", "high");

                byte[] outputBytes = payload.toString().getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);
                os.close();

                int responseCode = conn.getResponseCode();
                conn.disconnect();

                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    Log.e(TAG, "FCM response code: " + responseCode);
                    mainHandler.post(() -> callback.onFailure("Erreur FCM : code " + responseCode
                            + ". Vérifiez la FCM_SERVER_KEY dans FcmNotificationService.java"));
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur envoi notification", e);
                mainHandler.post(() -> callback.onFailure("Erreur réseau : " + e.getMessage()));
            }
        });
    }
}