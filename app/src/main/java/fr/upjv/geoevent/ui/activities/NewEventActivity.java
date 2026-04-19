package fr.upjv.geoevent.ui.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import fr.upjv.geoevent.R;

public class NewEventActivity extends AppCompatActivity {

    ImageView imageEvent;
    TextView DateEventCreate;
    TextView PostalAdresseCreate;
    EditText TitreEventCreate;
    EditText DescriptionEventCreate;
    Button CreateEvent;

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
    }

    public void OnClicKPublish(View view) {
    }
}