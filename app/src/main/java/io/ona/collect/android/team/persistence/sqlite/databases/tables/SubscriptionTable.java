package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.persistence.sqlite.databases.DbWrapper;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Responsible for handling storage of data on subscriptions in the database
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 15/08/2017.
 */

public class SubscriptionTable extends Table {
    private static final String TAG = SubscriptionTable.class.getSimpleName();
    public static final String TABLE_NAME = "subscription";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONNECTION_ID = "connection_id";
    private static final String COLUMN_TOPIC = "topic";
    private static final String COLUMN_QOS = "qos";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CONNECTION_ID + " INTEGER NOT NULL," +
            COLUMN_TOPIC + " VARCHAR NOT NULL," +
            COLUMN_QOS + " INTEGER NOT NULL," +
            COLUMN_STATUS + " INTEGER NOT NULL," +
            COLUMN_CREATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            COLUMN_UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ")";
    private static final String INDEX_CONNECTION_TOPIC = "CREATE UNIQUE INDEX " +
            TABLE_NAME + "_" + COLUMN_CONNECTION_ID + "_" + COLUMN_TOPIC + "_index" +
            " ON " + TABLE_NAME +
            "(" + COLUMN_CONNECTION_ID + "," + COLUMN_TOPIC + ");";
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;

    public SubscriptionTable(DbWrapper dbWrapper) {
        super(dbWrapper, TABLE_NAME);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
        db.execSQL(INDEX_CONNECTION_TOPIC);
    }

