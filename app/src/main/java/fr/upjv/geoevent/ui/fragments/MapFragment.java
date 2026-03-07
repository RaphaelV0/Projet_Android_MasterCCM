package fr.upjv.geoevent.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.services.map.MapProviderType;
import fr.upjv.geoevent.services.map.MapService;
import fr.upjv.geoevent.services.map.MapServiceFactory;

/**
 * Fragment affichant la carte en utilisant un service cartographique abstrait.
 * Permet de changer facilement de fournisseur cartographique (OpenStreetMap ou Google Maps).
 */
public class MapFragment extends Fragment {

    private MapService mapService;

    // Fournisseur actif pour l’instant
    private static final MapProviderType MAP_PROVIDER = MapProviderType.OPEN_STREET_MAP;

    // Position par défaut : Saint-Quentin
    private static final double DEFAULT_LAT = 49.8489;
    private static final double DEFAULT_LNG = 3.2870;
    private static final double DEFAULT_ZOOM = 15.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        FrameLayout mapContainer = view.findViewById(R.id.map_container);

        mapService = MapServiceFactory.create(MAP_PROVIDER, requireContext());
        mapService.initialize(requireContext(), mapContainer);

        // Affichage de la position utilisateur sur la carte
        mapService.showCurrentLocation();

        // Centre la carte sur l’utilisateur si possible,
        // sinon sur une position par défaut
        afficherPositionEtEvenementsProches();

        return view;
    }

    /**
     * Centre la carte sur la position actuelle de l'utilisateur
     * et ajoute quelques événements simulés autour de lui.
     */
    private void afficherPositionEtEvenementsProches() {
        Location userLocation = recupererDernierePositionConnue();

        if (userLocation != null) {
            double userLat = userLocation.getLatitude();
            double userLng = userLocation.getLongitude();

            mapService.centerOn(userLat, userLng, 16.0);

            List<EventItem> events = genererEvenementsProches(userLat, userLng);
            for (EventItem event : events) {
                mapService.addMarker(event.latitude, event.longitude, event.title);
            }
        } else {
            // Fallback si aucune position n’est disponible
            mapService.centerOn(DEFAULT_LAT, DEFAULT_LNG, DEFAULT_ZOOM);

            List<EventItem> events = genererEvenementsProches(DEFAULT_LAT, DEFAULT_LNG);
            for (EventItem event : events) {
                mapService.addMarker(event.latitude, event.longitude, event.title);
            }
        }
    }

    /**
     * Récupère la dernière position connue via GPS ou réseau.
     */
    @Nullable
    private Location recupererDernierePositionConnue() {
        Context context = requireContext();

        boolean fineGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            return null;
        }

        LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) {
            return null;
        }

        Location gpsLocation = null;
        Location networkLocation = null;

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            return null;
        }

        if (gpsLocation != null && networkLocation != null) {
            return gpsLocation.getTime() > networkLocation.getTime()
                    ? gpsLocation
                    : networkLocation;
        }

        if (gpsLocation != null) {
            return gpsLocation;
        }

        return networkLocation;
    }

    /**
     * Génère des événements fictifs proches de l’utilisateur.
     * Plus tard, cette méthode pourra être remplacée par des données venant
     * de Firebase, Supabase ou d’une API.
     */
    @NonNull
    private List<EventItem> genererEvenementsProches(double userLat, double userLng) {
        List<EventItem> events = new ArrayList<>();

        events.add(new EventItem(userLat + 0.0020, userLng + 0.0010, "Concert en plein air"));
        events.add(new EventItem(userLat - 0.0015, userLng + 0.0020, "Match de football"));
        events.add(new EventItem(userLat + 0.0010, userLng - 0.0015, "Festival culturel"));
        events.add(new EventItem(userLat - 0.0020, userLng - 0.0010, "Exposition locale"));

        return events;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapService != null) {
            mapService.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapService != null) {
            mapService.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapService != null) {
            mapService.release();
            mapService = null;
        }
    }

    /**
     * Petit modèle local pour représenter un événement à afficher sur la carte.
     */
    private static class EventItem {
        final double latitude;
        final double longitude;
        final String title;

        EventItem(double latitude, double longitude, String title) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.title = title;
        }
    }
}