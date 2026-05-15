package fr.upjv.geoevent.services.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.api.IMapController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.upjv.geoevent.R;

/**
 * Implémentation du service cartographique utilisant OpenStreetMap via OSMDroid.
 */
public class OpenStreetMapService implements MapService {

    private MapView mapView;
    private MyLocationNewOverlay locationOverlay;
    private Polyline currentRoute;

    private Marker selectedMarker = null;





    private OnMarkerClickListener markerClickListener;

    @Override
    public void initialize(Context context, ViewGroup container) {
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        mapView = new MapView(context);
        mapView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mapView.setMultiTouchControls(true);

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
            mapView.getOverlays().add(locationOverlay);
        }
    }

    @Override
    public void addMarker(double lat, double lng, String title) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(lat, lng));
            marker.setTitle(title);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(createMarkerIcon(mapView.getContext(),
                    R.drawable.baseline_location_on_32,
                    0xFF1A4A8A)); // couleur en ARGB

            marker.setOnMarkerClickListener((m, map) -> {
                // ✅ Remettre l'ancienne couleur du marqueur précédent
                if (selectedMarker != null && selectedMarker != m) {
                    selectedMarker.setIcon(createMarkerIcon(mapView.getContext(),
                            R.drawable.baseline_location_on_32, 0xFF1A4A8A)); // couleur normale
                }

                // ✅ Colorier le marqueur cliqué en rouge
                m.setIcon(createMarkerIcon(mapView.getContext(),
                        R.drawable.baseline_location_on_32, 0xFFE53935)); // rouge

                selectedMarker = m;
                mapView.invalidate();

                if (markerClickListener != null) {
                    markerClickListener.onMarkerClick(m.getTitle(), lat, lng);
                }
                return true;
            });
            mapView.getOverlays().add(marker);
            mapView.invalidate(); //  sur le bon thread
        });
    }



    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }

    @Override
    public void clear() {
        if (mapView != null) {
            mapView.getOverlays().clear();
            locationOverlay = null;
            selectedMarker = null; // ✅
            mapView.invalidate();
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

    // --- Tracé d'itinéraire via OSRM ---
    @Override
    public void drawRoute(double fromLat, double fromLng, double toLat, double toLng, RouteCallback callback) {
        clearRoute();

        // OSRM public API — gratuite, pas de clé requise
        String url = String.format(java.util.Locale.US,
                "https://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=full&geometries=geojson",
                fromLng, fromLat, toLng, toLat
        );

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)");
                conn.setRequestProperty("Referer", "https://map.project-osrm.org/");

                int responseCode = conn.getResponseCode();
                Log.d("OSRM", "HTTP " + responseCode + " — " + url);

                //  Lire le bon stream selon le code réponse
                java.io.InputStream stream = (responseCode >= 200 && responseCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                Log.d("OSRM", "Réponse : " + sb.toString().substring(0, Math.min(200, sb.length())));

                JSONObject json = new JSONObject(sb.toString());

                // Vérifier que le code est "Ok" avant de parser
                if (!"Ok".equals(json.optString("code"))) {
                    throw new Exception("OSRM code: " + json.optString("code"));
                }

                JSONObject route = json.getJSONArray("routes").getJSONObject(0);
                double distanceKm  = route.getDouble("distance") / 1000.0;
                long   durationMin = (long)(route.getDouble("duration") / 60.0);

                JSONArray coords = route
                        .getJSONObject("geometry")
                        .getJSONArray("coordinates");

                List<GeoPoint> points = new ArrayList<>();
                for (int i = 0; i < coords.length(); i++) {
                    JSONArray c = coords.getJSONArray(i);
                    //  OSRM renvoie [lng, lat] — ne pas inverser !
                    points.add(new GeoPoint(c.getDouble(1), c.getDouble(0)));
                }

                Log.d("OSRM", "Points tracé : " + points.size());

                new Handler(android.os.Looper.getMainLooper()).post(() -> {
                    currentRoute = new Polyline(mapView);
                    currentRoute.setPoints(points);
                    currentRoute.getOutlinePaint().setColor(0xFF1565C0);
                    currentRoute.getOutlinePaint().setStrokeWidth(10f);
                    mapView.getOverlays().add(currentRoute);
                    mapView.invalidate();
                    if (callback != null) callback.onSuccess(distanceKm, durationMin);
                });

            } catch (Exception e) {
                Log.e("OSRM", "Erreur parsing", e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            }
        }).start();
    }


    @Override
    public void clearRoute() {
        if (currentRoute != null) {
            mapView.getOverlays().remove(currentRoute);
            currentRoute = null;
            mapView.invalidate();
        }
    }


    private Drawable createMarkerIcon(Context context, int iconRes, int tintColor) {
        Drawable icon = ContextCompat.getDrawable(context, iconRes).mutate();
        icon.setColorFilter(new android.graphics.PorterDuffColorFilter(
                tintColor, android.graphics.PorterDuff.Mode.SRC_IN));
        return icon;
    }

}