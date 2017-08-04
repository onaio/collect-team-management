package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.database.sqlite.SQLiteDatabase;

import io.ona.collect.android.team.persistence.sqlite.databases.DbWrapper;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public abstract class Table {
    protected final DbWrapper dbWrapper;

    protected Table(DbWrapper dbWrapper) {
        this.dbWrapper = dbWrapper;
    }

    public abstract void createTable(SQLiteDatabase db);
}
