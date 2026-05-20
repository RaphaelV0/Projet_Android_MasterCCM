package fr.upjv.geoevent.domain.repository;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.DataServiceFactory;
import fr.upjv.geoevent.domain.data.IDataService;
import fr.upjv.geoevent.domain.models.Evenement;

/**
 * Repository centralisant les opérations de persistance pour les événements.
 * Il fait abstraction des sources de données cloud (Firestore et Storage) pour découpler
 * la logique métier de l'interface utilisateur graphique, conformément à l'Option 3.
 */
public class EvenementRepository {

    private static final String COLLECTION = "events";
    private IDataService dataService;

    /**
     * Constructeur du Repository.
     * Initialise le service de données de manière découplée via une Factory abstraite.
     */
    public EvenementRepository() {
        this.dataService = DataServiceFactory.create();
    }

    /**
     * Crée un événement dans le cloud. Si une image locale est fournie, elle est d'abord
     * téléversée de manière asynchrone sur Firebase Storage avant l'écriture dans Firestore.
     *
     * @param event    L'objet événement contenant les informations saisies par l'utilisateur.
     * @param imageUri L'URI locale de l'image sélectionnée dans la galerie (peut être nulle).
     */
    public void createEvent(Evenement event, Uri imageUri) {
        if (imageUri != null) {
            // 1. Génération d'un chemin unique à l'aide d'un UUID pour éviter toute collision de nom sur Storage
            String cheminFichier = "events/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(cheminFichier);

            // 2. Lancement du téléversement asynchrone (Thread d'arrière-plan)
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // 3. En cas de succès, récupération de l'URL de téléchargement publique générée par le serveur
                ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                    // 4. Encapsulation de l'URL dans une liste de chaînes (Strings) pour correspondre au modèle
                    List<String> listeUrls = new ArrayList<>();
                    listeUrls.add(downloadUri.toString());
                    event.setImages(listeUrls);

                    // 5. Écriture finale et atomique du document complet mis à jour dans Firestore
                    dataService.create(COLLECTION, event);
                });
            });
        } else {
            dataService.create(COLLECTION, event);
        }
    }

    /** Récupère l'intégralité des événements via le service de données abstrait. */
    public void getEvents(DataCallback callback) { dataService.getAll(COLLECTION, callback); }

    /** Signature de secours pour la création d'un événement brut sans flux média associé. */
    public void createEvent(Evenement event) { dataService.create(COLLECTION, event); }
    public void updateEvent(String id, Evenement event) { dataService.update(COLLECTION, id, event); }
    public void deleteEvent(String id) { dataService.delete(COLLECTION, id); }

}