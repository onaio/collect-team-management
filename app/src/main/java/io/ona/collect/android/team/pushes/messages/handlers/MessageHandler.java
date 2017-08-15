package io.ona.collect.android.team.pushes.messages.handlers;

import java.util.ArrayList;

import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.pushes.messages.types.PushMessage;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/08/2017.
 */

public class MessageHandler implements PushService.ConnectionListener, PushService.MessageListener {
    private ArrayList<PushService.ConnectionListener> connectionListeners;
    private ArrayList<PushService.MessageListener> messageListeners;

    public MessageHandler() {
        this.connectionListeners = new ArrayList<>();
        this.messageListeners = new ArrayList<>();
    }

    public void addConnectionListener(PushService.ConnectionListener connectionListener) {
        if (connectionListener != null && !connectionListeners.contains(connectionListener)) {
            connectionListeners.add(connectionListener);
        }
    }

    public void removeConnectionListener(PushService.ConnectionListener connectionListener) {
        if (connectionListener != null && connectionListeners.contains(connectionListener)) {
            connectionListeners.remove(connectionListener);
        }
    }

    public void addMessageListener(PushService.MessageListener messageListener) {
        if (messageListener != null && !messageListeners.contains(messageListener)) {
            messageListeners.add(messageListener);
        }
    }

    public void removeMessageListener(PushService.MessageListener messageListener) {
        if (messageListener != null && messageListeners.contains(messageListener)) {
            messageListeners.remove(messageListener);
        }
    }

    @Override
    public void onConnected(Connection connection, boolean status) {
        for (PushService.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnected(connection, status);
        }
    }

    @Override
    public void onDisconnected() {
        for (PushService.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onDisconnected();
        }
    }

    @Override
    public void onConnectionLost(Connection connection, Throwable throwable) {
        for (PushService.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnectionLost(connection, throwable);
        }
    }

    @Override
    public void onMessageReceived(PushMessage message) {
        for (PushService.MessageListener messageListener : messageListeners) {
            messageListener.onMessageReceived(message);
        }
    }

    @Override
    public void onMessageSent(PushMessage message) {
        for (PushService.MessageListener messageListener : messageListeners) {
            messageListener.onMessageSent(message);
        }
    }
}
