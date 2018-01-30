package io.ona.collect.android.team.persistence.sqlite.databases.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Created by Jason Rogena - jrogena@ona.io on 29/09/2017.
 */

public class MessageVirtualTable extends VirtualTable {
    private static final String TAG = MessageVirtualTable.class.getSimpleName();
    public static final String TABLE_NAME = "message_virtual_table";
    private static final String COLUMN_MESSAGE_UUID = "message_uuid";
    private static final String COLUMN_AUTHOR_USERNAME = "author_username";
    private static final String COLUMN_AUTHOR_REAL_NAME = "author_real_name";
    private static final String COLUMN_FORM_ID = "form_id";
    private static final String COLUMN_FORM_NAME = "form_name";
    private static final String[] SEARCHABLE_COLUMNS = new String[]{
            COLUMN_FORM_NAME,
            COLUMN_FORM_ID,
            COLUMN_AUTHOR_USERNAME,
            COLUMN_AUTHOR_REAL_NAME
    };
    private static final String QUERY_CREATE_TABLE =
            "CREATE VIRTUAL TABLE " + TABLE_NAME +
                    " USING fts3 (" +
                    COLUMN_MESSAGE_UUID + ", " +
                    COLUMN_AUTHOR_USERNAME + ", " +
                    COLUMN_AUTHOR_REAL_NAME + ", " +
                    COLUMN_FORM_ID + ", " +
                    COLUMN_FORM_NAME + ")";

    public MessageVirtualTable(SQLiteOpenHelper databaseHelper) {
        super(TABLE_NAME, databaseHelper);
    }

    @Override
    public void createTable(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
        MessageTable mt = (MessageTable) TeamManagement.getInstance()
                .getTeamManagementDatabase().getTable(MessageTable.TABLE_NAME);
        try {
            List<Message> messages = mt.getMessages();
            if (messages != null) {
                for (Message curMessage : messages) {
                    addMessage(db, curMessage);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (PushService.PushSystemNotFoundException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public long addMessage(Message message) throws JSONException {
        return addMessage(databaseHelper.getWritableDatabase(), message);
    }

    public long addMessage(SQLiteDatabase db, Message message) throws JSONException {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MESSAGE_UUID, message.uuid);
        if (message.payload.has("author")) {
            JSONObject author = message.payload.getJSONObject("author");
            values.put(COLUMN_AUTHOR_REAL_NAME, author.getString("real_name"));
            values.put(COLUMN_AUTHOR_USERNAME, author.getString("username"));
        } else {
            String defaultUser = TeamManagement.getInstance().getString(R.string.default_author);
            values.put(COLUMN_AUTHOR_REAL_NAME, defaultUser);
            values.put(COLUMN_AUTHOR_USERNAME, defaultUser);
        }

        if (message.payload.has("form")
                && message.payload.getJSONObject("form").has("formId")) {
            String formId = message.payload.getJSONObject("form").getString("formId");
            String name = message.payload.getJSONObject("form").getString("name");
            values.put(COLUMN_FORM_ID, formId);
            values.put(COLUMN_FORM_NAME, name);
        }

        return db.insert(TABLE_NAME, null, values);
    }

    public List<String> getMessageUuids(String query) {
        Cursor cursor = null;
        List<String> uuids = new ArrayList<>();
        try {
            String selection = "";
            String[] selectionArgs = new String[SEARCHABLE_COLUMNS.length];
            for (int i = 0; i < SEARCHABLE_COLUMNS.length; i++) {
                if (i != 0) {
                    selection += " OR ";
                }
                selection += SEARCHABLE_COLUMNS[i] + " MATCH ?";
                selectionArgs[i] = "*" + query + "*";
            }

            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            builder.setTables(TABLE_NAME);
            cursor = builder.query(databaseHelper.getReadableDatabase(),
                    new String[]{COLUMN_MESSAGE_UUID}, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    uuids.add(cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_UUID)));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return uuids;
    }
}
