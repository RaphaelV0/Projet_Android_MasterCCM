package fr.upjv.geoevent.services;

/**
 * Interface d'abstraction pour le service de notifications push.
 * Découple la couche UI de l'implémentation FCM.
 */
public interface INotificationService {

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