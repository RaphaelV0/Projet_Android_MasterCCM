package fr.upjv.geoevent.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {

    private boolean lastState = true; // mémorise l'état précédent

    public interface NetworkListener {
        void onNetworkChanged(boolean isConnected);
    }

    private final NetworkListener listener;

    public NetworkReceiver(NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isConnected = isInternetAvailable(context);

        //  On affiche uniquement si l'état change
        if (isConnected != lastState) {

            if (listener != null) {
                listener.onNetworkChanged(isConnected);
            }

            Toast.makeText(
                    context,
                    isConnected ? "Connexion rétablie" : "Pas de connexion Internet",
                    Toast.LENGTH_SHORT
            ).show();

            lastState = isConnected;
        }
    }

    private boolean isInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}