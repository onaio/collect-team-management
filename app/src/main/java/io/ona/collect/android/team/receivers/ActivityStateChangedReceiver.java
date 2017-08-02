package io.ona.collect.android.team.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import io.ona.collect.android.team.services.MessageOverlayService;


/**
 * Created by Jason Rogena - jrogena@ona.io on 02/08/2017.
 */

public class ActivityStateChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            Intent startServiceIntent = new Intent(context, MessageOverlayService.class);
            startServiceIntent.putExtras(intent.getExtras());
            context.startService(startServiceIntent);
        }
    }
}