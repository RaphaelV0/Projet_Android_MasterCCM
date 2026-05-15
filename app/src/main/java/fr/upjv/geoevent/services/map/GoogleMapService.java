package fr.upjv.geoevent.services.map;

import android.content.Context;
import android.view.ViewGroup;

/**
 * Implémentation du service cartographique utilisant Google Maps.
 * Structure prête pour une future intégration complète.
 * Nécessite l'ajout de la dépendance Google Maps SDK et la configuration de l'API key.
 */
public class GoogleMapService implements MapService {

    @Override
    public void initialize(Context context, ViewGroup container) {
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void centerOn(double latitude, double longitude, double zoom) {
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void showCurrentLocation() {
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void addMarker(double latitude, double longitude, String title) {
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void clear() {
        // TODO: Implémenter la suppression des marqueurs Google Maps
        throw new UnsupportedOperationException("Google Maps non encore implémenté");
    }

    @Override
    public void release() {
        // TODO: Libérer les ressources
    }

    @Override
    public void onResume() {
        // TODO: Gérer onResume
    }

    @Override
    public void onPause() {
        // TODO: Gérer onPause
    }

    @Override
    public void setOnMarkerClickListener(OnMarkerClickListener listener) {

    }

    @Override
    public void drawRoute(double fromLat, double fromLng, double toLat, double toLng, RouteCallback callback) {

    }

    @Override
    public void clearRoute() {

    }
}