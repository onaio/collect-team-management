package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.ona.collect.android.team.persistence.carriers.Connection;
import io.ona.collect.android.team.persistence.sqlite.databases.TeamManagementDbWrapper;
import io.ona.collect.android.team.pushes.systems.PushSystem;

/**
 * Stores info on connections to push notification services
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class ConnectionTable extends Table {
    private static final String TAG = ConnectionTable.class.getSimpleName();
    private static final String TABLE_NAME = "connection";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_SYSTEM = "system";
    private static final String COLUMN_SERVER = "server";
    private static final String COLUMN_PORT = "port";
    private static final String COLUMN_ACTIVE = "active";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_SYSTEM + " VARCHAR NOT NULL," +
            COLUMN_SERVER + " VARCHAR NOT NULL," +
            COLUMN_PORT + " INTEGER NOT NULL," +
            COLUMN_USERNAME + " VARCHAR NOT NULL," +
            COLUMN_PASSWORD + " VARCHAR NULL," +
            COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT " + String.valueOf(ACTIVE) + "," +
            COLUMN_UPDATED_AT + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ")";
    private static final String QUERY_INDEX_SYSTEM_SERVER = "CREATE UNIQUE INDEX " +
            TABLE_NAME + "_" + COLUMN_SYSTEM + "_" + COLUMN_SERVER + "_index" +
            " ON " + TABLE_NAME +
            "(" + COLUMN_SYSTEM + "," + COLUMN_SERVER + "," + COLUMN_PORT + ");";

    public ConnectionTable(TeamManagementDbWrapper dbWrapper) {
        super(dbWrapper);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
        db.execSQL(QUERY_INDEX_SYSTEM_SERVER);
    }

    public List<Connection> getAllActiveConnections(PushSystem system) {
        List<Connection> activeConnections = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbWrapper.getReadableDatabase().query(TABLE_NAME, null,
                    COLUMN_ACTIVE + " = ? and " + COLUMN_SYSTEM + " = ?",
                    new String[]{String.valueOf(ACTIVE), system.getName()},
                    null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Connection curConnection = extractConnection(cursor);
                    if (curConnection != null) {
                        activeConnections.add(curConnection);
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

        return activeConnections;
    }

    private Connection extractConnection(Cursor cursor) {
        if (cursor != null && !cursor.isAfterLast()) {
            return new Connection(
                    cursor.getString(cursor.getColumnIndex(COLUMN_SYSTEM)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_SERVER)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_PORT)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVE)) == ACTIVE ? true : false,
                    new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATED_AT))));
        }

        return null;
    }
}
