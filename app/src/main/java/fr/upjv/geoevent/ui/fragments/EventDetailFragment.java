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

/**
 * Fragment affichant le détail d'un événement sélectionné dans la liste.
 * Permet à l'utilisateur authentifié de s'inscrire ou se désinscrire de l'événement.
 * Le compteur de participants dans Firestore est mis à jour en conséquence.
 */
public class EventDetailFragment extends Fragment {

    private TextView titreEvent, dateEvent, lieuEvent, descriptionEvent;
    private MaterialButton btnRegister;

    /** L'événement dont on affiche le détail, transmis par les arguments du fragment. */
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

    /**
     * Remplit les champs textuels de la vue avec les informations de l'événement courant.
     * La date est formatée en français (ex : "12 juin 2025 à 14:30").
     */
    private void displayEventDetails() {
        titreEvent.setText(currentEvent.getTitre());
        lieuEvent.setText(currentEvent.getLieu());
        descriptionEvent.setText(currentEvent.getDescription());

        if (currentEvent.getDateEvenement() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRANCE);
            dateEvent.setText(sdf.format(currentEvent.getDateEvenement()));
        }
    }

    /**
     * Vérifie dans Firestore si l'utilisateur courant est déjà inscrit à cet événement.
     * L'identifiant d'inscription est construit à partir du titre de l'événement et de l'UID Firebase.
     * Selon le résultat, le bouton affiche "S'inscrire" ou "Se désinscrire".
     */
    private void checkRegistrationStatus() {
        if (auth.getCurrentUser() == null) return;

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

    /**
     * Inscrit l'utilisateur courant à l'événement.
     * Crée un document dans la collection "inscriptionevent" avec ses informations
     * (UID, prénom, nom, date d'inscription), puis incrémente le compteur de participants
     * dans la collection "events".
     */
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

            db.collection("inscriptionevent").document(registrationId).set(reg)
                    .addOnSuccessListener(aVoid -> {
                        updateCounter(eventTitre, 1, "Inscription réussie");
                    })
                    .addOnFailureListener(e -> btnRegister.setEnabled(true));
        });
    }

    /**
     * Désinscrit l'utilisateur courant de l'événement.
     * Supprime le document correspondant dans "inscriptionevent",
     * puis décrémente le compteur de participants dans la collection "events".
     */
    private void desinscrire() {
        if (auth.getCurrentUser() == null) return;
        btnRegister.setEnabled(false);

        String eventTitre = currentEvent.getTitre();
        String registrationId = eventTitre + "_" + auth.getUid();

        db.collection("inscriptionevent").document(registrationId).delete()
                .addOnSuccessListener(aVoid -> {
                    updateCounter(eventTitre, -1, "Désinscription réussie");
                })
                .addOnFailureListener(e -> btnRegister.setEnabled(true));
    }

    /**
     * Met à jour le compteur de participants d'un événement dans Firestore.
     * Recherche d'abord le document événement par son titre, puis applique
     * un incrément (positif ou négatif) sur le champ "nombreParticipant".
     * Navigue vers l'écran précédent une fois la mise à jour effectuée.
     *
     * @param eventTitre Le titre de l'événement à mettre à jour.
     * @param value      La valeur à ajouter au compteur (+1 pour inscription, -1 pour désinscription).
     * @param message    Le message toast à afficher après la mise à jour.
     */
    private void updateCounter(String eventTitre, int value, String message) {
        db.collection("events")
                .whereEqualTo("titre", eventTitre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

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