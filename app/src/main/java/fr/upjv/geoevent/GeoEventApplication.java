package fr.upjv.geoevent;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * Classe d'application principale.
 * Responsable de l'initialisation des configurations globales au démarrage de l'application,
 * notamment la création des canaux de notification nécessaires au fonctionnement du service push.
 */
public class GeoEventApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Préparation des canaux de notification pour Android 8.0+
        createNotificationChannels();
    }

    /**
     * Définit et enregistre les canaux de notification auprès du système Android.
     * Cette étape est indispensable pour que les notifications push GeoEvent puissent être affichées.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal principal pour les alertes et informations sur les événements
            NotificationChannel channel = new NotificationChannel(
                    "geoevent_channel",
                    "GeoEvent Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications push concernant les événements GeoEvent");
            channel.enableVibration(true);
            channel.enableLights(true);

            // Enregistrement du canal via le NotificationManager
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
