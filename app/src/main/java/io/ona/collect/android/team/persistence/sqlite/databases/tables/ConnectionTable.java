package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.database.sqlite.SQLiteDatabase;

import io.ona.collect.android.team.persistence.sqlite.databases.TeamManagementDbWrapper;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class ConnectionTable extends Table {
    private static final String TAG = ConnectionTable.class.getSimpleName();
    private static final String TABLE_NAME = "connection";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_SERVER = "server";
    private static final String COLUMN_PORT = "port";
    private static final String COLUMN_ACTIVE = "active";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;
    private static final String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
            "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_SERVER + " VARCHAR NOT NULL," +
            COLUMN_PORT + " INTEGER NOT NULL," +
            COLUMN_USERNAME + " VARCHAR NOT NULL," +
            COLUMN_PASSWORD + " VARCHAR NULL," +
            COLUMN_ACTIVE + " INTEGER NOT NULL DEFAULT " + String.valueOf(INACTIVE) +
            ")";
    private static final String QUERY_INDEX_SERVER_USER = "CREATE UNIQUE INDEX " +
            TABLE_NAME + "_" + COLUMN_SERVER + "_" + COLUMN_USERNAME + "_index" +
            " ON " + TABLE_NAME +
            "(" + COLUMN_SERVER + "," + COLUMN_PORT + "," + COLUMN_USERNAME + ");";

    public ConnectionTable(TeamManagementDbWrapper dbWrapper) {
        super(dbWrapper);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
        db.execSQL(QUERY_INDEX_SERVER_USER);
    }
}