    public List<Subscription> getAllActiveSubscriptions(Connection connection)
            throws SQLiteException {
        List<Subscription> subscriptions = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbWrapper.getReadableDatabase().query(TABLE_NAME, null,
                    COLUMN_CONNECTION_ID + " = ? and " + COLUMN_STATUS + " = ?",
                    new String[]{
                            String.valueOf(connection.id),
                            String.valueOf(STATUS_ACTIVE)
                    },
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Subscription curSubscription = extractSubscription(cursor, connection);
                    if (curSubscription != null) {
                        subscriptions.add(curSubscription);
                    } else {
                        Log.w(TAG, "null connection excluded from getAllActiveConnections fetch");
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return subscriptions;
    }

    /**
     * Attempts to record a push system subscription to the database.
     *
     * @param subscription The subscription to be added to the database
     * @return {@code true} if subscription was successfully added
     * who's statuses were changed to {@code STATUS_INACTIVE})
     */
    public void addSubscription(Subscription subscription)
            throws SQLiteException, PushService.PushSystemNotFoundException {
        SQLiteDatabase db = dbWrapper.getWritableDatabase();
        try {
            db.beginTransaction();
            long connectionId = subscription.connection.id;
            if (connectionId == Connection.DEFAULT_ID) {
                ConnectionTable ct =
                        (ConnectionTable) dbWrapper.getTable(ConnectionTable.TABLE_NAME);
                Connection connection = subscription.connection;
                Connection tmpConnection = ct.getConnectionWithDetails(
                        connection.pushService.getName(),
                        connection.server,
                        connection.port,
                        connection.username,
                        connection.password);
                connectionId = tmpConnection.id;
            }

            if (connectionId == Connection.DEFAULT_ID) {
                throw new SQLiteException("Cannot associate with a connection without and id");
            }

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_CONNECTION_ID, connectionId);
            cv.put(COLUMN_TOPIC, subscription.topic);
            cv.put(COLUMN_QOS, subscription.qos);
            cv.put(COLUMN_STATUS, subscription.active ? STATUS_ACTIVE : STATUS_INACTIVE);
            cv.put(COLUMN_CREATED_AT, subscription.createdAt.getTime());
            cv.put(COLUMN_UPDATED_AT, subscription.updatedAt.getTime());
            long id = db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {// The connection was already in the database
                Subscription tmpSubscription = new Subscription(subscription);
                if (tmpSubscription.id == Subscription.DEFAULT_ID) {
                    tmpSubscription = getSubscriptionWithDetails(
                            subscription.connection,
                            subscription.topic);
                }

                updateStatus(
                        db,
                        tmpSubscription,
                        subscription.active ? STATUS_ACTIVE : STATUS_INACTIVE);
                updateQos(db, tmpSubscription, subscription.qos);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public HashMap<Long, Subscription> getAllSubscriptions()
            throws PushService.PushSystemNotFoundException {
        ConnectionTable ct = (ConnectionTable) TeamManagement.getInstance()
                .getTeamManagementDatabase().getTable(ConnectionTable.TABLE_NAME);
        HashMap<Long, Connection> connections = ct.getAllConnections();

        Cursor cursor = null;
        HashMap<Long, Subscription> subscriptions = new HashMap<>();
        try {
            cursor = dbWrapper.getReadableDatabase().query(
                    TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    long curConnectionId = cursor.getLong(
                            cursor.getColumnIndex(COLUMN_CONNECTION_ID));
                    if (connections.containsKey(curConnectionId)) {
                        Subscription curSubscription =
                                extractSubscription(cursor, connections.get(curConnectionId));
                        subscriptions.put(curSubscription.id, curSubscription);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return subscriptions;
    }

    public Subscription getSubscriptionWithDetails(Connection connection, String topic)
            throws PushService.PushSystemNotFoundException, SQLiteException {
        if (connection.id == Connection.DEFAULT_ID) {
            ConnectionTable ct = (ConnectionTable) TeamManagement.getInstance()
                    .getTeamManagementDatabase().getTable(ConnectionTable.TABLE_NAME);
            connection = ct.getConnectionWithDetails(
                    connection.pushService.getName(),
                    connection.server,
                    connection.port,
                    connection.username,
                    connection.password);
        }

        if (connection.id == Connection.DEFAULT_ID) {
            throw new SQLiteException("Could not determine id for subscription's connection");
        }

        Cursor cursor = null;
        try {
            cursor = dbWrapper.getReadableDatabase().query(
                    TABLE_NAME, null,
                    COLUMN_CONNECTION_ID + " = ? and " +
                            COLUMN_TOPIC + " = ?",
                    new String[]{
                            String.valueOf(connection.id),
                            topic
                    },
                    null, null, null);
            if (cursor != null) {
                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    return extractSubscription(cursor, connection);
                } else if (cursor.getCount() > 1) {
                    throw new SQLiteException("Found more than one subscription with a set of" +
                            " values that should be unique");
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public void updateStatus(Subscription subscription, int status)
            throws PushService.PushSystemNotFoundException, SQLiteException {
        SQLiteDatabase db = dbWrapper.getWritableDatabase();
        try {
            db.beginTransaction();
            Subscription tmpSubscription = new Subscription(subscription);
            if (tmpSubscription.id == Subscription.DEFAULT_ID) {
                tmpSubscription = getSubscriptionWithDetails(tmpSubscription.connection,
                        tmpSubscription.topic);
            }
            updateStatus(db, tmpSubscription, status);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void updateStatus(SQLiteDatabase db, Subscription subscription, int status)
            throws SQLiteException {
        if (subscription == null || subscription.id == Subscription.DEFAULT_ID) {
            throw new SQLiteException("Cannot update status for unknown subscription " +
                    Subscription.DEFAULT_ID);
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STATUS, status);
        cv.put(COLUMN_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
        db.update(TABLE_NAME,
                cv,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(subscription.id)});
    }

    private void updateQos(SQLiteDatabase db, Subscription subscription, int qos)
            throws SQLiteException {
        if (subscription.id == Subscription.DEFAULT_ID) {
            throw new SQLiteException("Cannot update qos for subscription with id as " +
                    Subscription.DEFAULT_ID);
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_QOS, qos);
        cv.put(COLUMN_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
        db.update(TABLE_NAME,
                cv,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(subscription.id)});
    }

    private Subscription extractSubscription(Cursor cursor, Connection connection) {
        return new Subscription(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                connection,
                cursor.getString(cursor.getColumnIndex(COLUMN_TOPIC)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_QOS)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)) == STATUS_ACTIVE,
                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT))),
                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATED_AT))));
    }
}
