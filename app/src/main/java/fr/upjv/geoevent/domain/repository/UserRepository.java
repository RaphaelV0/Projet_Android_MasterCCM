package fr.upjv.geoevent.domain.repository;

import java.util.HashMap;
import java.util.Map;

import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.DataServiceFactory;
import fr.upjv.geoevent.domain.data.IDataService;
import fr.upjv.geoevent.domain.models.User;

public class UserRepository {

    private static final String COLLECTION = "users";

    private IDataService dataService;

    public UserRepository() {
        this.dataService = DataServiceFactory.create();
    }

    // Récupère le profil complet d'un user par son UID
    public void getUserById(String uid, DataCallback callback) {
        dataService.getById(COLLECTION, uid, callback);
    }

    // Crée le document profil dans Firestore juste après l'inscription
    // Appelé depuis FirebaseAuthServiceImpl après createUserWithEmailAndPassword
    public void createUser(User user) {
        dataService.create(COLLECTION, user);
    }

    // Met à jour les infos modifiables du profil (prénom, nom, photo)
    public void updateUser(String uid, User user) {
        dataService.update(COLLECTION, uid, user);
    }

    // Supprime le document profil (à coupler côté Auth avec la suppression du compte)
    public void deleteUser(String uid) {
        dataService.delete(COLLECTION, uid);
    }

    // Récupère tous les utilisateurs — usage admin uniquement
    public void getAllUsers(DataCallback callback) {
        dataService.getAll(COLLECTION, callback);
    }

    // Met à jour uniquement le champ "role" sans écraser tout le document
    public void updateUserRole(String uid, String role, DataCallback callback) {
        Map<String, Object> patch = new HashMap<>();
        patch.put("role", role);
        dataService.update(COLLECTION, uid, patch);
    }
}