package io.ona.collect.android.team.pushes.messages.types;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Jason Rogena - jrogena@ona.io on 10/08/2017.
 */

public class PushMessage {
    private static final String TAG = PushMessage.class.getSimpleName();
    public static final String KEY_MESSAGE_ID = "message_id";
    public final String payload;
    public final String topic;
    public final String systemMessageId;
    public final String globalMessageId;
    public final Date timeReceived;

    public PushMessage(String topic, String payload, String systemMessageId) {
        this.payload = payload;
        this.topic = topic;
        this.systemMessageId = systemMessageId;
        this.globalMessageId = getGlobalMessageId(payload);
        this.timeReceived = new Date();
    }

    public PushMessage(PushMessage message) {
        this.payload = message.payload;
        this.topic = message.topic;
        this.timeReceived = message.timeReceived;
        this.systemMessageId = message.systemMessageId;
        this.globalMessageId = message.globalMessageId;
    }

    private String getGlobalMessageId(String payload) {
        try {
            JSONObject payloadObject = new JSONObject(payload);
            return payloadObject.getString(PushMessage.KEY_MESSAGE_ID);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }
}
