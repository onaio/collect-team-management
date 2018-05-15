package io.ona.collect.android.team.ui.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.ona.collect.android.team.R;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.Message;
import io.ona.collect.android.team.persistence.carriers.Subscription;
import io.ona.collect.android.team.persistence.sqlite.databases.tables.MessageTable;

/**
 * Created by Jason Rogena - jrogena@ona.io on 05/09/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final String TAG = MessageAdapter.class.getSimpleName();
    private ArrayList<Message> receivedMessages;

    public MessageAdapter() {
        this.receivedMessages = new ArrayList<>();
    }

    public void update(List<Message> receivedMessages) {
        if (receivedMessages != null) {
            new MarkMessagesAsReadTask(receivedMessages).execute();
            this.receivedMessages = new ArrayList<>();
            this.receivedMessages.addAll(receivedMessages);
            sort();
            this.notifyDataSetChanged();
        }
    }

    public void addAll(List<Message> receivedMessages) {
        if (receivedMessages != null) {
            new MarkMessagesAsReadTask(receivedMessages).execute();
            for (Message curMessage : receivedMessages) {
                add(curMessage, false);
            }
        }
    }

    public void add(Message message) {
        add(message, true);
    }

    private void add(Message message, boolean markRead) {
        if (this.receivedMessages == null) this.receivedMessages = new ArrayList<>();
        if (!this.receivedMessages.contains(message)) {
            if (markRead) {
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                new MarkMessagesAsReadTask(messages).execute();
            }
            this.receivedMessages.add(message);
            sort();
            this.notifyItemInserted(receivedMessages.indexOf(message));
        }
    }

    private void sort() {
        Collections.sort(receivedMessages, new Comparator<Message>() {
            @Override
            public int compare(Message t1, Message t2) {
                return t2.receivedAt.compareTo(t1.receivedAt);
            }
        });
    }

    public ArrayList<Message> getReceivedMessages() {
        return new ArrayList<>(receivedMessages);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        if (position < receivedMessages.size()) {
            try {
                holder.updateData(receivedMessages.get(position));
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            Log.wtf(TAG, "Could not add a message to the message recycler view because it" +
                    " appears to have an index that is out of bounds");
        }
    }

    @Override
    public int getItemCount() {
        return receivedMessages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final SimpleDateFormat TIME_RECEIVED_FULL_DATE_FORMAT =
                new SimpleDateFormat("MMM d, y 'at' h:mm a");// Jun 8, 2017 at 06:33 PM
        private final SimpleDateFormat TIME_RECEIVED_THIS_YEAR_FORMAT =
                new SimpleDateFormat("MMM d 'at' h:mm a");// Jun 8, 2017 at 06:33 PM
        private final SimpleDateFormat TIME_RECEIVED_TODAY_FORMAT =
                new SimpleDateFormat("'at' h:mm a");// Jun 8, 2017 at 06:33 PM
        private final String TAG_FORM_ID = "form_id";
        private final String TAG_IS_SCHEMA_UPDTE = "is_schema_update";
        private static HashMap<String, Integer> AVATAR_COLORS = new HashMap<>();
        private static Random colorRandom = new Random();
        private final TextView senderNameTextView;
        private final TextView receiveTimeTextView;
        private final TextView messageTextView;
        private final TextView formIdTextView;
        private final TextView initialsTextView;
        private final TextView launchFormTextView;
        private final View launchFormView;
        private final Context context;

        public MessageViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            senderNameTextView = (TextView) itemView.findViewById(R.id.senderNameTextView);
            receiveTimeTextView = (TextView) itemView.findViewById(R.id.receiveTimeTextView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            formIdTextView = (TextView) itemView.findViewById(R.id.formIdTextView);
            initialsTextView = (TextView) itemView.findViewById(R.id.initialsTextView);
            launchFormTextView = (TextView) itemView.findViewById(R.id.launchFormTextView);
            launchFormView = itemView.findViewById(R.id.launchFormView);
            launchFormView.setOnClickListener(this);
        }

        public void updateData(Message message) throws JSONException {
            int typeface = Typeface.NORMAL;
            if (!message.read) {
                typeface = Typeface.BOLD;
            }

            senderNameTextView.setTypeface(null, typeface);
            if (message.payload.has("author")) {
                JSONObject author = message.payload.getJSONObject("author");
                senderNameTextView.setText(String.format(
                        "%s (%s)", author.getString("real_name"), author.getString("username")));
                initialsTextView.setText(getInitials(author.getString("real_name")));
                GradientDrawable initialsBackground = (GradientDrawable) initialsTextView.getBackground();
                initialsBackground.setColor(getUsernameColor(author.getString("username")));

            } else {
                String defaultUser = context.getString(R.string.default_author);
                senderNameTextView.setText(defaultUser);
                initialsTextView.setText(getInitials(defaultUser));
                initialsTextView.setBackgroundColor(getUsernameColor(defaultUser));
            }

            receiveTimeTextView.setTypeface(null, typeface);
            if (message.receivedAt != null) {
                Calendar receivedAtCalendar = Calendar.getInstance();
                receivedAtCalendar.setTime(message.receivedAt);
                Calendar today = Calendar.getInstance();
                SimpleDateFormat format = TIME_RECEIVED_FULL_DATE_FORMAT;
                if (receivedAtCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                        && receivedAtCalendar.get(Calendar.DATE) == today.get(Calendar.DATE)) {
                    format = TIME_RECEIVED_TODAY_FORMAT;
                } else if (receivedAtCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    format = TIME_RECEIVED_THIS_YEAR_FORMAT;
                }

                receiveTimeTextView.setText(format.format(message.receivedAt));
            } else {
                receiveTimeTextView.setText(null);
            }

            messageTextView.setTypeface(null, typeface);
            if (message.payload.has("message")) {
                messageTextView.setText(message.payload.getString("message"));
            } else {
                messageTextView.setText(null);
            }

            if (message.payload.has("context")
                    && message.payload.getJSONObject("context").has("type")
                    && message.payload.getJSONObject("context").has("metadata")
                    && message.payload.getJSONObject("context").getString("type").equals("xform")) {
                boolean isRelatedToSchemaUpdate = Subscription.isFormSchemaUpdateSubscription(message.subscription);
                String formId = message.payload.getJSONObject("context").getJSONObject("metadata").getString("form_id");
                formIdTextView.setText(formId);
                launchFormView.setClickable(true);
                launchFormView.setTag(R.id.formId, formId);
                launchFormView.setTag(R.id.isRelatedToFormSchema, isRelatedToSchemaUpdate);

                if (isRelatedToSchemaUpdate) {
                    launchFormTextView.setText(context.getString(R.string.update_form));
                } else {
                    launchFormTextView.setText(context.getString(R.string.open_form));
                }
            } else {
                formIdTextView.setText(null);
                launchFormView.setClickable(false);
                launchFormView.setTag(null);
            }
        }

        @Override
        public void onClick(View view) {
            if (view.equals(launchFormView) && launchFormView.getTag() != null) {
                String formId = (String) launchFormView.getTag(R.id.formId);
                Boolean isRelatedToFormSchema = (Boolean) launchFormView.getTag(R.id.isRelatedToFormSchema);
            }
        }

        private String getInitials(String fullName) {
            if (!TextUtils.isEmpty(fullName)) {
                String[] names = fullName.split("\\s+");
                String joined = "";
                for(int i = 0; i < names.length && i < 2; i++) {
                    joined += names[i].charAt(0);
                }

                if (!TextUtils.isEmpty(joined)) return joined;
            }
            return null;
        }

        private int getUsernameColor(String username) {
            if (!AVATAR_COLORS.containsKey(username)) {
                TypedArray colors = context.getResources().obtainTypedArray(
                        context.getResources().getIdentifier(
                                "avatarColors",
                                "array",
                                context.getApplicationContext().getPackageName()));
                int index = colorRandom.nextInt(colors.length());
                AVATAR_COLORS.put(username, colors.getColor(index, Color.RED));
                colors.recycle();
            }

            return AVATAR_COLORS.get(username);
        }
    }

    private class MarkMessagesAsReadTask extends AsyncTask<Void, Void, Void> {
        private final List<Message> messages;

        public MarkMessagesAsReadTask(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MessageTable mt = (MessageTable) TeamManagement.getInstance()
                    .getTeamManagementDatabase().getTable(MessageTable.TABLE_NAME);
            mt.markAsRead(messages);
            return null;
        }
    }
}
