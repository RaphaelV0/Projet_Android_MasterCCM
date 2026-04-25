package fr.upjv.geoevent.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Locale;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;

public class EventRegister extends AppCompatActivity {

    ImageView imageEventDetail;
    TextView titreEvent, dateEvent, lieuEvent, descriptionEvent, nbParticipants;
    MaterialButton btnRegister;

    private Evenement currentEvent;
    private EvenementRepository evenementRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_register);

        evenementRepository = new EvenementRepository();

        imageEventDetail = findViewById(R.id.ImageEventDetail);
        titreEvent = findViewById(R.id.TitreEvent);
        dateEvent = findViewById(R.id.DateEvent);
        lieuEvent = findViewById(R.id.LieuEvent);
        descriptionEvent = findViewById(R.id.DescriptionEvent);
      //  nbParticipants = findViewById(R.id.NbParticipants); // Si tu as un champ pour le nombre
        btnRegister = findViewById(R.id.RegisterEvent);

        currentEvent = (Evenement) getIntent().getSerializableExtra("EVENEMENT_EXTRA");

        if (currentEvent != null) {
            displayEventDetails();
        } else {
            Toast.makeText(this, "Erreur : Événement introuvable", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void displayEventDetails() {
        titreEvent.setText(currentEvent.getTitre());
        lieuEvent.setText(currentEvent.getLieu());
        descriptionEvent.setText(currentEvent.getDescription());

     int count = currentEvent.getNombreParticipant();
      if (nbParticipants != null) {
           nbParticipants.setText(count + (count > 1 ? " participants" : " participant"));
        }

        if (currentEvent.getDateEvenement() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'à' HH:mm", Locale.FRANCE);
            dateEvent.setText(sdf.format(currentEvent.getDateEvenement()));
        }

        // Note : Pour l'image, comme on n'a pas encore Firebase Storage,
        // on garde l'image par défaut du XML ou on pourrait charger une ressource.
    }


    public void OnClickInscriptionEvent(View view) {
        int nouveauNb = currentEvent.getNombreParticipant() + 1;
        currentEvent.setNombreParticipant(nouveauNb);


        Toast.makeText(this, "Inscription réussie à l'événement : " + currentEvent.getTitre(), Toast.LENGTH_LONG).show();

        btnRegister.setEnabled(false);
        btnRegister.setText("Déjà inscrit");

        if (nbParticipants != null) {
            nbParticipants.setText(nouveauNb + " participants");
        }
    }
}