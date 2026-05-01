package fr.upjv.geoevent.ui.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;

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
        Date madate = new Date();
        // Création d'un nouvel événement
        Evenement event = new Evenement("Concert à Saint-Quentin", "description test", "10 Rue de Flandre, 59210 Coudekerque-Branche, France", madate);
        Evenement event2 = new Evenement("Jeux vidéo chez Brice", "Attention c'est une blague", "5 Av. de Remicourt, 02100 Saint-Quentin, France", madate);

        Evenement event3 = new Evenement("Allons à l'expo !! ", "Là c'est une blague", "Musée des Beaux-Arts Antoine Lécuyer, 28 Rue Antoine Lécuyer, 02100 Saint-Quentin", madate);


        event.setPosition(49.848736, 3.294313);
        event2.setPosition(49.8376020, 3.3051780);
        event3.setPosition(49.8500072, 3.2814713);


        //repository.createEvent(event);
        //repository.createEvent(event2);
        repository.createEvent(event3);
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