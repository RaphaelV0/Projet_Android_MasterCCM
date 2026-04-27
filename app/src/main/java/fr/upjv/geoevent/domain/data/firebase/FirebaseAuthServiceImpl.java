package fr.upjv.geoevent.domain.data.firebase;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

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
    public void loginWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    if (fbUser != null) {
                        User user = mapFirebaseUserToUser(fbUser);
                        
                        // Si c'est une première connexion, on crée le profil Firestore
                        if (authResult.getAdditionalUserInfo() != null && authResult.getAdditionalUserInfo().isNewUser()) {
                            // On essaie de récupérer le nom/prénom depuis Google
                            String displayName = fbUser.getDisplayName();
                            if (displayName != null && !displayName.isEmpty()) {
                                String[] parts = displayName.split(" ", 2);
                                user.setFirstName(parts[0]);
                                if (parts.length > 1) user.setLastName(parts[1]);
                            }
                            new UserRepository().createUser(user);
                        }
                        
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Erreur lors de la connexion Google.");
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
        user.setPhotoUrl(fbUser.getPhotoUrl() != null ? fbUser.getPhotoUrl().toString() : null);
        return user;
    }
}
