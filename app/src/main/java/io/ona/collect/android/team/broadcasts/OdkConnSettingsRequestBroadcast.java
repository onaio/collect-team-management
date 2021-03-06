package io.ona.collect.android.team.broadcasts;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.ona.collect.android.team.BuildConfig;

/**
 * Responsible for sending a broadcast to ODK Collect requesting for connection settings
 * Created by Jason Rogena - jrogena@ona.io on 16/08/2017.
 */

public class OdkConnSettingsRequestBroadcast {
    private static final String ACTION_REQUEST = BuildConfig.ODK_COLLECT_PACKAGE_NAME + ".CONNECTION_SETTINGS_REQUEST";

    public static void sendBroadcast(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_REQUEST);
        context.sendBroadcast(broadcastIntent);
    }
}
