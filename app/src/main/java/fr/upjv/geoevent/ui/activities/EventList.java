package fr.upjv.geoevent.ui.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Import manquant
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;
import fr.upjv.geoevent.ui.adapters.EventAdapter;

public class EventList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private EvenementRepository repository;
    private List<Evenement> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        repository = new EvenementRepository();
        recyclerView = findViewById(R.id.recyclerEvents);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        repository.getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                com.google.firebase.firestore.QuerySnapshot snapshot = (com.google.firebase.firestore.QuerySnapshot) data;

                List<Evenement> loadedEvents = snapshot.toObjects(Evenement.class);

                eventList.clear();
                eventList.addAll(loadedEvents);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventList.this, "Erreur : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}