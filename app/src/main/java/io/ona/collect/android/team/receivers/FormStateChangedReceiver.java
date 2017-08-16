package io.ona.collect.android.team.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.carriers.OdkForm;
import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.ConnectionTable;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.services.ConnectionService;
import io.ona.collect.android.team.services.SubscriptionService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 03/08/2017.
 */

public class FormStateChangedReceiver extends BroadcastReceiver {
    private static final String TAG = FormStateChangedReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        if (intent != null && intent.getExtras() != null) {
            Log.d(TAG, "Intent looks ok");
            Bundle bundle = intent.getExtras();
            for (String curKey : bundle.keySet()) {
                Log.d(TAG, "Intent has key " + curKey + " with value " + bundle.getString(curKey));
            }

            try {
                OdkForm form = OdkForm.getFromBundle(intent.getExtras());
                ArrayList<Subscription> subscriptions = new ArrayList<>();
                ConnectionTable ct = (ConnectionTable) TeamManagement.getInstance()
                        .getTeamManagementDatabase().getTable(ConnectionTable.TABLE_NAME);
                for (PushService curService : TeamManagement.getInstance().getActivePushServices()) {
                    try {
                        List<Connection> connections = ct.getAllActiveConnections(curService);
                        if (connections.size() == 1) {
                            subscriptions.addAll(Subscription.getSubscriptionsFromOdkForm(
                                    connections.get(0),
                                    form));
                        } else if (connections.size() > 1) {
                            Log.e(TAG, "More than one active connection found for service "
                                    + curService.getName());
                        }
                    } catch (PushService.PushSystemNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if (subscriptions != null && subscriptions.size() > 0) {
                    Intent serviceIntent = new Intent(context, SubscriptionService.class);
                    serviceIntent.putExtra(SubscriptionService.KEY_SUBSCRIPTIONS, subscriptions);
                }
            } catch (OdkForm.MalformedObjectException e) {
                e.printStackTrace();
            }
        }
    }
}