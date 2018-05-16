package io.ona.collect.android.team.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.List;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.activities.MessagesActivity;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.MessageTable;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 02/08/2017.
 * Attribution:
 * - https://gist.github.com/bjoernQ/6975256
 */

public class MessageOverlayService extends Service implements View.OnTouchListener, PushService.MessageListener {
    private static final String TAG = MessageOverlayService.class.getCanonicalName();
    private static final String PREFERENCE_X_POS = "message_overlay_x";
    private static final String PREFERENCE_Y_POS = "message_overlay_y";
    private static final String KEY_ACTIVITY_NAME = "activity_name";
    private static final String KEY_STATE = "state";
    private static final String STATE_RESUMED = "resumed";
    private static final String STATE_STARTED = "started";
    private RelativeLayout messageOverlay;
    private WindowManager windowManager;
    private SharedPreferences sharedPreferences;
    private View topLeftView;
    private Button messageOverlayButton;
    private TextView messageOverlayText;
    private int originalXPos;
    private int originalYPos;
    private float offsetX;
    private float offsetY;
    private boolean moving;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            String state = bundle.getString(KEY_STATE);
            String activityName = bundle.getString(KEY_ACTIVITY_NAME);
            Log.d(TAG, "state = " + state + " and activity name = " + activityName);
            if (!TextUtils.isEmpty(state) && !TextUtils.isEmpty(activityName)
                    && state.equals(STATE_STARTED)
                    && activityName.contains("MainMenuActivity")) {
                createOverlayView();
            }
        }
        TeamManagement.getInstance().getPushServiceManager().addMessageListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "MessageOverlayService onDestroy called");
        destroyOverlayView();
        TeamManagement.getInstance().getPushServiceManager().removeMessageListener(this);
        super.onDestroy();
    }

    private void createOverlayView() {
        if (messageOverlay != null ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !Settings.canDrawOverlays(this))) {
            return;
        }

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = sharedPreferences.getInt(PREFERENCE_X_POS, 0);
        params.y = sharedPreferences.getInt(PREFERENCE_Y_POS, 0);

        messageOverlay =
                (RelativeLayout) layoutInflater.inflate(R.layout.view_message_overlay, null);
        messageOverlayButton =
                (Button) messageOverlay.findViewById(R.id.messageOverlayButton);
        messageOverlayText = (TextView) messageOverlay.findViewById(R.id.messageOverlayText) ;
        messageOverlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageOverlayService.this, MessagesActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                destroyOverlayView();
            }
        });
        messageOverlayButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!moving) {
                    destroyOverlayView();
                }
                return true;
            }
        });
        messageOverlay.setOnTouchListener(this);
        messageOverlayButton.setOnTouchListener(this);
        windowManager.addView(messageOverlay, params);

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        windowManager.addView(topLeftView, topLeftParams);

        new UpdateMessageViewsTask().execute();
    }

    private void updateOverlayText(List<Message> messages) {
        if (messages != null) {
            if (messageOverlayText != null) {
                if (messages.size() > 0) {
                    String messagePluralString = getResources().getString(R.string.messages);
                    if (messages.size() == 1) {
                        messagePluralString = getResources().getString(R.string.message);
                    }
                    messageOverlayText.setText(
                            String.format(
                                    this.getResources().getString(R.string.you_have_new_messages),
                                    messages.size(), messagePluralString));
                } else {
                    messageOverlayText.setText(R.string.you_have_no_new_messages);
                }
            }

            if (messageOverlayButton != null) {
                messageOverlayButton.setText(String.valueOf(messages.size()));
            }
        }
    }

    private void destroyOverlayView() {
        if (messageOverlay != null) {
            int[] location = new int[2];
            messageOverlay.getLocationOnScreen(location);
            saveOverlayPosition(location[0], location[1]);

            windowManager.removeView(messageOverlay);
            windowManager.removeView(topLeftView);
            messageOverlay = null;
            topLeftView = null;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        try {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                float x = motionEvent.getRawX();
                float y = motionEvent.getRawY();

                moving = false;

                int[] location = new int[2];
                messageOverlay.getLocationOnScreen(location);

                originalXPos = location[0];
                originalYPos = location[1];

                offsetX = location[0] - x;
                offsetY = location[1] - y;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                int[] topLeftLocationOnScreen = new int[2];
                topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

                float x = motionEvent.getRawX();
                float y = motionEvent.getRawY();

                WindowManager.LayoutParams params =
                        (WindowManager.LayoutParams) messageOverlay.getLayoutParams();

                int newX = (int) (offsetX + x);
                int newY = (int) (offsetY + y);

                if (Math.abs(newX - originalXPos) < 20
                        && Math.abs(newY - originalYPos) < 20
                        && !moving) {
                    return false;
                }

                params.x = newX - (topLeftLocationOnScreen[0]);
                params.y = newY - (topLeftLocationOnScreen[1]);

                windowManager.updateViewLayout(messageOverlay, params);
                moving = true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (moving) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    private void saveOverlayPosition(int x, int y) {
        sharedPreferences.edit()
                .putInt(PREFERENCE_X_POS, x)
                .putInt(PREFERENCE_Y_POS, y)
                .commit();
    }

    @Override
    public void onMessageReceived(Message message) {
        new UpdateMessageViewsTask().execute();
    }

    @Override
    public void onMessageSent(Message message) {

    }

    private class UpdateMessageViewsTask extends AsyncTask<Void, Void, List<Message>> {
        private static final int LIST_SIZE = 50;

        public UpdateMessageViewsTask() {
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            MessageTable mt = (MessageTable) TeamManagement.getInstance()
                    .getTeamManagementDatabase().getTable(MessageTable.TABLE_NAME);
            try {
                    return mt.getMessages(false);
            } catch (PushService.PushSystemNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            super.onPostExecute(messages);
            updateOverlayText(messages);
        }
    }
}
