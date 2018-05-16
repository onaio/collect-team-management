package io.ona.collect.android.team.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.activities.MessagesActivity;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.receivers.LaunchOdkReceiver;
import io.ona.collect.android.team.receivers.MarkMessageReadReceiver;

/**
 * Created by Jason Rogena - jrogena@ona.io on 5/16/18.
 */

public class MessageNotifications implements PushService.MessageListener {
    private static final String TAG = MessageNotifications.class.getSimpleName();
    private final Context context;
    private final NotificationChannel freeformMessagesChannel;
    private static final String CHANNEL_ID_FREEFORM_MESSAGES = "3243242";
    private static final String ACTION_MARK_READ = "mark_read";
    private static final String ACTION_LAUNCH_ODK = "launch_odk";

    public MessageNotifications() {
        context = TeamManagement.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.free_form_messages_channel_name);
            String description = context.getString(R.string.free_form_messages_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            freeformMessagesChannel = new NotificationChannel(CHANNEL_ID_FREEFORM_MESSAGES, name, importance);
            freeformMessagesChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(freeformMessagesChannel);
        } else {
            freeformMessagesChannel = null;
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        try {
            showNotification(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageSent(Message message) {

    }

    private void showNotification(Message message) throws JSONException {
        Log.d(TAG, "About to show notification " + message.uuid);
        if (message.payload.has("message")
                && message.payload.getJSONObject("context").getString("type").equals("xform")) {
            Intent messageActivityIntent = new Intent(context, MessagesActivity.class);
            messageActivityIntent
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent messageActivityPendingIntent =
                    PendingIntent.getActivity(context, 0, messageActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent launchOdkIntent = new Intent(context, LaunchOdkReceiver.class);
            launchOdkIntent.setAction(ACTION_LAUNCH_ODK);
            launchOdkIntent.putExtra(LaunchOdkReceiver.EXTRA_MESSAGE_UUID, message.uuid);
            PendingIntent launchOdkPendingIntent =
                    PendingIntent.getBroadcast(context, 0, launchOdkIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent markReadIntent = new Intent(context, MarkMessageReadReceiver.class);
            markReadIntent.setAction(ACTION_MARK_READ);
            markReadIntent.putExtra(MarkMessageReadReceiver.EXTRA_MESSAGE_UUID, message.uuid);
            PendingIntent markReadPendingIntent =
                    PendingIntent.getBroadcast(context, 0, markReadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(message.payload.getJSONObject("author")
                            .getString("real_name"))
                    .setContentText(message.payload.getJSONObject("context")
                            .getJSONObject("metadata").getString("name"))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message.payload.getString("message")))
                    .setContentIntent(messageActivityPendingIntent)
                    .addAction(0, context.getResources().getString(R.string.mark_read), markReadPendingIntent)
                    .addAction(0, context.getResources().getString(R.string.launch_odk), launchOdkPendingIntent)
                    .setAutoCancel(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && freeformMessagesChannel != null) {
                builder.setChannelId(freeformMessagesChannel.getId());
            }

            Notification notification = builder.build();

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Message.getNotificationId(message.uuid), notification);
        }
    }
}
