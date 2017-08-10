package io.ona.collect.android.team.pushes.systems;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.pushes.messages.types.PushMessage;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public abstract class PushSystem {
    private static final String TAG = PushSystem.class.getSimpleName();
    private static final String ANDROID6_FAKE_MAC = "02:00:00:00:00:00";
    protected final Context context;
    protected final String name;
    protected final ConnectionListener connectionListener;
    protected final MessageListener messageListener;

    protected PushSystem(Context context, String name,
                         ConnectionListener connectionListener, MessageListener messageListener) {
        this.context = context.getApplicationContext();
        this.name = name;
        this.connectionListener = connectionListener;
        this.messageListener = messageListener;
    }

    public String getName() {
        return name;
    }

    protected String getClientId(Connection connection) {
        String deviceId = getDeviceId();
        return connection.username + "_" + deviceId;
    }

    /**
     * This method uses the same logic to determine the device Id used by ODK Collect
     *
     * @return The device id
     */
    private String getDeviceId() {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        String orDeviceId = null;
        if (deviceId != null) {
            if ((deviceId.contains("*") || deviceId.contains("000000000000000"))) {
                deviceId =
                        Settings.Secure.getString(context.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                orDeviceId = deviceId;
            } else {
                orDeviceId = deviceId;
            }
        }

        if (deviceId == null) {
            // no SIM -- WiFi only
            // Retrieve WiFiManager
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            // Get WiFi status
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null && !ANDROID6_FAKE_MAC.equals(info.getMacAddress())) {
                deviceId = info.getMacAddress();
                orDeviceId = deviceId;
            }
        }

        // if it is still null, use ANDROID_ID
        if (deviceId == null) {
            deviceId =
                    Settings.Secure
                            .getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            orDeviceId = deviceId;
        }

        return orDeviceId;
    }

    public abstract boolean connect(Connection connection);

    public abstract boolean disconnect();

    public interface ConnectionListener {
        void onConnected(Connection connection, boolean status);

        void onDisconnected();

        void onConnectionLost(Connection connection, Throwable throwable);
    }

    public interface MessageListener {
        void onMessageReceived(PushMessage message);

        void onMessageSent(PushMessage message);
    }
}
