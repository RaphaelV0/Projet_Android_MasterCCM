package fr.upjv.geoevent.domain.data.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.IDataService;


public class FirebaseDataService implements IDataService {

    private FirebaseFirestore db;

    public FirebaseDataService() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void create(String collection, Object data) {
        db.collection(collection).add(data);
    }

    @Override
    public void update(String collection, String id, Object data) {
        // Utilisation de SetOptions.merge() pour ne pas écraser les champs existants
        db.collection(collection)
                .document(id)
                .set(data, SetOptions.merge());
    }

    @Override
    public void delete(String collection, String id) {
        db.collection(collection)
                .document(id)
                .delete();
    }

    @Override
    public void getById(String collection, String id, DataCallback callback) {
        db.collection(collection)
                .document(id)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void getByName(String collection, String name, DataCallback callback) {
        db.collection(collection)
                .document(name)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    public void getInscriptionsByUser(String collection, String uid, DataCallback callback) {
        db.collection(collection)
                .whereEqualTo("uid", uid)  // filtre sur le champ uid
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }


    @Override
    public void getAll(String collection, DataCallback callback) {
        db.collection(collection)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }




}
