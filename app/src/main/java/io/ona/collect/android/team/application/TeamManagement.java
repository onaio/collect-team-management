package io.ona.collect.android.team.application;

import android.Manifest;
import android.app.Application;

import io.ona.collect.android.team.persistence.sqlite.databases.TeamManagementDbWrapper;
import io.ona.collect.android.team.utils.Permissions;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class TeamManagement extends Application {
    private TeamManagementDbWrapper teamManagementDatabase;
    private static TeamManagement instance;

    public static TeamManagement getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
        getTeamManagementDatabase();
    }

    @Override
    public void onTerminate() {
        if (teamManagementDatabase != null) {
            teamManagementDatabase.close();
        }
        super.onTerminate();
    }

    public TeamManagementDbWrapper getTeamManagementDatabase() {
        if (teamManagementDatabase == null && Permissions.check(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            teamManagementDatabase = new TeamManagementDbWrapper(this);
            teamManagementDatabase.getWritableDatabase();
        }

        return teamManagementDatabase;
    }
}
