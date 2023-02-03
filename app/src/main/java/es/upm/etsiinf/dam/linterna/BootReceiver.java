package es.upm.etsiinf.dam.linterna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("Startuptest","onReceive");
            Intent serviceIntent = new Intent(context, TorchService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}

