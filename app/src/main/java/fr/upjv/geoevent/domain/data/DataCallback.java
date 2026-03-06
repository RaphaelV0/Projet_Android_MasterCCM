package fr.upjv.geoevent.domain.data;

public interface DataCallback {
    void onSuccess(Object data);
    void onError(Exception e);
}
