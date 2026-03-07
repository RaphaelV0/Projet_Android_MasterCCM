package fr.upjv.geoevent.services.map;

import android.content.Context;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.api.IMapController;

/**
 * Implémentation du service cartographique utilisant OpenStreetMap via OSMDroid.
 * Gère l'initialisation, le centrage, la localisation et les marqueurs.
 */
public class OpenStreetMapService implements MapService {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;

    @Override
    public void initialize(Context context, ViewGroup container) {
        // Configuration OSMDroid
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        // Créer la MapView
        mapView = new MapView(context);
        mapView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mapView.setMultiTouchControls(true);

        // Ajouter au conteneur
        container.addView(mapView);

        // Centrer par défaut sur Saint-Quentin
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(49.8489, 3.2870);
        mapController.setCenter(startPoint);
    }

    @Override
    public void centerOn(double latitude, double longitude, double zoom) {
        if (mapView != null) {
            IMapController controller = mapView.getController();
            controller.setZoom(zoom);
            controller.setCenter(new GeoPoint(latitude, longitude));
        }
    }

    @Override
    public void showCurrentLocation() {
        if (mapView != null && locationOverlay == null) {
            locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(mapView.getContext()), mapView);
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            mapView.getOverlays().add(locationOverlay);
        }
    }

    @Override
    public void addMarker(double latitude, double longitude, String title) {
        if (mapView != null) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(latitude, longitude));
            marker.setTitle(title);
            mapView.getOverlays().add(marker);
        }
    }

    @Override
    public void release() {
        if (mapView != null) {
            mapView.onDetach();
            mapView = null;
        }
        locationOverlay = null;
    }

    @Override
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
    }
}
