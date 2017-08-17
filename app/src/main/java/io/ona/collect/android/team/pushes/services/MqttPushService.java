package io.ona.collect.android.team.pushes.services;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.carriers.Subscription;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class MqttPushService extends PushService {
    private static final String TAG = MqttPushService.class.getSimpleName();
    public static final String NAME = "MQTT";
    public static final int QOS_AT_MOST_ONCE = 0;
    public static final int QOS_AT_LEAST_ONCE = 1;
    public static final int QOS_EXACTLY_ONCE = 2;
    private static final int CONNECTION_TIMEOUT = 60;
    private static final int KEEP_ALIVE_INTERVAL = 60;
    private static final String CA_KEYSTORE_FILE = "mqtt_ca.bks";
    private static final String CA_KEYSTORE_PASSWORD = "usTjh46qEvHeWmhTVB6CXQjFb";
    private static final String APP_KEYSTORE_FILE = "team.android.collect.ona.io.bks";
    private static final String APP_KEYSTORE_PASSWORD = "bU3mVNhcMHn8RwJBsKEdMBbpN";
    private static MqttAndroidClient mqttAndroidClient;
    private Connection currentConnection;

    public MqttPushService(ConnectionListener connectionListener,
                           MessageListener messageListener) {
        super(NAME, connectionListener, messageListener);
    }

    @Override
    public synchronized boolean connect(final Connection connection) throws ConnectionException {
        super.connect(connection);

        String clientId = getClientId(connection);

        if (mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(TeamManagement.getInstance(),
                    connection.getServerUri(), clientId);
            try {
                Log.d(TAG, "About to try connection");
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(false);
                options.setAutomaticReconnect(true);
                options.setConnectionTimeout(CONNECTION_TIMEOUT);
                options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
                //options.setSocketFactory(getSocketFactory());
                mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                    @Override
                    public void connectComplete(boolean reconnect, String serverURI) {
                        Log.d(TAG, "Was successfully able to connect to MQTT broker");
                        connectionListener.onConnected(connection, true, reconnect);
                    }

                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.w(TAG, "Connection to broker lost");
                        connectionListener.onConnectionLost(connection, cause);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        messageListener.onMessageReceived(extractMessage(topic, message, false));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        try {
                            if (token != null) {
                                String[] topics = token.getTopics();
                                for (int i = 0; i < topics.length; i++) {
                                    try {
                                        messageListener.onMessageSent(
                                                extractMessage(topics[i], token.getMessage(), true));
                                    } catch (JSONException e) {
                                        Log.e(TAG, Log.getStackTraceString(e));
                                    } catch (IllegalArgumentException e) {
                                        Log.e(TAG, Log.getStackTraceString(e));
                                    } catch (UnsupportedOperationException e) {
                                        Log.e(TAG, Log.getStackTraceString(e));
                                    }
                                }
                            }
                        } catch (MqttException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                });

                mqttAndroidClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        exception.printStackTrace();
                        Log.w(TAG, "Unable to connect to the MQTT broker because of " +
                                exception.getMessage());
                        connectionListener.onConnected(connection, false, false);
                    }
                });
                Log.d(TAG, "Waiting for client to finish connecting to broker");
                currentConnection = connection;
                return true;
            } catch (Exception e) {
                disconnect(connection, true);
                throw new ConnectionException(e);
            }
        }

        return false;
    }

    private Message extractMessage(String topic, MqttMessage mqttMessage, boolean sentByApp)
            throws JSONException, IllegalArgumentException, UnsupportedOperationException {
        Subscription subscription =
                new Subscription(currentConnection, topic, Subscription.DEFAULT_QOS, true);
        Message message = new Message(
                subscription, mqttMessage, sentByApp, false, Calendar.getInstance().getTime());
        saveMessage(message);
        return message;
    }

    private SSLSocketFactory getSocketFactory() throws Exception {
        Context context = TeamManagement.getInstance();
        InputStream caInputStream = context.getAssets().open(CA_KEYSTORE_FILE);
        InputStream clientInputStream = context.getAssets().open(APP_KEYSTORE_FILE);
        try {
            //load ca cert
            SSLContext ctx = null;
            SSLSocketFactory sslSockFactory = null;
            KeyStore caKs;
            caKs = KeyStore.getInstance("BKS");
            caKs.load(caInputStream, CA_KEYSTORE_PASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(caKs);

            //load client key and cert
            KeyStore ks;
            ks = KeyStore.getInstance("BKS");
            ks.load(clientInputStream, APP_KEYSTORE_PASSWORD.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, APP_KEYSTORE_PASSWORD.toCharArray());

            ctx = SSLContext.getInstance("TLSv1");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            sslSockFactory = ctx.getSocketFactory();

            return sslSockFactory;
        } catch (KeyStoreException e) {
            throw new MqttSecurityException(e);
        } catch (CertificateException e) {
            throw new MqttSecurityException(e);
        } catch (FileNotFoundException e) {
            throw new MqttSecurityException(e);
        } catch (IOException e) {
            throw new MqttSecurityException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new MqttSecurityException(e);
        } catch (KeyManagementException e) {
            throw new MqttSecurityException(e);
        }
    }

    @Override
    public synchronized boolean disconnect(Connection connection, boolean permanent) throws ConnectionException {
        super.disconnect(connection, permanent);

        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient = null;
                currentConnection = null;

                return true;
            } catch (MqttException e) {
                throw new ConnectionException(e);
            } catch (NullPointerException e) {
                Log.wtf(TAG, "Looks like MQTT client is null");
            }
        }

        return false;
    }

    @Override
    public synchronized boolean subscribe(Subscription subscription) throws SubscriptionException {
        super.subscribe(subscription);

        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.subscribe(subscription.topic, subscription.qos);
                Log.d(TAG, "Subscribed to " + subscription.topic + " with QoS " + subscription.qos);
                return true;
            } catch (MqttException e) {
                throw new SubscriptionException(e);
            } catch (NullPointerException e) {
                Log.wtf(TAG, "Looks like MQTT client is null");
            }
        }

        return false;
    }

    @Override
    public synchronized boolean unsubscribe(Subscription subscription) throws SubscriptionException {
        super.unsubscribe(subscription);

        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.unsubscribe(subscription.topic);
                Log.d(TAG, "Unsubscribed from " + subscription.topic);
                return true;
            } catch (MqttException e) {
                throw new SubscriptionException(e);
            } catch (NullPointerException e) {
                Log.wtf(TAG, "Looks like MQTT client is null");
            }
        }

        return false;
    }
}
