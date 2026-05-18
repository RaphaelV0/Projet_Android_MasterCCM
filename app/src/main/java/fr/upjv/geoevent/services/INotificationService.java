package fr.upjv.geoevent.services;

/**
 * Interface définissant les capacités du système de notifications push.
 * Découple l'interface d'administration (AdminNotificationsFragment) de la solution technique (FCM/HTTP).
 */
public interface INotificationService {

    /**
     * Interface de retour pour le suivi asynchrone de l'envoi d'une notification.
     */
    interface NotificationCallback {
        void onSuccess();
        void onFailure(String error);
    }

    void sendToAllUsers(String title, String body, NotificationCallback callback);

    void sendToEventSubscribers(String eventId, String title, String body, NotificationCallback callback);

    void subscribeCurrentUserToAllUsers();

    void subscribeCurrentUserToEvent(String eventId);

    void unsubscribeCurrentUserFromEvent(String eventId);
}