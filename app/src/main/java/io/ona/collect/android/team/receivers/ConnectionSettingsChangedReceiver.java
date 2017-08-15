package io.ona.collect.android.team.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.services.ConnectionService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 03/08/2017.
 */

public class ConnectionSettingsChangedReceiver extends BroadcastReceiver {
    private static final String TAG = ConnectionSettingsChangedReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        if (intent != null && intent.getExtras() != null) {
            Log.d(TAG, "Intent looks ok");
            Bundle bundle = intent.getExtras();
            for (String curKey : bundle.keySet()) {
                Log.d(TAG, "Intent has key " + curKey + " with value " + bundle.getString(curKey));
            }
            for (PushService curService : TeamManagement.getInstance().getActivePushServices()) {
                try {
                    Connection connection = Connection.createFromSharedPreference(curService, true);
                    Intent serviceIntent = new Intent(context, ConnectionService.class);
                    serviceIntent.putExtra(ConnectionService.KEY_CONNECTION, connection);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (ConnectionService.ProtocolNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
