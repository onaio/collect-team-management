package io.ona.collect.android.team.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Jason Rogena - jrogena@ona.io on 03/08/2017.
 */

public class ServerSettingsChangedReceiver extends BroadcastReceiver {
    private static final String TAG = ServerSettingsChangedReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        if (intent != null && intent.getExtras() != null) {
            Log.d(TAG, "Intent looks ok");
            Bundle bundle = intent.getExtras();
            for (String curKey : bundle.keySet()) {
                Log.d(TAG, "Intent has key " + curKey + " with value " + bundle.getString(curKey));
            }
        }
    }
}
