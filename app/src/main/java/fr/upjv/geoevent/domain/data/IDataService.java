package fr.upjv.geoevent.domain.data;

public interface IDataService {

    void create(String collection, Object data);

    void update(String collection, String id, Object data);

    void delete(String collection, String id);

    void getById(String collection, String id, DataCallback callback);

    void getAll(String collection, DataCallback callback);
}
