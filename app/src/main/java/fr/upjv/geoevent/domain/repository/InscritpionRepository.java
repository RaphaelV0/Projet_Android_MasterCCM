package fr.upjv.geoevent.domain.repository;

import fr.upjv.geoevent.domain.data.DataCallback;
import fr.upjv.geoevent.domain.data.DataServiceFactory;
import fr.upjv.geoevent.domain.data.IDataService;

public class InscritpionRepository {

    private static final String COLLECTION = "inscriptionevent";
    private IDataService dataService;

    public InscritpionRepository() {
        this.dataService = DataServiceFactory.create();
    }



    public void getInscriptionEvents(DataCallback callback) { dataService.getAll(COLLECTION, callback); }




}
