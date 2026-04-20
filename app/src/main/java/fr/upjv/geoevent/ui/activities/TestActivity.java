package fr.upjv.geoevent.ui.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import fr.upjv.geoevent.R;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.models.Evenement;
import fr.upjv.geoevent.domain.repository.EvenementRepository;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        EvenementRepository repository = new EvenementRepository();

        // Création d'un nouvel événement
        Evenement event = new Evenement();
        event.setTitre("Concert à Saint-Quentin");
        event.setDescription("Concert payant au palais des sports");
        event.setLatitude(49.8566);
        event.setLongitude(7.3522);

        repository.createEvent(event);


        // Récupération des événements (exemple)


        repository.getEvents(new DataCallback() {
            @Override
            public void onSuccess(Object data) {
                //System.out.println("Récupération réussie: " + data);

                //Log.i("LOG_Evenement", data.toString());
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });




    }
}