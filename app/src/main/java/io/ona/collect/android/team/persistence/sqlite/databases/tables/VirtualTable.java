package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.ona.collect.android.team.persistence.sqlite.databases.TeamManagementVirtualDatabase;

/**
 * Created by Jason Rogena - jrogena@ona.io on 29/09/2017.
 */

public abstract class VirtualTable {
    public abstract void createTable(SQLiteDatabase db);
    private final String tableName;
    protected final SQLiteOpenHelper databaseHelper;

    public VirtualTable(String tableName, SQLiteOpenHelper databaseHelper) {
        this.tableName = tableName;
        this.databaseHelper = databaseHelper;
    }

    public String getTableName() {
        return tableName;
    }
}
