package io.ona.collect.android.team.persistence.carriers;

import java.util.Date;

/**
 * Contains info on a connection to a push notification system
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public class Connection {
    public final String system;
    public final String server;
    public final int port;
    public final String username;
    public final String password;
    public final boolean active;
    public final Date updatedAt;

    public Connection(String system, String server, int port, String username,
                      String password, boolean active, Date updatedAt) {
        this.system = system;
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public String getServerUri() {
        return server + ":" + String.valueOf(port);
    }
}
