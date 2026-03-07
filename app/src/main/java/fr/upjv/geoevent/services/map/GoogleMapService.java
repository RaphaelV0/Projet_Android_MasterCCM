package fr.upjv.geoevent.services.map;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Implémentation du service cartographique utilisant Google Maps.
 * Structure prête pour une future intégration complète.
 * Nécessite l'ajout de la dépendance Google Maps SDK et la configuration de l'API key.
 */
public class GoogleMapService implements MapService {

    // TODO: Ajouter les imports nécessaires pour Google Maps
    // import com.google.android.gms.maps.GoogleMap;
    // import com.google.android.gms.maps.MapView;
    // import com.google.android.gms.maps.OnMapReadyCallback;
    // etc.

    // Champs pour Google Maps
    // private MapView mapView;
    // private GoogleMap googleMap;

    @Override
    public void initialize(Context context, ViewGroup container) {
        // TODO: Implémenter l'initialisation de Google Maps
        // Créer MapView, l'ajouter au container, configurer
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void centerOn(double latitude, double longitude, double zoom) {
        // TODO: Centrer la carte Google Maps
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void showCurrentLocation() {
        // TODO: Afficher la localisation actuelle sur Google Maps
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void addMarker(double latitude, double longitude, String title) {
        // TODO: Ajouter un marqueur sur Google Maps
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void release() {
        // TODO: Libérer les ressources Google Maps
        // mapView.onDestroy();
    }

    @Override
    public void onResume() {
        // TODO: Gérer onResume pour Google Maps
        // mapView.onResume();
    }

    @Override
    public void onPause() {
        // TODO: Gérer onPause pour Google Maps
        // mapView.onPause();
    }
}
