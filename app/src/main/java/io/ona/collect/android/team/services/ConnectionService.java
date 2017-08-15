package io.ona.collect.android.team.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.persistence.preferences.OdkPreferences;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.SubscriptionTable;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Service handles creating, renewing connections to push notifications services
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 11/08/2017.
 */

public class ConnectionService extends IntentService {
    private static final String TAG = ConnectionService.class.getSimpleName();
    public static final String KEY_CONNECTION = "connection";
    public static final ArrayList<String> SUPPORTED_ODK_CONNECTION_PROTOCOLS;

    static {
        SUPPORTED_ODK_CONNECTION_PROTOCOLS = new ArrayList<>();
        SUPPORTED_ODK_CONNECTION_PROTOCOLS.add(OdkPreferences.PROTOCOL_ODK);
        SUPPORTED_ODK_CONNECTION_PROTOCOLS.add(OdkPreferences.PROTOCOL_OTHER);
    }

    public ConnectionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getSerializable(KEY_CONNECTION) != null) {
            createConnectionFromObject((Connection) bundle.getSerializable(KEY_CONNECTION));
        }
    }

    /**
     * Creates a connection to a push notification system using the provided connection object
     *
     * @param connection The connection object to use to attempt to create a connection
     */
    private void createConnectionFromObject(Connection connection) {
        if (connection != null && connection.pushService != null) {
            try {
                connection.pushService.connect(connection);
                SubscriptionTable st = (SubscriptionTable) TeamManagement.getInstance()
                        .getTeamManagementDatabase().getTable(SubscriptionTable.TABLE_NAME);
                List<Subscription> subscriptions = st.getAllActiveSubscriptions(connection);
                for (Subscription curSubscription : subscriptions) {
                    curSubscription.connection.pushService.subscribe(curSubscription);
                }
            } catch (PushService.ConnectionException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } catch (PushService.SubscriptionException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    public static class ProtocolNotSupportedException extends Exception {
        public ProtocolNotSupportedException() {
            super();
        }

        public ProtocolNotSupportedException(String message) {
            super(message);
        }

        public ProtocolNotSupportedException(String message, Throwable cause) {
            super(message, cause);
        }

        public ProtocolNotSupportedException(Throwable cause) {
            super(cause);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        protected ProtocolNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
