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

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */
public class Permissions {
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
