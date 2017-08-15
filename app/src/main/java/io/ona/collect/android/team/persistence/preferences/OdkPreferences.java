package io.ona.collect.android.team.persistence.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import io.ona.collect.android.team.application.TeamManagement;

/**
 * Created by Jason Rogena - jrogena@ona.io on 07/08/2017.
 */

public class OdkPreferences {
    private static final String TAG = OdkPreferences.class.getSimpleName();
    public static final String KEY_PROTOCOL = "protocol";
    public static final String PROTOCOL_ODK = "odk_default";
    public static final String PROTOCOL_OTHER = "other_protocol";
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT = "selected_google_account";
    public static final String KEY_GOOGLE_SHEETS_URL = "google_sheets_url";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SERVER_URL = "server_url";
    public static final String KEY_FORMLIST_URL = "formlist_url";
    public static final String KEY_SUBMISSION_URL = "submission_url";

    public static SharedPreferences getPreferences() throws PackageManager.NameNotFoundException {
        Context odkContext = TeamManagement.getInstance().createPackageContext(
                TeamManagement.ODK_PACKAGE_NAME,
                Context.MODE_WORLD_READABLE);
        return PreferenceManager.getDefaultSharedPreferences(odkContext);
    }
}
