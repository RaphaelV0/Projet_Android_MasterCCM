package fr.upjv.geoevent.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.auth.AuthServiceFactory;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.models.InscriptionEvent;
import fr.upjv.geoevent.domain.models.User;
import fr.upjv.geoevent.domain.repository.EvenementRepository;
import fr.upjv.geoevent.domain.repository.InscritpionRepository;
import fr.upjv.geoevent.domain.repository.UserRepository;
import fr.upjv.geoevent.services.IAuthService;
import fr.upjv.geoevent.ui.activities.ConnexionActivity;
import fr.upjv.geoevent.ui.adapters.EventAdapter;
import fr.upjv.geoevent.ui.viewmodels.EventViewModel;

public class ProfileFragment extends Fragment {

    private IAuthService authService;
    private TextView usernameField;
    private TextView emailField;
    private UserRepository userRepository;
    private EvenementRepository evenementRepository;

    private InscritpionRepository inscriptionEvent;

    List<InscriptionEvent> inscriptionEvents;
    List<Evenement> evenementList;
    List<Evenement> evenementsInscrits = new ArrayList<>();

    private EventViewModel viewModelEvenement;
    private RecyclerView recyclerView;
    private EventAdapter adapter;


    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userRepository = new UserRepository();
        evenementRepository = new EvenementRepository();
        inscriptionEvent = new InscritpionRepository();

        usernameField = view.findViewById(R.id.userName);
        emailField = view.findViewById(R.id.userEmail);

        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        adapter = new EventAdapter(new ArrayList<>(), requireContext());
        recyclerView.setAdapter(adapter);

        //  ViewModel initialisé EN PREMIER, avant tout chargement
        viewModelEvenement = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        //  Observation branchée ici — met à jour l'adapter dès que setEvents est appelé
        viewModelEvenement.getEvents().observe(getViewLifecycleOwner(), evenements -> {
            if (evenements != null) {
                Log.d("DEBUG", "Observation déclenchée : " + evenements.size() + " événements");
                adapter.updateList(evenements);
            }
        });

        authService = AuthServiceFactory.create();
        String uid = authService.getCurrentUser().getUid();

        userRepository.getUserById(uid, new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof DocumentSnapshot) {
                    DocumentSnapshot doc = (DocumentSnapshot) data;
                    if (doc.exists()) {
                        requireActivity().runOnUiThread(() -> {
                            usernameField.setText(doc.getString("firstName") + " " + doc.getString("lastName"));
                            emailField.setText(doc.getString("email"));
                        });
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("ProfileFragment", "Erreur chargement utilisateur", e);
            }
        });

        //  Chargements lancés APRÈS l'init du ViewModel
        this.loadInscription(uid);
        this.loadEvents();

        view.findViewById(R.id.logoutButton).setOnClickListener(v -> {
            authService.logout();
            Intent intent = new Intent(requireActivity(), ConnexionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void traitementListe() {
        if (inscriptionEvents == null || evenementList == null) return;

        List<String> titresInscrits = new ArrayList<>();
        for (InscriptionEvent inscription : inscriptionEvents) {
            titresInscrits.add(inscription.getEventTitre());
        }

        evenementsInscrits.clear();
        for (Evenement evenement : evenementList) {
            if (titresInscrits.contains(evenement.getTitre())) {
                evenementsInscrits.add(evenement);
            }
        }

        Log.d("ProfileFragment", "Événements inscrits : " + evenementsInscrits.size());

        //  setValue doit être appelé sur le thread principal
        requireActivity().runOnUiThread(() ->
                viewModelEvenement.setEvents(evenementsInscrits)
        );
    }


    private void loadEvents() {
        evenementRepository.getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof QuerySnapshot) {
                    evenementList = ((QuerySnapshot) data).toObjects(Evenement.class);
                    traitementListe();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("ProfileFragment", "Erreur", e);
            }
        });
    }

    private void loadInscription(String uid) {
        inscriptionEvent.getInscriptionEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                if (data instanceof QuerySnapshot) {
                    List<InscriptionEvent> toutes = ((QuerySnapshot) data).toObjects(InscriptionEvent.class);
                    inscriptionEvents = new ArrayList<>();
                    for (InscriptionEvent inscription : toutes) {
                        if (uid.equals(inscription.getUid())) {
                            inscriptionEvents.add(inscription);
                        }
                    }
                    traitementListe();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("ProfileFragment", "Erreur", e);
            }
        });
    }


}
