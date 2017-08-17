package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.persistence.sqlite.databases.DbWrapper;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 17/08/2017.
 */

public class MessageTable extends Table {
    public static final String TABLE_NAME = "message";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_SUBSCRIPTION_ID = "subscription_id";
    private static final String COLUMN_UUID = "uuid";
    private static final String COLUMN_PAYLOAD = "payload";
    private static final String COLUMN_SENT_BY_APP = "sent_by_app";
    private static final String COLUMN_READ = "read";
    private static final String COLUMN_SENT_AT = "sent_at";
    private static final String COLUMN_RECEIVED_AT = "received_at";
    private static final int SENT_BY_APP = 1;
    private static final int NOT_SENT_BY_APP = 0;
    private static final int READ = 1;
    private static final int NOT_READ = 0;
    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_SUBSCRIPTION_ID + " INTEGER NOT NULL," +
            COLUMN_UUID + " VARCHAR NOT NULL," +
            COLUMN_PAYLOAD + " VARCHAR NOT NULL," +
            COLUMN_SENT_BY_APP + " INTEGER NOT NULL," +
            COLUMN_READ + " INTEGER NOT NULL," +
            COLUMN_SENT_AT + " TIMESTAMP NULL DEFAULT NULL," +
            COLUMN_RECEIVED_AT + " TIMESTAMP NULL DEFAULT NULL" +
            ")";
    private static final String INDEX_UUID = "CREATE UNIQUE INDEX " +
            TABLE_NAME + "_" + COLUMN_UUID + "_index" +
            " ON " + TABLE_NAME +
            "(" + COLUMN_UUID + ");";

    public MessageTable(DbWrapper dbWrapper) {
        super(dbWrapper, TABLE_NAME);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
        db.execSQL(INDEX_UUID);
    }

    /**
     * Attempts to record a push system message to the database.
     *
     * @param message The message to be added to the database
     * @return {@code true} if subscription was successfully added
     * who's statuses were changed to {@code STATUS_INACTIVE})
     */
    public void addMessage(Message message)
            throws SQLiteException, PushService.PushSystemNotFoundException {
        SQLiteDatabase db = dbWrapper.getWritableDatabase();
        try {
            db.beginTransaction();
            long subscriptionId = message.subscription.id;
            if (subscriptionId == Subscription.DEFAULT_ID) {
                SubscriptionTable st =
                        (SubscriptionTable) dbWrapper.getTable(SubscriptionTable.TABLE_NAME);
                Subscription subscription = message.subscription;
                Subscription tmpSubscription = st.getSubscriptionWithDetails(
                        subscription.connection,
                        subscription.topic);
                subscriptionId = tmpSubscription.id;
            }

            if (subscriptionId == Subscription.DEFAULT_ID) {
                throw new SQLiteException("Cannot associate with a subscription without and id");
            }

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_SUBSCRIPTION_ID, subscriptionId);
            cv.put(COLUMN_UUID, message.uuid);
            cv.put(COLUMN_PAYLOAD, message.payload.toString());
            cv.put(COLUMN_SENT_BY_APP, message.sentByApp ? SENT_BY_APP : NOT_SENT_BY_APP);
            cv.put(COLUMN_READ, message.read ? READ : NOT_READ);
            cv.put(COLUMN_SENT_AT, message.sentAt.getTime());
            cv.put(COLUMN_RECEIVED_AT, message.receivedAt.getTime());
            db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
}
