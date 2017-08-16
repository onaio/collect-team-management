package io.ona.collect.android.team.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */
public class Permissions {
    private static final String[] CRITICAL_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK
    };

    public static List<String> getUnauthorizedCriticalPermissions(Context context) {
        List<String> unauthorizedPermissions = new ArrayList<>();
        for (String curPermission : CRITICAL_PERMISSIONS) {
            if (!Permissions.check(context, curPermission)) {
                unauthorizedPermissions.add(curPermission);
            }
        }

        return unauthorizedPermissions;
    }

    public static boolean check(Context context, String permission) {
        if(ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkCanDrawOverlay(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static void request(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static void requestDrawOverlay(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }
}
