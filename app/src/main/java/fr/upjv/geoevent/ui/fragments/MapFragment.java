package fr.upjv.geoevent.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;
import fr.upjv.geoevent.services.map.MapProviderType;
import fr.upjv.geoevent.services.map.MapService;
import fr.upjv.geoevent.services.map.MapServiceFactory;
import fr.upjv.geoevent.ui.activities.NewEventActivity;
import fr.upjv.geoevent.ui.viewmodels.EventViewModel;

public class MapFragment extends Fragment {

    private MapService mapService;
    private EventViewModel viewModel;
    private List<Evenement> allEvents = new ArrayList<>();

    // Références UI itinéraire
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheet;

    private TextView tvItineraryTitle, tvItineraryDistance;
    private Button btnShowRoute, btnClearRoute;

    // Événement actuellement sélectionné
    private double selectedLat, selectedLng;
    private String selectedTitle;
    private ActivityResultLauncher<String> locationPermissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                        isGranted -> {

                            if (isGranted) {
                                mapService.showCurrentLocation();
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        "Permission localisation refusée",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );

        // 1. Initialisation du ViewModel Partagé
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // 2. Configuration du Service de Carte
        FrameLayout mapContainer = view.findViewById(R.id.map_container);
        mapService = MapServiceFactory.create(MapProviderType.OPEN_STREET_MAP, requireContext());
        mapService.initialize(requireContext(), mapContainer);


        // 3. Configuration du Bouton d'ajout (FAB)
        FloatingActionButton fabAddEvent = view.findViewById(R.id.fab_add_event);
        if (fabAddEvent != null) {
            fabAddEvent.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), NewEventActivity.class);
                startActivity(intent);
            });
        }

        // 4. Configuration de la SeekBar et du Texte
        SeekBar seekBar = view.findViewById(R.id.seekbar_distance);
        TextView tvDistance = view.findViewById(R.id.tv_distance_filter);

        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int radius = progress > 0 ? progress : 1;
                    if (tvDistance != null) tvDistance.setText("Rayon de recherche : " + radius + " km");
                    viewModel.setRadius(radius); // Met à jour le ViewModel pour la liste
                    appliquerFiltrage(); // Met à jour la carte
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }


        // --- UI Itinéraire ---
        //cardItinerary      = view.findViewById(R.id.card_itinerary);
        bottomSheet = view.findViewById(R.id.bottom_sheet_itinerary);

        bottomSheetBehavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(true);

        // Hauteur = 30% de l'écran
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        bottomSheetBehavior.setPeekHeight((int)(screenHeight * 0.40));

        tvItineraryTitle   = view.findViewById(R.id.tv_itinerary_title);
        tvItineraryDistance = view.findViewById(R.id.tv_itinerary_distance);
        btnShowRoute       = view.findViewById(R.id.btn_show_route);
        btnClearRoute      = view.findViewById(R.id.btn_clear_route);

        btnShowRoute.setOnClickListener(v -> lancerItineraire());
        btnClearRoute.setOnClickListener(v -> effacerItineraire());

        // --- Clic sur marqueur ---
        mapService.setOnMarkerClickListener((title, lat, lng) -> {
            selectedTitle = title;
            selectedLat   = lat;
            selectedLng   = lng;
            afficherPanneauItineraire(title, lat, lng);
        });



        //mapService.showCurrentLocation();
        verifierPermissionLocalisation();
        loadEvents();

        return view;
    }

    // Affiche le panneau bas avec titre + distance à vol d'oiseau
    private void afficherPanneauItineraire(String title, double lat, double lng) {
        tvItineraryTitle.setText("📍 " + title);

        Location userLoc = recupererDernierePositionConnue();
        if (userLoc != null) {
            float[] res = new float[1];
            Location.distanceBetween(
                    userLoc.getLatitude(), userLoc.getLongitude(), lat, lng, res);
            tvItineraryDistance.setText(
                    String.format("Distance à vol d'oiseau : %.1f km", res[0] / 1000.0));
        } else {
            tvItineraryDistance.setText("Position actuelle inconnue");
        }

        btnClearRoute.setVisibility(View.GONE);
        btnShowRoute.setVisibility(View.VISIBLE);

        bottomSheet.bringToFront();
        bottomSheet.setElevation(50f);

        // ✅ Ouvrir le bottom sheet
        bottomSheetBehavior.setState(
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void lancerItineraire() {
        Location userLoc = recupererDernierePositionConnue();
        if (userLoc == null) {
            Toast.makeText(requireContext(),
                    "Position actuelle introuvable", Toast.LENGTH_SHORT).show();
            return;
        }

        btnShowRoute.setEnabled(false);
        btnShowRoute.setText("Calcul…");

        mapService.drawRoute(
                userLoc.getLatitude(), userLoc.getLongitude(),
                selectedLat, selectedLng,
                new MapService.RouteCallback() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onSuccess(double distanceKm, long durationMinutes) {
                        tvItineraryDistance.setText(String.format("🚗 %.1f km  •  ~%d min", distanceKm, durationMinutes));
                        btnShowRoute.setEnabled(true);
                        btnShowRoute.setText("🗺️ Itinéraire");
                        btnClearRoute.setVisibility(View.VISIBLE);
                    }
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onError(String message) {
                        Toast.makeText(requireContext(),
                                "Erreur itinéraire : " + message, Toast.LENGTH_SHORT).show();
                        Log.d("Erreur itinéraire :", message);

                        btnShowRoute.setEnabled(true);
                        btnShowRoute.setText("🗺️ Itinéraire");
                    }
                }
        );
    }


    private void effacerItineraire() {
        mapService.clearRoute();
        btnClearRoute.setVisibility(View.GONE);
        // ✅ Fermer le bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    private void appliquerFiltrage() {
        if (mapService == null) return;
        Log.d("MapFragment", "appliquerFiltrage: " + allEvents.size() + " événements");

        mapService.clear();
        mapService.showCurrentLocation();

        Location userLoc = recupererDernierePositionConnue();
        int radius = viewModel.getRadius().getValue() != null ? viewModel.getRadius().getValue() : 10;
        int count = 0;
        for (Evenement event : allEvents) {
            if (userLoc != null) {
                float[] results = new float[1];
                Location.distanceBetween(userLoc.getLatitude(), userLoc.getLongitude(),
                        event.getLatitude(), event.getLongitude(), results);

                if (results[0] / 1000f <= radius) {
                    mapService.addMarker(event.getLatitude(), event.getLongitude(), event.getTitre());
                }
            } else {
                mapService.addMarker(event.getLatitude(), event.getLongitude(), event.getTitre());
            }
        }

        android.util.Log.d("MapFragment", "Marqueurs affichés : " + count + "/" + allEvents.size());

    }

    private void loadEvents() {
        new EvenementRepository().getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof QuerySnapshot) {
                    allEvents = ((QuerySnapshot) data).toObjects(Evenement.class);
                    viewModel.setEvents(allEvents);
                    //appliquerFiltrage();
                    //  Garantir l'exécution sur le thread UI
                    if (getActivity() != null) {
                        requireActivity().runOnUiThread(() -> appliquerFiltrage());
                    }
                }
            }
            @Override public void onError(Exception e) {}
        });
    }

    @Nullable
    private Location recupererDernierePositionConnue() {
        Context context = requireContext();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return null;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (gps != null && net != null) return gps.getTime() > net.getTime() ? gps : net;
        return (gps != null) ? gps : net;
    }


    private void verifierPermissionLocalisation() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mapService.showCurrentLocation();

        } else {

            locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
            );
        }
    }

    @Override public void onResume() { super.onResume(); if (mapService != null) mapService.onResume(); }
    @Override public void onPause() { super.onPause(); if (mapService != null) mapService.onPause(); }
    @Override public void onDestroyView() { super.onDestroyView(); if (mapService != null) { mapService.release(); mapService = null; } }

}