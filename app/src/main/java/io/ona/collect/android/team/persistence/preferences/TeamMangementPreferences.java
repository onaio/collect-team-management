package io.ona.collect.android.team.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import io.ona.collect.android.team.application.TeamManagement;

/**
 * Handles {@link android.content.SharedPreferences} tied to this package
 *
 * Created by Jason Rogena - jrogena@ona.io on 14/08/2017.
 */

public class TeamMangementPreferences {
    private static final String ALL_PREFERENCES = "all_preferences";
    public static final String KEY_MQTT_SERVER = "mqtt_server";
    public static final String KEY_MQTT_PORT = "mqtt_port";
    public static SharedPreferences getPreferences() {
        return TeamManagement.getInstance()
                .getSharedPreferences(ALL_PREFERENCES, Context.MODE_PRIVATE);
    }
}
