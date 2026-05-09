package fr.upjv.geoevent.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import fr.upjv.geoevent.domain.models.Evenement;

public class EventViewModel extends ViewModel {
    private final MutableLiveData<Integer> radiusKm = new MutableLiveData<>(10);
    private final MutableLiveData<List<Evenement>> allEvents = new MutableLiveData<>();

    public void setRadius(int radius) {
        radiusKm.setValue(radius);
    }

    public LiveData<Integer> getRadius() {
        return radiusKm;
    }

    public void setEvents(List<Evenement> events) {
        allEvents.setValue(events);
    }

    public LiveData<List<Evenement>> getEvents() {
        return allEvents;
    }
}