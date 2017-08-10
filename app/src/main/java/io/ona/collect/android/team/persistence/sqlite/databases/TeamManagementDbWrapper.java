package io.ona.collect.android.team.persistence.sqlite.databases;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.ConnectionTable;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class TeamManagementDbWrapper extends DbWrapper {
    private static final String DATABASE_NAME = "team_management.db";
    private static final String DATABASE_PATH = TeamManagement.ODK_ROOT + File.separator + "metadata";
    private static final int DATABASE_VERSION = 1;
    private ConnectionTable connectionTable;

    public TeamManagementDbWrapper(Application application) {
        super(application, DATABASE_PATH, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.connectionTable = new ConnectionTable(this);
        this.connectionTable.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ConnectionTable getConnectionTable() {
        return this.connectionTable;
    }
}
