package fr.upjv.geoevent.services.map;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Interface commune pour les services cartographiques.
 * Permet d'abstraire les fonctionnalités de la carte pour faciliter le changement de fournisseur.
 */
public interface MapService {

    /**
     * Initialise la carte dans le conteneur fourni.
     * @param context Contexte de l'application
     * @param container Conteneur où ajouter la vue de la carte
     */
    void initialize(Context context, ViewGroup container);

    /**
     * Centre la carte sur les coordonnées spécifiées avec un niveau de zoom.
     * @param latitude Latitude du centre
     * @param longitude Longitude du centre
     * @param zoom Niveau de zoom
     */
    void centerOn(double latitude, double longitude, double zoom);

    /**
     * Affiche la position actuelle de l'utilisateur sur la carte.
     */
    void showCurrentLocation();

    /**
     * Ajoute un marqueur sur la carte aux coordonnées spécifiées avec un titre.
     * @param latitude Latitude du marqueur
     * @param longitude Longitude du marqueur
     * @param title Titre du marqueur
     */
    void addMarker(double latitude, double longitude, String title);


    /**
     * Supprime tous les marqueurs et tracés de la carte.
     */
    void clear();

    /**
     * Libère les ressources utilisées par le service cartographique.
     */
    void release();

    /**
     * Méthode appelée lors de la reprise de l'activité/fragment.
     */
    void onResume();

    /**
     * Méthode appelée lors de la pause de l'activité/fragment.
     */
    void onPause();



    //Méthode pour afficher l'itineraire sur la carte

    // --- Tracé d'itinéraire via OSRM ---
    /**
     * Définit un callback déclenché au clic sur un marqueur.
     * @param listener reçoit le titre du marqueur cliqué
     */
    void setOnMarkerClickListener(OnMarkerClickListener listener);

    /**
     * Trace un itinéraire routier entre deux points via l'API OSRM (offline-friendly).
     */
    void drawRoute(double fromLat, double fromLng, double toLat, double toLng, RouteCallback callback);

    /**
     * Supprime le tracé d'itinéraire de la carte.
     */
    void clearRoute();

    interface OnMarkerClickListener {
        void onMarkerClick(String markerTitle, double lat, double lng);
    }

    interface RouteCallback {
        void onSuccess(double distanceKm, long durationMinutes);
        void onError(String message);
    }



}
