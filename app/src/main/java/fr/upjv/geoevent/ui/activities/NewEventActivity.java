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
 * Activité permettant à l'utilisateur de créer un nouvel événement.
 * Elle gère la saisie du titre, de la description, de l'adresse (avec autocomplétion),
 * de la date/heure, ainsi que le choix d'une image illustrative.
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

    /**
     * Indique si l'utilisateur a bien sélectionné une date et une heure.
     * Utilisé lors de la validation du formulaire avant publication.
     */
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

        imageEvent.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        DateEventCreate.setOnClickListener(v -> showDateTimePicker());

        setupAddressAutocomplete();
    }

    /**
     * Configure l'autocomplétion du champ d'adresse postale.
     * Dès que l'utilisateur saisit au moins 3 caractères, une recherche
     * d'adresses est lancée via le Geocoder Android.
     */
    private void setupAddressAutocomplete() {
        PostalAdresseCreate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    searchAddresses(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Interroge le Geocoder Android pour obtenir une liste de suggestions
     * d'adresses correspondant à la saisie de l'utilisateur.
     * Les résultats sont affichés dans le menu déroulant du champ d'adresse.
     *
     * @param query Le texte saisi par l'utilisateur dans le champ d'adresse.
     */
    private void searchAddresses(String query) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 5);
            List<String> suggestions = new ArrayList<>();

            if (addresses != null) {
                for (Address addr : addresses) {
                    suggestions.add(addr.getAddressLine(0));
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, suggestions);
            PostalAdresseCreate.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère l'URI de l'image choisie dans la galerie et l'affiche
     * dans le composant ImageView prévu à cet effet.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageEvent.setImageURI(imageUri);
        }
    }

    /**
     * Affiche un DatePickerDialog permettant à l'utilisateur de sélectionner
     * la date de l'événement. À la confirmation, ouvre le sélecteur d'heure.
     */
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

    /**
     * Affiche un TimePickerDialog permettant à l'utilisateur de sélectionner
     * l'heure de l'événement. À la confirmation, met à jour l'affichage
     * de la date/heure dans le champ prévu et active le flag isDateTimeSelected.
     */
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

    /**
     * Appelée lors du clic sur le bouton "Publier".
     * Valide les champs du formulaire, géocode l'adresse saisie pour obtenir
     * les coordonnées GPS, crée un objet Evenement et le sauvegarde via le repository.
     *
     * @param view La vue ayant déclenché l'événement (le bouton "Publier").
     */
    public void OnClicKPublish(View view) {
        String titre = TitreEventCreate.getText().toString().trim();
        String lieuSaisie = PostalAdresseCreate.getText().toString().trim();
        String description = DescriptionEventCreate.getText().toString().trim();

        if (titre.isEmpty() || !isDateTimeSelected || lieuSaisie.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_LONG).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(mYear, mMonth, mDay, mHour, mMinute);
        Date dateEvenement = calendar.getTime();

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

        evenementRepository.createEvent(nouvelEvent);

        Toast.makeText(this, "Événement " + titre + " publié !", Toast.LENGTH_SHORT).show();
        finish();
    }
}