package fr.upjv.geoevent.domain.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fr.upjv.geoevent.domain.models.User;
import fr.upjv.geoevent.domain.repository.UserRepository;
import fr.upjv.geoevent.services.IAuthService;

/**
 * Implémentation Firebase de IAuthService.
 */
public class FirebaseAuthServiceImpl implements IAuthService {

    private final FirebaseAuth auth;

    public FirebaseAuthServiceImpl() {
        this.auth = FirebaseAuth.getInstance();
    }

    @Override
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser != null) {
                        callback.onSuccess(mapFirebaseUserToUser(fbUser));
                    } else {
                        callback.onFailure("Erreur lors de la récupération de l'utilisateur.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void register(String email, String password,
                         String firstName, String lastName,
                         AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser != null) {
                        String uid = fbUser.getUid();
                        User user = new User(uid, email, firstName, lastName);
                        
                        // Sauvegarder le profil dans Firestore via UserRepository
                        new UserRepository().createUser(user);
                        
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Erreur lors de la création du compte.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    @Override
    public void logout() {
        auth.signOut();
    }

    @Override
    public User getCurrentUser() {
        FirebaseUser fbUser = auth.getCurrentUser();
        if (fbUser == null) return null;
        return mapFirebaseUserToUser(fbUser);
    }

    @Override
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    @Override
    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Mappe un FirebaseUser (SDK) vers notre modèle User (Métier).
     */
    private User mapFirebaseUserToUser(FirebaseUser fbUser) {
        User user = new User();
        user.setUid(fbUser.getUid());
        user.setEmail(fbUser.getEmail());
        // Note: Le prénom/nom ne sont pas dans FirebaseUser par défaut, 
        // ils sont stockés dans Firestore. Ici on map ce qu'on peut depuis Auth.
        return user;
    }
}
