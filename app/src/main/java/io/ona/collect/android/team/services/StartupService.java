package io.ona.collect.android.team.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.preferences.OdkSharedPreferences;
import io.ona.collect.android.team.pushes.systems.PushSystem;

/**
 * Created by Jason Rogena - jrogena@ona.io on 10/08/2017.
 */

public class StartupService extends IntentService {
    private static final String TAG = StartupService.class.getSimpleName();

    public StartupService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    private void startSystems() {
    }

    private void startSystem(PushSystem system) {
        // 1. Check if there's a stored active connection in the database
        List<Connection> activeConnections = TeamManagement.getInstance()
                .getTeamManagementDatabase().getConnectionTable().getAllActiveConnections(system);

        switch (activeConnections.size()) {
            case 0:
                try {
                    createConnectionFromOdkSharedPreference();
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                break;
            case 1:
                break;
            default:
                break;
        }
    }

    private void createConnectionFromOdkSharedPreference()
            throws PackageManager.NameNotFoundException {
        SharedPreferences preferences = OdkSharedPreferences.getOdkPreferences();
        String protocol = preferences.getString(OdkSharedPreferences.KEY_PROTOCOL, null);
        if (protocol != null) {
            if (protocol.equals(OdkSharedPreferences.PROTOCOL_ODK)
                    || protocol.equals(OdkSharedPreferences.PROTOCOL_OTHER)) {
                Intent serviceIntent = new Intent(this, ConnectionService.class);
                String[] keys = new String[]{
                        OdkSharedPreferences.KEY_PROTOCOL,
                        OdkSharedPreferences.KEY_USERNAME,
                        OdkSharedPreferences.KEY_PASSWORD,
                        OdkSharedPreferences.KEY_SERVER_URL,
                        OdkSharedPreferences.KEY_FORMLIST_URL,
                        OdkSharedPreferences.KEY_SUBMISSION_URL
                };
                for (int i = 0; i < keys.length; i++) {
                    serviceIntent.putExtra(keys[i], preferences.getString(keys[i], null));
                }
                startService(serviceIntent);
            }
        }
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, StartupService.class);
        context.startService(intent);
    }
}
