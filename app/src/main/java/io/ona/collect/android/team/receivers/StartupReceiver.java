package io.ona.collect.android.team.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.ona.collect.android.team.services.StartupService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 10/08/2017.
 */

public class StartupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        StartupService.start(context);
    }
}
