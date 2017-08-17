package io.ona.collect.android.team.pushes.messages.handlers;

import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.carriers.OdkForm;
import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.SubscriptionTable;
import io.ona.collect.android.team.pushes.messages.types.PushMessage;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.services.SubscriptionService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/08/2017.
 */

public class PushServiceManager implements PushService.ConnectionListener, PushService.MessageListener {
    private static final String TAG = PushServiceManager.class.getSimpleName();
    private ArrayList<PushService.ConnectionListener> connectionListeners;
    private ArrayList<PushService.MessageListener> messageListeners;

    public PushServiceManager() {
        this.connectionListeners = new ArrayList<>();
        this.messageListeners = new ArrayList<>();
    }

    public void addConnectionListener(PushService.ConnectionListener connectionListener) {
        if (connectionListener != null && !connectionListeners.contains(connectionListener)) {
            connectionListeners.add(connectionListener);
        }
    }

    public void removeConnectionListener(PushService.ConnectionListener connectionListener) {
        if (connectionListener != null && connectionListeners.contains(connectionListener)) {
            connectionListeners.remove(connectionListener);
        }
    }

    public void addMessageListener(PushService.MessageListener messageListener) {
        if (messageListener != null && !messageListeners.contains(messageListener)) {
            messageListeners.add(messageListener);
        }
    }

    public void removeMessageListener(PushService.MessageListener messageListener) {
        if (messageListener != null && messageListeners.contains(messageListener)) {
            messageListeners.remove(messageListener);
        }
    }

    @Override
    public void onConnected(Connection connection, boolean status, boolean wasReconnection) {
        Log.d(TAG, "onConnected fired");

        if (status && !wasReconnection) { // Successfully connected for the first time
            Log.d(TAG, "About to start subscribing to topics");
            SubscriptionTable st = (SubscriptionTable) TeamManagement.getInstance()
                    .getTeamManagementDatabase().getTable(SubscriptionTable.TABLE_NAME);
            ArrayList<Subscription> subscriptions =
                    (ArrayList<Subscription>) st.getAllActiveSubscriptions(connection);

            try {
                List<OdkForm> odkForms = TeamManagement.getInstance()
                        .getOdkFormsContentResolver().getAllDownloadedForms();
                for (OdkForm curForm : odkForms) {
                    Log.d(TAG, "Current odk form is " + curForm.getDisplayName());
                    subscriptions.addAll(Subscription
                            .getSubscriptionsFromOdkForm(connection, curForm));
                }
            } catch (PushService.PushSystemNotFoundException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } catch (OdkForm.MalformedObjectException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            if (subscriptions != null && subscriptions.size() > 0) {
                Intent serviceIntent = new Intent(TeamManagement.getInstance(),
                        SubscriptionService.class);
                serviceIntent.putExtra(SubscriptionService.KEY_SUBSCRIPTIONS, subscriptions);
                TeamManagement.getInstance().startService(serviceIntent);
            }
        }

        for (PushService.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnected(connection, status, wasReconnection);
        }
    }

    @Override
    public void onDisconnected() {
        for (PushService.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onDisconnected();
        }
    }

    @Override
    public void onConnectionLost(Connection connection, Throwable throwable) {
        for (PushService.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnectionLost(connection, throwable);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        Log.d(TAG, "Message received");
        for (PushService.MessageListener messageListener : messageListeners) {
            messageListener.onMessageReceived(message);
        }
    }

    @Override
    public void onMessageSent(Message message) {
        Log.d(TAG, "Message sent");
        for (PushService.MessageListener messageListener : messageListeners) {
            messageListener.onMessageSent(message);
        }
    }
}
