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

/**
 * Fragment matérialisant l'affichage sous forme de liste.
 * Intercepte les données du ViewModel partagé pour appliquer un tri de proximité croissante et un filtrage kilométrique.
 */
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

        // Connexion au ViewModel partagé indexé sur le cycle de vie global de l'Activité (requireActivity)
        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Observation réactive de la collection d'événements calée sur le cycle de vie de la vue (ViewLifecycleOwner)
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            this.allEvents = events;
            filtrerEtAfficher();
        });

        // Observation réactive du curseur de distance manipulé sur le fragment cartographique
        viewModel.getRadius().observe(getViewLifecycleOwner(), radius -> {
            this.currentRadius = radius;
            filtrerEtAfficher();
        });

        return view;
    }

    /** Calcule la distance séparant l'utilisateur de chaque événement et filtre la liste selon le rayon requis. */
    private void filtrerEtAfficher() {
        Location userLoc = recupererDernierePositionConnue();
        List<Evenement> filtered = new ArrayList<>();

        for (Evenement event : allEvents) {
            // Scénario nominal : les services de géolocalisation et permissions Android sont opérationnels
            if (userLoc != null) {
                float[] results = new float[1];
                // Calcul mathématique rigoureux de la distance orthodromique à la surface du globe
                Location.distanceBetween(
                        userLoc.getLatitude(), userLoc.getLongitude(),
                        event.getLatitude(), event.getLongitude(),
                        results
                );
                float dist = results[0] / 1000f; // Conversion des mètres en kilomètres

                // Rétention sélective de l'événement s'il se situe dans la zone géographique autorisée
                if (dist <= currentRadius) {
                    event.setDistance(dist);
                    filtered.add(event);
                }
            } else {
                // Scénario de repli (Fallback UI) : absence de coordonnées. Affichage universel non trié (Robustesse)
                event.setDistance(0);
                filtered.add(event);
            }
        }

        // Tri mathématique par proximité croissante si le point de départ utilisateur est authentifié
        if (userLoc != null) {
            Collections.sort(filtered, (e1, e2) -> Float.compare(e1.getDistance(), e2.getDistance()));
        }

        // Transmission de la liste filtrée et mise à jour de l'adaptateur graphique
        if (adapter != null) {
            adapter.updateList(filtered);
        }
    }

    /** Interroge les capteurs matériels pour extraire la position spatiale fraîche de l'appareil. */
    @Nullable
    private Location recupererDernierePositionConnue() {
        Context context = getContext();
        // Vérification de sécurité stricte des permissions accordées au runtime avant invocation (Android Sécurité)
        if (context == null || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return null;

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location net = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // Sélection temporelle du repère spatial le plus récent pour optimiser la précision du calcul
        if (gps != null && net != null) return gps.getTime() > net.getTime() ? gps : net;
        return (gps != null) ? gps : net;
    }
}