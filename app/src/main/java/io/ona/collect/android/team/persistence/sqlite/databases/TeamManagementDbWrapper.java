package io.ona.collect.android.team.persistence.sqlite.databases;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.ConnectionTable;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.SubscriptionTable;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.Table;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class TeamManagementDbWrapper extends DbWrapper {
    private static final String DATABASE_NAME = "team_management.db";
    private static final String DATABASE_PATH = TeamManagement.ODK_ROOT + File.separator + "metadata";
    private static final int DATABASE_VERSION = 1;

    public TeamManagementDbWrapper(Application application) {
        super(application, DATABASE_PATH, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ConnectionTable connectionTable = new ConnectionTable(this);
        connectionTable.createTable(db);
        tables.put(connectionTable.getName(), connectionTable);
        SubscriptionTable subscriptionTable = new SubscriptionTable(this);
        subscriptionTable.createTable(db);
        tables.put(subscriptionTable.getName(), subscriptionTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
