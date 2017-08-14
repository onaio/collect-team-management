package io.ona.collect.android.team.pushes.systems;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.pushes.messages.types.PushMessage;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class MqttPushSystem extends PushSystem {
    private static final String TAG = MqttPushSystem.class.getSimpleName();
    private static final String NAME = "MQTT";
    private static final int CONNECTION_TIMEOUT = 60;
    private static final int KEEP_ALIVE_INTERVAL = 60;
    private static MqttAndroidClient mqttAndroidClient;
    private static final String CA_KEYSTORE_FILE = "mqtt_ca.bks";
    private static final String CA_KEYSTORE_PASSWORD = "usTjh46qEvHeWmhTVB6CXQjFb";
    private static final String APP_KEYSTORE_FILE = "team.android.collect.ona.io.bks";
    private static final String APP_KEYSTORE_PASSWORD = "bU3mVNhcMHn8RwJBsKEdMBbpN";

    public MqttPushSystem(Context context, ConnectionListener connectionListener,
                             MessageListener messageListener) {
        super(context, NAME, connectionListener, messageListener);
    }

    @Override
    public boolean connect(final Connection connection) {
        String clientId = getClientId(connection);
        if (mqttAndroidClient != null
                && (!mqttAndroidClient.isConnected()
                || !clientId.equals(mqttAndroidClient.getClientId())
                || !connection.getServerUri().equals(mqttAndroidClient.getServerURI()))) {
            disconnect();
        }

        if (mqttAndroidClient == null) {
            mqttAndroidClient = new MqttAndroidClient(context, connection.getServerUri(), clientId);
            try {
                Log.d(TAG, "About to try connection");
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(false);
                options.setAutomaticReconnect(true);
                options.setConnectionTimeout(CONNECTION_TIMEOUT);
                options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
                options.setSocketFactory(getSocketFactory());

                final IMqttToken token = mqttAndroidClient.connect(options);
                Log.d(TAG, "Post connect code");
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        connectionListener.onConnected(connection, true);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        exception.printStackTrace();
                        Log.w(TAG, "Unable to connect to the MQTT broker because of " + exception.getMessage());
                        connectionListener.onConnected(connection, false);
                    }
                });

                mqttAndroidClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.w(TAG, "Connection to broker lost");
                        connectionListener.onConnectionLost(connection, cause);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        messageListener.onMessageReceived(getPushMessage(topic, message));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        try {
                            if (token != null) {
                                String[] topics = token.getTopics();
                                for (int i = 0; i < topics.length; i++) {
                                    messageListener.onMessageSent(
                                            getPushMessage(topics[i], token.getMessage()));
                                }
                            }
                        } catch (MqttException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                });

                return true;
            } catch (Exception e) {
                disconnect();
                e.printStackTrace();
            }
        }

        return false;
    }

    private PushMessage getPushMessage(String topic, MqttMessage message) {
        String payload = null;
        String systemMessageId = null;
        if (message != null) {
            payload = new String(message.getPayload());
            systemMessageId = String.valueOf(message.getId());
        }

        return new PushMessage(topic, payload, systemMessageId);
    }

    private SSLSocketFactory getSocketFactory() throws Exception {
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
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
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
    public boolean disconnect() {
        return false;
    }
}
