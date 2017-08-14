package io.ona.collect.android.team.pushes.messages.handlers;

import java.util.ArrayList;

import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.pushes.messages.types.PushMessage;
import io.ona.collect.android.team.pushes.systems.PushSystem;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/08/2017.
 */

public class MessageHandler implements PushSystem.ConnectionListener, PushSystem.MessageListener {
    private ArrayList<PushSystem.ConnectionListener> connectionListeners;
    private ArrayList<PushSystem.MessageListener> messageListeners;

    public MessageHandler() {
        this.connectionListeners = new ArrayList<>();
        this.messageListeners = new ArrayList<>();
    }

    public void addConnectionListener(PushSystem.ConnectionListener connectionListener) {
        if (connectionListener != null && !connectionListeners.contains(connectionListener)) {
            connectionListeners.add(connectionListener);
        }
    }

    public void removeConnectionListener(PushSystem.ConnectionListener connectionListener) {
        if (connectionListener != null && connectionListeners.contains(connectionListener)) {
            connectionListeners.remove(connectionListener);
        }
    }

    public void addMessageListener(PushSystem.MessageListener messageListener) {
        if (messageListener != null && !messageListeners.contains(messageListener)) {
            messageListeners.add(messageListener);
        }
    }

    public void removeMessageListener(PushSystem.MessageListener messageListener) {
        if (messageListener != null && messageListeners.contains(messageListener)) {
            messageListeners.remove(messageListener);
        }
    }

    @Override
    public void onConnected(Connection connection, boolean status) {
        for (PushSystem.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnected(connection, status);
        }
    }

    @Override
    public void onDisconnected() {
        for (PushSystem.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onDisconnected();
        }
    }

    @Override
    public void onConnectionLost(Connection connection, Throwable throwable) {
        for (PushSystem.ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnectionLost(connection, throwable);
        }
    }

    @Override
    public void onMessageReceived(PushMessage message) {
        for (PushSystem.MessageListener messageListener : messageListeners) {
            messageListener.onMessageReceived(message);
        }
    }

    @Override
    public void onMessageSent(PushMessage message) {
        for (PushSystem.MessageListener messageListener : messageListeners) {
            messageListener.onMessageSent(message);
        }
    }
}
