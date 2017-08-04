package io.ona.collect.android.team.pushes.systems;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class MqttPushSystem extends PushSystem {
    //private static MqttAndroidClient mqttAndroidClient;

    protected MqttPushSystem(Context context) {
        super(context);
    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }
}
