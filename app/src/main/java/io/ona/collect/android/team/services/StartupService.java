package io.ona.collect.android.team.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.broadcasts.OdkConnSettingsRequestBroadcast;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.sqlite.databases.DbWrapper;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.ConnectionTable;
import io.ona.collect.android.team.pushes.services.PushService;

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
        Log.d(TAG, "service started");
        startSystems();
    }

    private void startSystems() {
        for (PushService curSystem : TeamManagement.getInstance().getActivePushServices()) {
            startSystem(curSystem);
        }
    }

    /**
     * Starts connections to the provided push notification system
     *
     * @param system    The system to create connections to
     */
    private void startSystem(PushService system) {
        // Check if there's a stored active connection in the database
        try {
            ConnectionTable ct = (ConnectionTable) TeamManagement.getInstance()
                    .getTeamManagementDatabase().getTable(ConnectionTable.TABLE_NAME);
            List<Connection> activeConnections = ct.getAllActiveConnections(system);
            switch (activeConnections.size()) {
                case 0:
                    // No connections yet, request Collect to send current connection settings
                    OdkConnSettingsRequestBroadcast.sendBroadcast(this);
                    break;
                case 1:
                    startConnection(activeConnections.get(0));
                    break;
                default:
                    Log.e(TAG, "More than one active connection to "
                            + system.getName() + " found in the database. Not creating any");
                    break;
            }
        } catch (PushService.PushSystemNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (DbWrapper.TableNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void startConnection(Connection connection) {
        Intent serviceIntent = new Intent(this, ConnectionService.class);
        serviceIntent.putExtra(ConnectionService.KEY_CONNECTION, connection);
        startService(serviceIntent);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, StartupService.class);
        context.startService(intent);
    }
}
