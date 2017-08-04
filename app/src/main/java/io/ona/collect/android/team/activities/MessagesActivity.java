package io.ona.collect.android.team.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.utils.Permissions;

public class MessagesActivity extends AppCompatActivity {
    private static final String TAG = MessagesActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERM_OVERLAY = 213;
    private static final int REQUEST_CODE_CRITICAL_PERMISSIONS = 231;
    private static final String[] CRITICAL_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Permissions.checkCanDrawOverlay(this)) {
            Permissions.requestDrawOverlay(this, REQUEST_CODE_PERM_OVERLAY);
        } else {
            requestCriticalPermissions();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "request code is " + requestCode + " and result code " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_PERM_OVERLAY:
                requestCriticalPermissions();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_CRITICAL_PERMISSIONS:
                TeamManagement.getInstance().getTeamManagementDatabase();
                requestCriticalPermissions();
                break;
        }
    }

    private boolean requestCriticalPermissions() {
        boolean result = true;
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String curPermission : CRITICAL_PERMISSIONS) {
            if (!Permissions.check(this, curPermission)) {
                permissionsToRequest.add(curPermission);
            }
        }

        if (permissionsToRequest.size() > 0) {
            result = false;
            Permissions.request(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_CODE_CRITICAL_PERMISSIONS);
        }

        return result;
    }
}
