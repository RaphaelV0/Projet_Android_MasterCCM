package fr.upjv.geoevent.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BatteryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(
                context,
                "Batterie faible : certaines fonctionnalités peuvent être limitées",
                Toast.LENGTH_LONG
        ).show();
    }
}