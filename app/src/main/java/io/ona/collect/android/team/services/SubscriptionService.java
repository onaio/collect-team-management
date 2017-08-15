package io.ona.collect.android.team.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 15/08/2017.
 */

public class SubscriptionService extends IntentService {
    private static final String TAG = SubscriptionService.class.getSimpleName();
    public static final String KEY_SUBSCRIPTIONS = "subscriptions";
    public SubscriptionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Serializable serializable = bundle.getSerializable(KEY_SUBSCRIPTIONS);
            if (serializable != null && serializable instanceof ArrayList) {
                ArrayList<Subscription> subscriptions = (ArrayList<Subscription>) serializable;
                for (Subscription curSubscription : subscriptions) {
                    try {
                        curSubscription.connection.pushService.subscribe(curSubscription);
                    } catch (PushService.SubscriptionException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        }
    }

}
