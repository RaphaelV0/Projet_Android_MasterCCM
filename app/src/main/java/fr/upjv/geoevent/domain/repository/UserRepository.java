package fr.upjv.geoevent.domain.repository;

import java.util.HashMap;
import java.util.Map;

import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.DataServiceFactory;
import fr.upjv.geoevent.domain.data.IDataService;
import fr.upjv.geoevent.domain.models.User;

/**
 * Repository gérant l'accès aux données des utilisateurs dans Firestore.
 * Centralise les opérations de lecture, création et mise à jour des profils utilisateurs.
 */
public class UserRepository {

    private static final String COLLECTION = "users";

    private final IDataService dataService;

    public UserRepository() {
        this.dataService = DataServiceFactory.create();
    }

    /**
     * Récupère le profil complet d'un utilisateur par son identifiant unique (UID).
     * @param uid L'UID de l'utilisateur à récupérer.
     * @param callback Callback pour gérer le résultat de la requête asynchrone.
     */
    public void getUserById(String uid, DataCallback callback) {
        dataService.getById(COLLECTION, uid, callback);
    }

    /**
     * Crée le document profil dans Firestore juste après l'inscription.
     * Utilise l'UID de l'utilisateur comme identifiant de document pour faciliter les recherches.
     * @param user L'objet utilisateur contenant les informations de base.
     */
    public void createUser(User user) {
        // On utilise update qui fait un .set() dans FirebaseDataService.
        // Si le document n'existe pas, il est créé avec l'ID spécifié (user.getUid()).
        dataService.update(COLLECTION, user.getUid(), user);
    }

    /**
     * Met à jour les informations du profil utilisateur (prénom, nom, etc.).
     * @param uid L'UID de l'utilisateur à mettre à jour.
     * @param user L'objet utilisateur contenant les nouvelles données.
     */
    public void updateUser(String uid, User user) {
        dataService.update(COLLECTION, uid, user);
    }

    /**
     * Supprime le document profil d'un utilisateur.
     * @param uid L'UID de l'utilisateur à supprimer.
     */
    public void deleteUser(String uid) {
        dataService.delete(COLLECTION, uid);
    }

    /**
     * Récupère la liste de tous les utilisateurs enregistrés.
     * Note : Usage réservé aux fonctionnalités d'administration.
     * @param callback Callback pour gérer la liste des résultats.
     */
    public void getAllUsers(DataCallback callback) {
        dataService.getAll(COLLECTION, callback);
    }

    /**
     * Met à jour spécifiquement le rôle d'un utilisateur sans impacter les autres champs.
     * @param uid L'UID de l'utilisateur concerné.
     * @param role Le nouveau rôle à attribuer (ex: "admin", "user").
     * @param callback Callback pour confirmer la réussite de l'opération.
     */
    public void updateUserRole(String uid, String role, DataCallback callback) {
        Map<String, Object> patch = new HashMap<>();
        patch.put("role", role);
        dataService.update(COLLECTION, uid, patch);
    }
}
