package io.ona.collect.android.team.activities;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.utils.Permissions;

public class MessagesActivity extends AppCompatActivity {
    private static final String TAG = MessagesActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERM_OVERLAY = 213;
    private static final int REQUEST_CODE_CRITICAL_PERMISSIONS = 231;

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
                if (requestCriticalPermissions()) {
                    TeamManagement.getInstance().initBackend();
                    Toast.makeText(MessagesActivity.this, "Initializing backend", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean requestCriticalPermissions() {
        boolean result = true;
        List<String> permissionsToRequest = Permissions.getUnauthorizedCriticalPermissions(this);
        if (permissionsToRequest.size() > 0) {
            result = false;
            Permissions.request(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_CODE_CRITICAL_PERMISSIONS);
        }

        return result;
    }
}
