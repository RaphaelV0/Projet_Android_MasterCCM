package fr.upjv.geoevent.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import fr.upjv.geoevent.domain.models.Evenement;

/**
 * ViewModel d'architecture Jetpack. Centralise et préserve l'état de l'application en mémoire cache.
 * Il survit aux destructions/recréations d'Activités provoquées par les changements de configuration (ex: rotation).
 * Il applique le principe d'encapsulation en séparant la modification interne (Mutable) de l'exposition externe (Read-Only).
 */
public class EventViewModel extends ViewModel {

    // MutableLiveData privé : modifiable uniquement à l'intérieur de cette classe via setValue()
    private final MutableLiveData<Integer> radiusKm = new MutableLiveData<>(10);
    private final MutableLiveData<List<Evenement>> allEvents = new MutableLiveData<>();

    /**
     * Met à jour la valeur numérique du rayon de recherche.
     * Déclenche automatiquement les observateurs branchés sur les fragments.
     */
    public void setRadius(int radius) {
        radiusKm.setValue(radius);
    }

    /**
     * Expose le rayon sous la forme d'un LiveData immuable (Read-Only) pour l'UI.
     * Empêche les fragments d'altérer directement la donnée sans passer par le setter (Encapsulation).
     */
    public LiveData<Integer> getRadius() {
        return radiusKm;
    }

    /** Met à jour la collection d'événements issue du flux temps réel Firestore. */
    public void setEvents(List<Evenement> events) {
        allEvents.setValue(events);
    }

    /** Expose la collection d'événements sous forme immuable pour les fragments Observateurs. */
    public LiveData<List<Evenement>> getEvents() {
        return allEvents;
    }
}