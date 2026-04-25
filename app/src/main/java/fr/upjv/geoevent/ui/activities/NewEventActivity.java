package fr.upjv.geoevent.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.appcompat.app.AppCompatActivity;


import fr.upjv.geoevent.R;

public class NewEventActivity extends AppCompatActivity {

    ImageView imageEvent;
    TextView DateEventCreate;
    TextView PostalAdresseCreate;
    EditText TitreEventCreate;
    EditText DescriptionEventCreate;
    MaterialButton CreateEvent;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        imageEvent = findViewById(R.id.imageEvent);
        DateEventCreate = findViewById(R.id.DateEventCreate);
        PostalAdresseCreate = findViewById(R.id.PostalAdresseCreate);
        TitreEventCreate = findViewById(R.id.TitreEventCreate);
        DescriptionEventCreate = findViewById(R.id.DescriptionEventCreate);
        CreateEvent = findViewById(R.id.CreateEvent);

        imageEvent.setOnClickListener(v-> {
            Toast.makeText(this, "Ouvrir la galerie...", Toast.LENGTH_SHORT).show();
        });

        DateEventCreate.setOnClickListener(v -> {
            Toast.makeText(this, "Ouvrir le calendrier...", Toast.LENGTH_SHORT).show();
        });
    }

    public void OnClicKPublish(View view) {
        String titre = TitreEventCreate.getText().toString();
        String date = DateEventCreate.getText().toString();
        String lieu = PostalAdresseCreate.getText().toString();
        String description = DescriptionEventCreate.getText().toString();

        if (titre.isEmpty() || date.isEmpty() || lieu.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Événement " + titre + " publié !", Toast.LENGTH_SHORT).show();

        finish();
}}