package fr.upjv.geoevent.domain.data.firebase;

import com.google.firebase.firestore.FirebaseFirestore;

import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.IDataService;


public class FirebaseDataService implements IDataService {

    private FirebaseFirestore db;

    public FirebaseDataService() {
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void create(String collection, Object data) {

        db.collection(collection)
                .add(data);
    }

    @Override
    public void update(String collection, String id, Object data) {

        db.collection(collection)
                .document(id)
                .set(data);
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
    public void getAll(String collection, DataCallback callback) {

        db.collection(collection)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }
}

