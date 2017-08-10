package io.ona.collect.android.team.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

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

    public static void start(Context context) {
        Intent intent = new Intent(context, StartupService.class);
        context.startService(intent);
    }
}
