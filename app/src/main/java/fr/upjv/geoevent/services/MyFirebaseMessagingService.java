package fr.upjv.geoevent.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataServiceFactory;
import fr.upjv.geoevent.domain.data.IDataService;
import fr.upjv.geoevent.ui.activities.MainActivity;

/**
 * Service Firebase Messaging.
 *
 * Deux responsabilités :
 *   1. onNewToken       → sauvegarder le token FCM dans Firestore (collection "users")
 *   2. onMessageReceived → afficher la notification dans le système Android
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FcmService";
    private static final String CHANNEL_ID = "geoevent_channel";
    private static final String CHANNEL_NAME = "GeoEvent Notifications";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nouveau token FCM : " + token);
        saveTokenToFirestore(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "GeoEvent";
        String body  = "";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle() != null
                    ? remoteMessage.getNotification().getTitle() : title;
            body  = remoteMessage.getNotification().getBody() != null
                    ? remoteMessage.getNotification().getBody() : body;
        } else if (!remoteMessage.getData().isEmpty()) {
            title = remoteMessage.getData().getOrDefault("title", title);
            body  = remoteMessage.getData().getOrDefault("body", body);
        }

        showNotification(title, body);
    }

    private void saveTokenToFirestore(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        IDataService dataService = DataServiceFactory.create();

        Map<String, Object> tokenUpdate = new HashMap<>();
        tokenUpdate.put("fcmToken", token);

        dataService.update("users", uid, tokenUpdate);
        Log.d(TAG, "Token FCM sauvegardé pour l'utilisateur " + uid);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications de l'application GeoEvent");
            channel.enableVibration(true);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_logo_geoevent)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(0xF5780A);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}