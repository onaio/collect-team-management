package io.ona.collect.android.team.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.MessageTable;

/**
 * Created by Jason Rogena - jrogena@ona.io on 5/16/18.
 */

public class MarkMessageReadReceiver extends BroadcastReceiver {
    private static final String TAG = MarkMessageReadReceiver.class.getSimpleName();
    public static final String EXTRA_MESSAGE_UUID = "message_uuid";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String messageUuid = bundle.getString(EXTRA_MESSAGE_UUID);

            if (messageUuid != null) {
                MessageTable mt = (MessageTable) TeamManagement.getInstance()
                        .getTeamManagementDatabase().getTable(MessageTable.TABLE_NAME);
                mt.markAsRead(new String[]{messageUuid});

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Message.getNotificationId(messageUuid));
            } else {
                Log.w(TAG, "Message UUID is null");
            }
        } else {
            Log.w(TAG, "Bundle is null");
        }
    }
}
