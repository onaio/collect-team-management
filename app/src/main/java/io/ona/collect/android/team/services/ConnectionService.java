package io.ona.collect.android.team.services;

import android.app.IntentService;
import android.content.Intent;

import io.ona.collect.android.team.persistence.carriers.Connection;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/08/2017.
 */

public class ConnectionService extends IntentService {
    private static final String TAG = ConnectionService.class.getSimpleName();
    public ConnectionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
