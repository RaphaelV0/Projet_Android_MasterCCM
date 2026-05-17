package fr.upjv.geoevent.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide; // Bibliothèque asynchrone de chargement d'images distantes
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
 * Fragment gérant la fiche descriptive d'un événement.
 * Intègre le chargement réseau de l'image de couverture via Glide et pilote les inscriptions.
 */
public class EventDetailFragment extends Fragment {

    private TextView titreEvent, dateEvent, lieuEvent, descriptionEvent;
    private MaterialButton btnRegister;
    private ImageView imageEventDetail;
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
        imageEventDetail = view.findViewById(R.id.ImageEventDetail);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        if (getArguments() != null) {
            currentEvent = (Evenement) getArguments().getSerializable("EVENEMENT_EXTRA");
        }

        // Si le transfert de données est validé, affichage des informations et gestion de l'image distante
        if (currentEvent != null) {
            displayEventDetails();
            checkRegistrationStatus();

            if (currentEvent.getImages() != null && !currentEvent.getImages().isEmpty() && currentEvent.getImages().get(0) != null) {
                String urlImage = currentEvent.getImages().get(0); // Récupération directe de la String URL

                // Traitement d'affichage asynchrone fluide découplé du thread principal (Main Thread)
                Glide.with(this)
                        .load(urlImage)
                        .placeholder(android.R.drawable.ic_menu_gallery) // Image d'attente pendant la requête réseau
                        .error(android.R.drawable.ic_menu_report_image)    // Visuel de secours en cas de lien mort
                        .centerCrop()                                     // Ajustement géométrique proportionnel
                        .into(imageEventDetail);
            } else {
                // Initialisation par défaut si l'événement ne comporte aucun média illustratif
                imageEventDetail.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    /** Projette les données textuelles de l'événement sur l'interface graphique. */
    private void displayEventDetails() {
        titreEvent.setText(currentEvent.getTitre());
        lieuEvent.setText(currentEvent.getLieu());
        descriptionEvent.setText(currentEvent.getDescription());

        if (currentEvent.getDateEvenement() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRANCE);
            dateEvent.setText(sdf.format(currentEvent.getDateEvenement()));
        }
    }

    /** Contrôle l'existence d'une inscription sur Firestore via un ID calculé unique (Titre_UID). */
    private void checkRegistrationStatus() {
        if (auth.getCurrentUser() == null) return;

        String registrationId = currentEvent.getTitre() + "_" + auth.getUid();

        db.collection("inscriptionevent").document(registrationId)
                .get()
                .addOnSuccessListener(doc -> {
                    // Mutation d'état réactive du bouton pivot unique
                    if (doc.exists()) {
                        btnRegister.setText("Se désinscrire");
                        btnRegister.setOnClickListener(v -> desinscrire());
                    } else {
                        btnRegister.setText("S'inscrire");
                        btnRegister.setOnClickListener(v -> inscrire());
                    }
                });
    }

    /** Persiste l'inscription en base de données et ordonne l'incrémentation atomique du compteur d'inscrits. */
    private void inscrire() {
        if (auth.getCurrentUser() == null) return;
        btnRegister.setEnabled(false); // Verrouillage immédiat pour bloquer les clics concurrents en cours de traitement

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

    /** Supprime l'inscription en base de données et ordonne la décrémentation du compteur. */
    private void desinscrire() {
        if (auth.getCurrentUser() == null) return;
        btnRegister.setEnabled(false); // Verrouillage antispam

        String eventTitre = currentEvent.getTitre();
        String registrationId = eventTitre + "_" + auth.getUid();

        db.collection("inscriptionevent").document(registrationId).delete()
                .addOnSuccessListener(aVoid -> {
                    updateCounter(eventTitre, -1, "Désinscription réussie");
                })
                .addOnFailureListener(e -> btnRegister.setEnabled(true));
    }

    /** Applique une modification arithmétique atomique sécurisée sur le compteur de participants Firestore. */
    private void updateCounter(String eventTitre, int value, String message) {
        db.collection("events")
                .whereEqualTo("titre", eventTitre)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Utilisation de FieldValue.increment pour faire exécuter l'opération mathématique par le cloud (Sans conflits)
                        db.collection("events").document(documentId)
                                .update("nombreParticipant", FieldValue.increment(value))
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                                    // navigateUp() dépile l'écran actuel de la Backstack et renvoie dynamiquement à l'écran émetteur
                                    Navigation.findNavController(requireView()).navigateUp();
                                });
                    } else {
                        Log.e("FIRESTORE", "Aucun event trouvé avec le titre: " + eventTitre);
                        btnRegister.setEnabled(true);
                    }
                });
    }
}