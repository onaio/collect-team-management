package io.ona.collect.android.team.application;

import android.Manifest;
import android.app.Application;
import android.os.Environment;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.io.File;
import java.util.ArrayList;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.persistence.resolvers.OdkFormsContentResolver;
import io.ona.collect.android.team.persistence.sqlite.databases.TeamManagementDbWrapper;
import io.ona.collect.android.team.pushes.messages.handlers.MessageHandler;
import io.ona.collect.android.team.pushes.services.MqttPushService;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.services.StartupService;
import io.ona.collect.android.team.utils.Permissions;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class TeamManagement extends Application {
    public static final String ODK_PACKAGE_NAME = "org.odk.collect.android";
    public static final String ODK_ROOT = Environment.getExternalStorageDirectory()
            + File.separator + "odk";
    private TeamManagementDbWrapper teamManagementDatabase;
    private OdkFormsContentResolver odkFormsContentResolver;
    private static TeamManagement instance;
    private MessageHandler messageHandler;
    private ArrayList<PushService> activePushServices;

    public static TeamManagement getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
        initBackend();
    }

    public void initBackend() {
        if (Permissions.getUnauthorizedCriticalPermissions(this).size() == 0) {
            prepareDatabases();
            preparePushSystems();
            if (teamManagementDatabase != null) {
                StartupService.start(this);
            }
        }
    }

    private void prepareDatabases() {
        createODKDirs();
        getTeamManagementDatabase();
    }

    private void preparePushSystems() {
        messageHandler = new MessageHandler();
        activePushServices = new ArrayList<>();
        activePushServices.add(new MqttPushService(messageHandler, messageHandler));
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

    public OdkFormsContentResolver getOdkFormsContentResolver() {
        if (odkFormsContentResolver == null) {
            odkFormsContentResolver = new OdkFormsContentResolver();
        }

        return odkFormsContentResolver;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public ArrayList<PushService> getActivePushServices() {
        return activePushServices;
    }

    /**
     * Creates required directories on the SDCard (or other external storage)
     *
     * @throws RuntimeException if there is no SDCard or the directory exists as a non directory
     */
    private void createODKDirs() throws RuntimeException {
        String cardstatus = Environment.getExternalStorageState();
        if (!cardstatus.equals(Environment.MEDIA_MOUNTED)) {
            throw new RuntimeException(getString(R.string.sdcard_unmounted, cardstatus));
        }

        String[] dirs = {
                ODK_ROOT
        };

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new RuntimeException("ODK reports :: Cannot create directory: "
                            + dirName);
                }
            } else {
                if (!dir.isDirectory()) {
                    throw new RuntimeException("ODK reports :: " + dirName
                            + " exists, but is not a directory");
                }
            }
        }
    }
}
