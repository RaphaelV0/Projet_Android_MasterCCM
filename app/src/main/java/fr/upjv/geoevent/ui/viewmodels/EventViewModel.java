package fr.upjv.geoevent.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import fr.upjv.geoevent.domain.models.Evenement;

/**
 * ViewModel partagé entre le MapFragment et le ListFragment.
 * Centralise et expose deux données réactives via LiveData :
 * la liste complète des événements et le rayon de filtrage en kilomètres.
 * Grâce à l'architecture ViewModel, ces données survivent aux rotations d'écran
 * et sont accessibles depuis plusieurs fragments sans couplage direct entre eux.
 */
public class EventViewModel extends ViewModel {

    /**
     * Rayon de recherche en kilomètres. Valeur par défaut : 10 km.
     * Mis à jour par le MapFragment lorsque l'utilisateur déplace le curseur de rayon.
     */
    private final MutableLiveData<Integer> radiusKm = new MutableLiveData<>(10);

    /**
     * Liste complète des événements chargés depuis Firestore.
     * Mise à jour par le composant qui récupère les données (ex : MapFragment ou repository).
     */
    private final MutableLiveData<List<Evenement>> allEvents = new MutableLiveData<>();

    /**
     * Met à jour le rayon de filtrage.
     * Déclenche automatiquement les observers (notamment dans ListFragment).
     *
     * @param radius Le nouveau rayon en kilomètres.
     */
    public void setRadius(int radius) {
        radiusKm.setValue(radius);
    }

    /**
     * Retourne le LiveData exposant le rayon de filtrage courant.
     * Les fragments peuvent l'observer pour réagir à tout changement.
     *
     * @return LiveData contenant le rayon en kilomètres.
     */
    public LiveData<Integer> getRadius() {
        return radiusKm;
    }

    /**
     * Met à jour la liste des événements disponibles.
     * Déclenche automatiquement les observers (notamment dans ListFragment).
     *
     * @param events La nouvelle liste d'événements à exposer.
     */
    public void setEvents(List<Evenement> events) {
        allEvents.setValue(events);
    }

    /**
     * Retourne le LiveData exposant la liste complète des événements.
     * Les fragments peuvent l'observer pour afficher ou filtrer les données.
     *
     * @return LiveData contenant la liste des événements.
     */
    public LiveData<List<Evenement>> getEvents() {
        return allEvents;
    }
}