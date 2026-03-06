package fr.upjv.geoevent.domain.data.firebase;

import fr.upjv.geoevent.services.IAuthService;
import fr.upjv.geoevent.domain.models.User;

/**
 * Implémentation Firebase de IAuthService.
 *
 * STUB — La logique Firebase (FirebaseAuth, Firestore) est à compléter
 * par le collègue responsable de l'intégration backend.
 *
 * La ConnexionActivity n'a connaissance que de IAuthService :
 * elle ne sait pas que Firebase existe.
 */
public class FirebaseAuthServiceImpl implements IAuthService {

    @Override
    public void login(String email, String password, AuthCallback callback) {
        // TODO (collègue Firebase) :
        //   FirebaseAuth.getInstance()
        //       .signInWithEmailAndPassword(email, password)
        //       .addOnSuccessListener(authResult -> {
        //           FirebaseUser fbUser = authResult.getUser();
        //           User user = mapFirebaseUserToUser(fbUser);
        //           callback.onSuccess(user);
        //       })
        //       .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        callback.onFailure("Firebase non encore intégré");
    }

    @Override
    public void register(String email, String password,
                         String firstName, String lastName,
                         AuthCallback callback) {
        // TODO (collègue Firebase) :
        //   FirebaseAuth.getInstance()
        //       .createUserWithEmailAndPassword(email, password)
        //       .addOnSuccessListener(authResult -> {
        //           String uid = authResult.getUser().getUid();
        //           User user = new User(uid, email, firstName, lastName);
        //           // Sauvegarder le profil dans Firestore
        //           saveUserProfile(user, callback);
        //       })
        //       .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        callback.onFailure("Firebase non encore intégré");
    }

    @Override
    public void logout() {
        // TODO : FirebaseAuth.getInstance().signOut();
    }

    @Override
    public User getCurrentUser() {
        // TODO :
        //   FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        //   if (fbUser == null) return null;
        //   return mapFirebaseUserToUser(fbUser);
        return null;
    }

    @Override
    public boolean isLoggedIn() {
        // TODO : return FirebaseAuth.getInstance().getCurrentUser() != null;
        return false;
    }

    @Override
    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        // TODO :
        //   FirebaseAuth.getInstance().sendPasswordResetEmail(email)
        //       .addOnSuccessListener(v -> callback.onSuccess(null))
        //       .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        callback.onFailure("Firebase non encore intégré");
    }
}