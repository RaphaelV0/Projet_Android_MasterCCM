package fr.upjv.geoevent.domain.repository;

import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.DataServiceFactory;
import fr.upjv.geoevent.domain.data.IDataService;
import fr.upjv.geoevent.domain.models.Evenement;


public class EvenementRepository {

    private static final String COLLECTION = "events";

    private IDataService dataService;

    public EvenementRepository() {
        this.dataService = DataServiceFactory.create();
    }

    public void getEvents(DataCallback callback) {
        dataService.getAll(COLLECTION, callback);
    }

    public void createEvent(Evenement event) {
        dataService.create(COLLECTION, event);
    }

    public void updateEvent(String id, Evenement event) {
        dataService.update(COLLECTION, id, event);
    }

    public void deleteEvent(String id) {
        dataService.delete(COLLECTION, id);
    }

}