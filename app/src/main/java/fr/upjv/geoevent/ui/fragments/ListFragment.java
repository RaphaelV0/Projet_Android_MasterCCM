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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.ui.adapters.EventAdapter;
import fr.upjv.geoevent.ui.viewmodels.EventViewModel;

public class ListFragment extends Fragment {

    private EventAdapter adapter;
    private EventViewModel viewModel;
    private List<Evenement> allEvents = new ArrayList<>();
    private int currentRadius = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(adapter);

        // Initialisation ViewModel Partagé
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Observer les événements
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            this.allEvents = events;
            filtrerEtAfficher();
        });

        // Observer le rayon (mis à jour par le MapFragment)
        viewModel.getRadius().observe(getViewLifecycleOwner(), radius -> {
            this.currentRadius = radius;
            filtrerEtAfficher();
        });

        return view;
    }

    private void filtrerEtAfficher() {
        Location userLoc = recupererDernierePositionConnue();
        List<Evenement> filtered = new ArrayList<>();

        for (Evenement event : allEvents) {
            if (userLoc != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                        userLoc.getLatitude(), userLoc.getLongitude(),
                        event.getLatitude(), event.getLongitude(),
                        results
                );
                float dist = results[0] / 1000f;

                if (dist <= currentRadius) {
                    event.setDistance(dist);
                    filtered.add(event);
                }
            } else {
                event.setDistance(0);
                filtered.add(event);
            }
        }

        // 2. TRI DE LA LISTE PAR DISTANCE (Croissant)
        if (userLoc != null) {
            Collections.sort(filtered, (e1, e2) -> Float.compare(e1.getDistance(), e2.getDistance()));
        }

        if (adapter != null) {
            adapter.updateList(filtered);
        }
    }

    @Nullable
    private Location recupererDernierePositionConnue() {
        Context context = getContext();
        if (context == null || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return null;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (gps != null && net != null) return gps.getTime() > net.getTime() ? gps : net;
        return (gps != null) ? gps : net;
    }
}