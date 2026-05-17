package fr.upjv.geoevent.ui.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;

/**
 * Activité contrôlant le formulaire de création d'événement.
 * Intègre la capture d'images, l'autocomplétion prédictive des adresses et leur géocodage spatial.
 */
public class NewEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    ImageView imageEvent;
    TextView DateEventCreate;
    AutoCompleteTextView PostalAdresseCreate;
    EditText TitreEventCreate, DescriptionEventCreate;
    MaterialButton CreateEvent;
    Uri imageUri;

    private EvenementRepository evenementRepository;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private boolean isDateTimeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        evenementRepository = new EvenementRepository();

        imageEvent = findViewById(R.id.imageEvent);
        DateEventCreate = findViewById(R.id.DateEventCreate);
        PostalAdresseCreate = findViewById(R.id.PostalAdresseCreate);
        TitreEventCreate = findViewById(R.id.TitreEventCreate);
        DescriptionEventCreate = findViewById(R.id.DescriptionEventCreate);
        CreateEvent = findViewById(R.id.CreateEvent);

        // Déclenchement d'une intention implicite vers la galerie multimédia du terminal
        imageEvent.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        DateEventCreate.setOnClickListener(v -> showDateTimePicker());
        setupAddressAutocomplete();
    }

    /** Configure un écouteur de texte pour déclencher le Geocoder dès que la saisie atteint 3 caractères. */
    private void setupAddressAutocomplete() {
        PostalAdresseCreate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    searchAddresses(s.toString()); // Requête asynchrone d'arrière-plan
                }
            }
        });
    }

    /** Interroge le moteur de géocodage système pour récupérer des adresses géographiques valides. */
    private void searchAddresses(String query) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Extraction des 5 meilleures correspondances textuelles
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            List<String> suggestions = new ArrayList<>();

            if (addresses != null) {
                for (Address addr : addresses) {
                    suggestions.add(addr.getAddressLine(0)); // Récupération de la ligne d'adresse formatée
                }
            }

            // Rafraîchissement dynamique de la liste déroulante d'autocomplétion
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            PostalAdresseCreate.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        } catch (IOException e) {
            // Interception des pannes de connectivité réseau pour sécuriser l'expérience utilisateur (Robustesse)
            e.printStackTrace();
        }
    }

    /** Récupère le pointeur de fichier (Uri) de l'image sélectionnée et l'injecte dans la vue. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageEvent.setImageURI(imageUri); // Affichage de l'image locale en mémoire cache
        }
    }

    /** Déploie le dialogue système de sélection de date. */
    private void showDateTimePicker() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, monthOfYear, dayOfMonth) -> {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            showTimePicker();
        }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    /** Déploie le dialogue système de sélection de l'heure. */
    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            mHour = hourOfDay;
            mMinute = minute;
            isDateTimeSelected = true;

            String fullDate = String.format(Locale.getDefault(), "%02d/%02d/%d %02d:%02d",
                    mDay, mMonth + 1, mYear, mHour, mMinute);
            DateEventCreate.setText(fullDate);

        }, mHour, mMinute, true);
        timePickerDialog.show();
    }

    /** Traite la validation finale, géocode l'adresse saisie en points GPS et transmet le modèle au Repository. */
    public void OnClicKPublish(View view) {
        String titre = TitreEventCreate.getText().toString().trim();
        String lieuSaisie = PostalAdresseCreate.getText().toString().trim();
        String description = DescriptionEventCreate.getText().toString().trim();

        // Contrôle de surface obligatoire pour interdire la publication de formulaires incomplets
        if (titre.isEmpty() || !isDateTimeSelected || lieuSaisie.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute);
        Date dateEvenement = calendar.getTime();

        // Détermination des coordonnées géographiques (Latitude / Longitude) de l'adresse retenue
        double lat = 0.0;
        double lon = 0.0;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(lieuSaisie, 1);
            if (addresses != null && !addresses.isEmpty()) {
                lat = addresses.get(0).getLatitude();
                lon = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Evenement nouvelEvent = new Evenement(titre, description, lieuSaisie, dateEvenement);
        nouvelEvent.setLatitude(lat);
        nouvelEvent.setLongitude(lon);

        // Transmission au repository qui orchestrera de manière asynchrone l'envoi de l'image puis de l'événement
        evenementRepository.createEvent(nouvelEvent, imageUri);

        Toast.makeText(this, "Événement " + titre + " publié !", Toast.LENGTH_SHORT).show();

        // Destruction de l'activité pour libérer les ressources système (Gestion du cycle de vie)
        finish();
    }
}