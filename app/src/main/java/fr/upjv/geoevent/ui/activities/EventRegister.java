package fr.upjv.geoevent.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import fr.upjv.geoevent.R;

public class EventRegister extends AppCompatActivity {

    ImageView imageEventDetail;
    TextView titreEvent, dateEvent, lieuEvent, descriptionEvent;
    MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_register);

        imageEventDetail = findViewById(R.id.ImageEventDetail);
        titreEvent = findViewById(R.id.TitreEvent);
        dateEvent = findViewById(R.id.DateEvent);
        lieuEvent = findViewById(R.id.LieuEvent);
        descriptionEvent = findViewById(R.id.DescriptionEvent);
        btnRegister = findViewById(R.id.RegisterEvent);
    }

    public void OnClickInscriptionEvent(View view) {
        Toast.makeText(this, "Inscription réussie à l'événement !", Toast.LENGTH_LONG).show();

        btnRegister.setEnabled(false);
        btnRegister.setText("Déjà inscrit");
    }
}