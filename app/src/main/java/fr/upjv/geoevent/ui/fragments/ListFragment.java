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
 * Fragment affichant la liste des événements filtrés selon la position de l'utilisateur
 * et le rayon de recherche sélectionné sur la carte.
 * Il observe le ViewModel partagé pour réagir aux changements d'événements ou de rayon
 * et met à jour la liste triée par distance croissante.
 */
public class ListFragment extends Fragment {

    private EventAdapter adapter;
    private EventViewModel viewModel;

    /** Liste complète des événements récupérés depuis Firestore via le ViewModel. */
    private List<Evenement> allEvents = new ArrayList<>();

    /** Rayon de filtrage en kilomètres, synchronisé avec le MapFragment via le ViewModel. */
    private int currentRadius = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        /**
         * Observe la liste d'événements exposée par le ViewModel.
         * À chaque mise à jour, relance le filtrage et le tri pour actualiser l'affichage.
         */
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            this.allEvents = events;
            filtrerEtAfficher();
        });

        /**
         * Observe le rayon de filtrage mis à jour par le MapFragment.
         * Tout changement de rayon déclenche un nouveau filtrage de la liste.
         */
        viewModel.getRadius().observe(getViewLifecycleOwner(), radius -> {
            this.currentRadius = radius;
            filtrerEtAfficher();
        });

        return view;
    }

    /**
     * Filtre la liste complète des événements selon le rayon défini et la position de l'utilisateur,
     * calcule la distance entre l'utilisateur et chaque événement, puis trie les résultats
     * par distance croissante avant de les transmettre à l'adaptateur.
     * Si la localisation n'est pas disponible (permission refusée ou GPS absent),
     * tous les événements sont affichés sans filtrage ni tri.
     */
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

        if (userLoc != null) {
            Collections.sort(filtered, (e1, e2) -> Float.compare(e1.getDistance(), e2.getDistance()));
        }

        if (adapter != null) {
            adapter.updateList(filtered);
        }
    }

    /**
     * Retourne la dernière position connue de l'appareil en comparant les données
     * du fournisseur GPS et du fournisseur réseau.
     * Retourne null si la permission de localisation n'est pas accordée
     * ou si aucune position n'est disponible.
     *
     * @return La position la plus récente disponible, ou null.
     */
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