package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.sqlite.databases.TeamManagementDbWrapper;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Stores info on connections to push notification services
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class ConnectionTable extends Table {
    private static final String TAG = ConnectionTable.class.getSimpleName();
    public static final String TABLE_NAME = "connection";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_SYSTEM = "system";
    private static final String COLUMN_SERVER = "server";
    private static final String COLUMN_PORT = "port";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_INACTIVE = 0;
    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_SYSTEM + " VARCHAR NOT NULL," +
            COLUMN_SERVER + " VARCHAR NOT NULL," +
            COLUMN_PORT + " INTEGER NOT NULL," +
            COLUMN_USERNAME + " VARCHAR NOT NULL," +
            COLUMN_PASSWORD + " VARCHAR NULL," +
            COLUMN_STATUS + " INTEGER NOT NULL DEFAULT " + String.valueOf(STATUS_ACTIVE) + "," +
            COLUMN_CREATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            COLUMN_UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ")";
    private static final String INDEX_SYSTEM_SERVER = "CREATE UNIQUE INDEX " +
            TABLE_NAME + "_" + COLUMN_SYSTEM + "_" + COLUMN_SERVER + "_index" +
            " ON " + TABLE_NAME +
            "("
            + COLUMN_SYSTEM + ","
            + COLUMN_SERVER + ","
            + COLUMN_PORT + ","
            + COLUMN_USERNAME + ","
            + COLUMN_PASSWORD
            + ");";

    public ConnectionTable(TeamManagementDbWrapper dbWrapper) {
        super(dbWrapper, TABLE_NAME);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
        db.execSQL(INDEX_SYSTEM_SERVER);
    }

    /**
     * Attempts to record a push system connection to the database.
     *
     * @param connection The connection to be added to the database
     * @return A {@link List} of affected connections (including those that was created and those
     * who's statuses were changed to {@code STATUS_INACTIVE})
     */
    public List<Connection> addConnection(Connection connection)
            throws SQLiteException, PushService.PushSystemNotFoundException {
        SQLiteDatabase db = dbWrapper.getWritableDatabase();
        List<Connection> affectedConnections = new ArrayList<>();
        affectedConnections.add(connection);
        try {
            db.beginTransaction();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_SYSTEM, connection.pushService.getName());
            cv.put(COLUMN_SERVER, connection.server);
            cv.put(COLUMN_PORT, connection.port);
            cv.put(COLUMN_USERNAME, connection.username);
            cv.put(COLUMN_PASSWORD, connection.password);
            cv.put(COLUMN_STATUS, connection.active ? STATUS_ACTIVE : STATUS_INACTIVE);
            cv.put(COLUMN_CREATED_AT, connection.createdAt.getTime());
            cv.put(COLUMN_UPDATED_AT, connection.updatedAt.getTime());
            long id = db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
            if (id == -1) {// The connection was already in the database
                updateStatus(
                        db,
                        connection.id != Connection.DEFAULT_ID ?
                                connection :
                                getConnectionWithDetails(
                                        connection.pushService.getName(),
                                        connection.server, connection.port,
                                        connection.username,
                                        connection.password),
                        connection.active ? STATUS_ACTIVE : STATUS_INACTIVE);
            }
            if (connection.active) {
                affectedConnections.addAll(deactivateAllOtherConnections(db, connection));
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return affectedConnections;
    }

    public Connection getConnectionWithDetails(String system, String server,
                                                int port, String username, String password)
            throws SQLiteException, PushService.PushSystemNotFoundException {
        Cursor cursor = null;
        try {
            cursor = dbWrapper.getReadableDatabase().query(
                    TABLE_NAME, null,
                    COLUMN_SYSTEM + " = ? and " +
                            COLUMN_SERVER + " = ? and " +
                            COLUMN_PORT + " = ? and " +
                            COLUMN_USERNAME + " = ? and " +
                            COLUMN_PASSWORD + " = ?",
                    new String[]{
                            system,
                            server,
                            String.valueOf(port),
                            username,
                            password
                    },
                    null, null, null);
            if (cursor != null) {
                if (cursor.getCount() == 1) {
                    cursor.moveToFirst();
                    return extractConnection(cursor);
                } else if (cursor.getCount() > 1) {
                    throw new SQLiteException("Found more than one connection with a set of" +
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

    /**
     * Deactivates all other connections sharing a push system with the one provided.
     *
     * @param connection
     * @return {@code true} if able to deactivate all other connections
     */
    private List<Connection> deactivateAllOtherConnections(SQLiteDatabase db, Connection connection)
            throws SQLiteException, PushService.PushSystemNotFoundException {
        List<Connection> activeConnections = getAllActiveConnections(connection.pushService);
        activeConnections.remove(connection);
        updateStatus(db, activeConnections, STATUS_INACTIVE);

        return activeConnections;
    }

    public void updateStatus(Connection connection, int status) throws SQLiteException {
        SQLiteDatabase db = dbWrapper.getWritableDatabase();
        try {
            db.beginTransaction();
            updateStatus(db, connection, status);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void updateStatus(SQLiteDatabase db, Connection connection, int status)
            throws SQLiteException {
        List<Connection> connections = new ArrayList<>();
        connections.add(connection);
        updateStatus(db, connections, connection.active ? STATUS_ACTIVE : status);
    }

    private void updateStatus(SQLiteDatabase db, List<Connection> connections, int status)
            throws SQLiteException {
        String selectionArgs = "";
        List<String> selections = new ArrayList<>();
        for (Connection curConnection : connections) {
            if (curConnection != null) {
                if (curConnection.id != Connection.DEFAULT_ID) {
                    if (!TextUtils.isEmpty(selectionArgs)) {
                        selectionArgs += ", ";
                    }
                    selectionArgs += "?";
                    selections.add(String.valueOf(curConnection.id));
                } else {
                    throw new SQLiteException("Not able to run query against connection with id " +
                            Connection.DEFAULT_ID);
                }
            } else {
                throw new SQLiteException("Found a null connection object while trying to change" +
                        " its status to " + status);
            }
        }

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STATUS, status);
        cv.put(COLUMN_UPDATED_AT, Calendar.getInstance().getTimeInMillis());
        db.update(TABLE_NAME,
                cv,
                COLUMN_ID + " in(" + selectionArgs + ")",
                selections.toArray(new String[0]));
    }

    public List<Connection> getAllActiveConnections(PushService system)
            throws PushService.PushSystemNotFoundException {
        List<Connection> activeConnections = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbWrapper.getReadableDatabase().query(TABLE_NAME, null,
                    COLUMN_STATUS + " = ? and " + COLUMN_SYSTEM + " = ?",
                    new String[]{String.valueOf(STATUS_ACTIVE), system.getName()},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Connection curConnection = extractConnection(cursor);
                    activeConnections.add(curConnection);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return activeConnections;
    }

    private Connection extractConnection(Cursor cursor)
            throws PushService.PushSystemNotFoundException {
        return new Connection(
                cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_SYSTEM)),
                cursor.getString(cursor.getColumnIndex(COLUMN_SERVER)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_PORT)),
                cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
                cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)) == STATUS_ACTIVE,
                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATED_AT))),
                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATED_AT))));
    }
}
