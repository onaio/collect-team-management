package io.ona.collect.android.team.persistence.sqlite.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;

import io.ona.collect.android.team.persistence.sqlite.databases.tables.MessageVirtualTable;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.VirtualTable;

/**
 * Created by Jason Rogena - jrogena@ona.io on 29/09/2017.
 */
public class TeamManagementVirtualDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TeamManagementVirtual";
    private static final int DATABASE_VERSION = 4;
    private final Context context;
    private final HashMap<String, VirtualTable> tables;
    private SQLiteDatabase database;

    public TeamManagementVirtualDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        tables = new HashMap<>();
        tables.put(MessageVirtualTable.TABLE_NAME, new MessageVirtualTable(this));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        database = db;
        for (VirtualTable curTable : tables.values()) {
            curTable.createTable(db);
        }
    }

    public VirtualTable getTable(String name) {
        return tables.get(name);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
