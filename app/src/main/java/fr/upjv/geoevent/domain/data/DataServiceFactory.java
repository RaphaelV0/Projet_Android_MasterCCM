package fr.upjv.geoevent.domain.data;

import fr.upjv.geoevent.domain.data.firebase.FirebaseDataService;

public class DataServiceFactory  {

    public static IDataService create() {
        return new FirebaseDataService();
    }

}
