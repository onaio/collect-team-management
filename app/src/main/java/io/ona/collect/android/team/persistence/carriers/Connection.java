package io.ona.collect.android.team.persistence.carriers;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.preferences.OdkPreferences;
import io.ona.collect.android.team.persistence.preferences.TeamMangementPreferences;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.services.ConnectionService;

/**
 * Contains info on a connection to a push notification system
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class Connection implements Serializable {
    public static final int DEFAULT_ID = -1;
    public final long id;
    public final PushService pushService;
    public final String server;
    public final int port;
    public final String username;
    public final String password;
    public final boolean active;
    public final Date createdAt;
    public final Date updatedAt;

    public Connection(long id, PushService pushService, String server, int port, String username,
                      String password, boolean active, Date createdAt, Date updatedAt) {
        this.id = id;
        this.pushService = pushService;
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Connection(PushService pushService, String server, int port, String username,
                      String password, boolean active) {
        this(DEFAULT_ID, pushService, server, port, username, password, active, Calendar.getInstance().getTime(), Calendar.getInstance().getTime());
    }

    public Connection(long id, String pushSystemName, String server, int port, String username,
                      String password, boolean active, Date createdAt, Date updatedAt)
            throws PushService.PushSystemNotFoundException {
        this.id = id;
        this.pushService = getPushSystem(pushSystemName);
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private PushService getPushSystem(String pushSystemName)
            throws PushService.PushSystemNotFoundException {
        ArrayList<PushService> systems = TeamManagement.getInstance().getActivePushServices();
        if (systems != null && pushSystemName != null) {
            for (PushService curSystem : systems) {
                if (curSystem != null && pushSystemName.equals(curSystem.getName())) {
                    return curSystem;
                }
            }
        }

        throw new PushService.PushSystemNotFoundException();
    }

    public String getServerUri() {
        return server + ":" + String.valueOf(port);
    }

    public static Connection createFromSharedPreference(PushService system, boolean active)
            throws PackageManager.NameNotFoundException, ConnectionService.ProtocolNotSupportedException {
        SharedPreferences odkPreferences = OdkPreferences.getPreferences();
        String protocol = odkPreferences.getString(OdkPreferences.KEY_PROTOCOL, null);
        if (ConnectionService.SUPPORTED_ODK_CONNECTION_PROTOCOLS.contains(protocol)) {
            SharedPreferences tmPreferences = TeamMangementPreferences.getPreferences();
            String server = tmPreferences.getString(TeamMangementPreferences.KEY_MQTT_SERVER,
                    TeamManagement.getInstance().getString(R.string.default_mqtt_server));
            int port = tmPreferences.getInt(TeamMangementPreferences.KEY_MQTT_PORT,
                    Integer.parseInt(
                            TeamManagement.getInstance().getString(R.string.default_mqtt_port)));
            String username = odkPreferences.getString(OdkPreferences.KEY_USERNAME, null);
            String password = odkPreferences.getString(OdkPreferences.KEY_PASSWORD, null);
            return new Connection(system, server, port, username, password, active);
        } else {
            throw new ConnectionService.ProtocolNotSupportedException(protocol + " is currently not supported");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Connection) {
            Connection connection = (Connection) obj;
            return pushService.equals(connection.pushService)
                    && server.equals(connection.server)
                    && port == connection.port
                    && username.equals(username)
                    && password.equals(password);
        }
        return false;
    }

}
