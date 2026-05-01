package fr.upjv.geoevent.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;
import fr.upjv.geoevent.ui.adapters.EventAdapter;

public class ListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private EvenementRepository repository;
    private List<Evenement> eventList = new ArrayList<>();

    public ListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        repository = new EvenementRepository();

        recyclerView = view.findViewById(R.id.recyclerEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(eventList, getContext());
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        repository.getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                com.google.firebase.firestore.QuerySnapshot snapshot =
                        (com.google.firebase.firestore.QuerySnapshot) data;

                List<Evenement> loadedEvents = snapshot.toObjects(Evenement.class);

                eventList.clear();
                eventList.addAll(loadedEvents);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(),
                            "Erreur : " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}