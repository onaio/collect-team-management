package io.ona.collect.android.team.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import io.ona.collect.android.team.R;

public class MessagesActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERM_OVERLAY = 213;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        checkDrawOverlayPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_PERM_OVERLAY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode == REQUEST_CODE_PERM_OVERLAY) {
            if (Settings.canDrawOverlays(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                
            }
        }
    }
}
