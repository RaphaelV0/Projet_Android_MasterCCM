package fr.upjv.geoevent.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

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

        mapService.showCurrentLocation();
        loadEvents();

        return view;
    }

    private void appliquerFiltrage() {
        if (mapService == null) return;

        mapService.clear();
        mapService.showCurrentLocation();

        Location userLoc = recupererDernierePositionConnue();
        int radius = viewModel.getRadius().getValue() != null ? viewModel.getRadius().getValue() : 10;

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
    }

    private void loadEvents() {
        new EvenementRepository().getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof QuerySnapshot) {
                    allEvents = ((QuerySnapshot) data).toObjects(Evenement.class);
                    viewModel.setEvents(allEvents);
                    appliquerFiltrage();
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

    @Override public void onResume() { super.onResume(); if (mapService != null) mapService.onResume(); }
    @Override public void onPause() { super.onPause(); if (mapService != null) mapService.onPause(); }
    @Override public void onDestroyView() { super.onDestroyView(); if (mapService != null) { mapService.release(); mapService = null; } }
}