package fr.upjv.geoevent.services;

import fr.upjv.geoevent.domain.models.User;

/**
 * Interface d'abstraction pour le service d'authentification.
 *
 * Toute la couche UI (ConnexionActivity) dépend uniquement de cette interface.
 * Cela permet de remplacer Firebase par Supabase (ou tout autre provider)
 * sans toucher à une seule ligne de code d'interface graphique.
 *
 * Implémentations attendues :
 *  - FirebaseAuthServiceImpl  (réalisée par le collègue en charge de Firebase)
 *  - SupabaseAuthServiceImpl  (alternative possible)
 */
public interface IAuthService {

    /**
     * Callback générique pour toutes les opérations asynchrones d'auth.
     */
    interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String errorMessage);
    }

    /**
     * Connecte un utilisateur existant avec email + mot de passe.
     *
     * @param email    Adresse email de l'utilisateur
     * @param password Mot de passe en clair (transmis directement au SDK auth)
     * @param callback Résultat de l'opération asynchrone
     */
    void login(String email, String password, AuthCallback callback);

    /**
     * Crée un nouveau compte utilisateur.
     *
     * @param email     Adresse email
     * @param password  Mot de passe choisi (min. 8 caractères)
     * @param firstName Prénom
     * @param lastName  Nom de famille
     * @param callback  Résultat de l'opération asynchrone
     */
    void register(String email, String password,
                  String firstName, String lastName,
                  AuthCallback callback);


    void loginWithGoogle(String idToken, AuthCallback callback);
    /**
     * Déconnecte l'utilisateur courant de la session.
     */
    void logout();

    /**
     * Retourne l'utilisateur actuellement connecté, ou null si aucune session.
     */
    User getCurrentUser();

    /**
     * Indique si une session utilisateur est active.
     */
    boolean isLoggedIn();

    /**
     * Envoie un email de réinitialisation de mot de passe.
     *
     * @param email    Adresse email du compte concerné
     * @param callback Résultat de l'opération asynchrone
     */
    void sendPasswordResetEmail(String email, AuthCallback callback);
}