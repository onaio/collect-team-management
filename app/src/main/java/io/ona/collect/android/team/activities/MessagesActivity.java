package io.ona.collect.android.team.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.MessageTable;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.MessageVirtualTable;
import io.ona.collect.android.team.pushes.services.PushService;
import io.ona.collect.android.team.ui.adapters.MessageAdapter;
import io.ona.collect.android.team.utils.Permissions;

public class MessagesActivity extends AppCompatActivity implements PushService.MessageListener {
    private static final String TAG = MessagesActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERM_OVERLAY = 213;
    private static final int REQUEST_CODE_CRITICAL_PERMISSIONS = 231;
    private RecyclerView messagesRecyclerView;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private MessageAdapter messageAdapter;
    private List<Message> allMessages;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        searchQuery = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Permissions.checkCanDrawOverlay(this)) {
            Permissions.requestDrawOverlay(this, REQUEST_CODE_PERM_OVERLAY);
        } else {
            requestCriticalPermissions();
        }
        initViews();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        searchQuery = intent.getStringExtra(SearchManager.QUERY);

        updateMessageListWithFilter();
    }


    private void updateMessageListWithFilter() {
        if (TextUtils.isEmpty(searchQuery)) {
            messageAdapter.update(allMessages);
        } else {
            new SearchTask(searchQuery).execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_messages, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchQuery = null;
                updateMessageListWithFilter();
                return false;
            }
        });

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (TeamManagement.getInstance().getPushServiceManager() != null)
            TeamManagement.getInstance().getPushServiceManager().removeMessageListener(this);
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
                    Toast.makeText(MessagesActivity.this,
                            "Initializing backend",
                            Toast.LENGTH_LONG).show();
                    updateViewData();
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

    private void initViews() {
        messagesRecyclerView = (RecyclerView) findViewById(R.id.messagesRecyclerView);
        recyclerLayoutManager = new LinearLayoutManager(this);
        messagesRecyclerView.setLayoutManager(recyclerLayoutManager);
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void updateViewData() {
        if (Permissions.getUnauthorizedCriticalPermissions(this).size() == 0) {
            new FetchMessagesTask(true).execute();
            TeamManagement.getInstance().getPushServiceManager().addMessageListener(this);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        messageAdapter.add(message);
    }

    @Override
    public void onMessageSent(Message message) {
        messageAdapter.add(message);
    }

    private class FetchMessagesTask extends AsyncTask<Void, Void, List<Message>> {
        private final boolean initial;
        private static final int LIST_SIZE = 50;

        public FetchMessagesTask(boolean initial) {
            this.initial = initial;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            MessageTable mt = (MessageTable) TeamManagement.getInstance()
                    .getTeamManagementDatabase().getTable(MessageTable.TABLE_NAME);
            try {
                if (initial) return mt.getMessages(LIST_SIZE);
                else {
                    ArrayList<Message> messages = messageAdapter.getReceivedMessages();
                    Collections.sort(messages);
                    return mt.getMessages(messages.get(messages.size() - 1).id + 1, LIST_SIZE);
                }
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

            if (messages != null) {
                if (initial) {
                    allMessages = new ArrayList<>(messages);
                } else {
                    if (allMessages == null) {
                        allMessages = new ArrayList<>();
                    }

                    allMessages.addAll(messages);
                }

                updateMessageListWithFilter();
            }
        }
    }

    private class SearchTask extends AsyncTask<Void, Void, List<Message>> {
        private final String query;

        public SearchTask(String query) {
            this.query = query;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            List<Message> filteredMessages = new ArrayList<>();
            MessageVirtualTable mvt = (MessageVirtualTable) TeamManagement.getInstance()
                    .getTeamManagementVirtualDatabase().getTable(MessageVirtualTable.TABLE_NAME);
            List<String> uuids = mvt.getMessageUuids(query);
            if (uuids != null) {
                HashMap<String, Message> messages = new HashMap<>();
                for (Message curMessage : allMessages) {
                    messages.put(curMessage.uuid, curMessage);
                }

                for (String curUuid : uuids) {
                    if (messages.containsKey(curUuid)) {
                        filteredMessages.add(messages.get(curUuid));
                    }
                }
            }

            return filteredMessages;
        }

        @Override
        protected void onPostExecute(List<Message> messages) {
            super.onPostExecute(messages);

            messageAdapter.update(messages);
        }
    }
}
