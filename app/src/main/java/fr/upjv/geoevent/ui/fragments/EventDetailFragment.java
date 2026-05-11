package fr.upjv.geoevent.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;

public class EventDetailFragment extends Fragment {

    private TextView titreEvent, dateEvent, lieuEvent, descriptionEvent;
    private MaterialButton btnRegister;
    private Evenement currentEvent;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public EventDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        titreEvent = view.findViewById(R.id.TitreEvent);
        dateEvent = view.findViewById(R.id.DateEvent);
        lieuEvent = view.findViewById(R.id.LieuEvent);
        descriptionEvent = view.findViewById(R.id.DescriptionEvent);
        btnRegister = view.findViewById(R.id.RegisterEvent);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        if (getArguments() != null) {
            currentEvent = (Evenement) getArguments().getSerializable("EVENEMENT_EXTRA");
        }

        if (currentEvent != null) {
            displayEventDetails();
            checkRegistrationStatus();
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void displayEventDetails() {
        titreEvent.setText(currentEvent.getTitre());
        lieuEvent.setText(currentEvent.getLieu());
        descriptionEvent.setText(currentEvent.getDescription());

        if (currentEvent.getDateEvenement() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRANCE);
            dateEvent.setText(sdf.format(currentEvent.getDateEvenement()));
        }
    }

    private void checkRegistrationStatus() {
        if (auth.getCurrentUser() == null) return;

        // ID unique d'inscription pour vérifier si l'utilisateur est déjà inscrit
        String registrationId = currentEvent.getTitre() + "_" + auth.getUid();

        db.collection("inscriptionevent").document(registrationId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btnRegister.setText("Se désinscrire");
                        btnRegister.setOnClickListener(v -> desinscrire());
                    } else {
                        btnRegister.setText("S'inscrire");
                        btnRegister.setOnClickListener(v -> inscrire());
                    }
                });
    }

    private void inscrire() {
        if (auth.getCurrentUser() == null) return;
        btnRegister.setEnabled(false);

        String uid = auth.getUid();
        String eventTitre = currentEvent.getTitre();
        String registrationId = eventTitre + "_" + uid;

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            Map<String, Object> reg = new HashMap<>();
            reg.put("uid", uid);
            reg.put("eventTitre", eventTitre);
            reg.put("firstName", userDoc.getString("firstName"));
            reg.put("lastName", userDoc.getString("lastName"));
            reg.put("dateInscription", FieldValue.serverTimestamp());

            // 1. On crée l'inscription
            db.collection("inscriptionevent").document(registrationId).set(reg)
                    .addOnSuccessListener(aVoid -> {
                        // 2. On met à jour le compteur dans la collection "events"
                        updateCounter(eventTitre, 1, "Inscription réussie");
                    })
                    .addOnFailureListener(e -> btnRegister.setEnabled(true));
        });
    }

    private void desinscrire() {
        if (auth.getCurrentUser() == null) return;
        btnRegister.setEnabled(false);

        String eventTitre = currentEvent.getTitre();
        String registrationId = eventTitre + "_" + auth.getUid();

        // 1. On supprime l'inscription
        db.collection("inscriptionevent").document(registrationId).delete()
                .addOnSuccessListener(aVoid -> {
                    // 2. On décrémente le compteur
                    updateCounter(eventTitre, -1, "Désinscription réussie");
                })
                .addOnFailureListener(e -> btnRegister.setEnabled(true));
    }

    private void updateCounter(String eventTitre, int value, String message) {
        // REQUÊTE : On cherche le document dont le champ "titre" correspond
        db.collection("events")
                .whereEqualTo("titre", eventTitre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // On récupère l'ID réel (ex: MGqX3BB...)
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // MISE À JOUR DU COMPTEUR
                        // Important : "nombreParticipant" avec un P majuscule !
                        db.collection("events").document(documentId)
                                .update("nombreParticipant", FieldValue.increment(value))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(requireView()).navigateUp();
                                });
                    } else {
                        Log.e("FIRESTORE", "Aucun event trouvé avec le titre: " + eventTitre);
                        btnRegister.setEnabled(true);
                    }
                });
    }
}