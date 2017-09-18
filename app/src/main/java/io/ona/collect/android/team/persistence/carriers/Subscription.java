package io.ona.collect.android.team.persistence.carriers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ona.collect.android.team.pushes.services.MqttPushService;
import io.ona.collect.android.team.pushes.services.PushService;

/**
 * Holds subscription data (theoretically stored by
 * {@link io.ona.collect.android.team.persistence.sqlite.databases.tables.SubscriptionTable})
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 15/08/2017.
 */

public class Subscription implements Serializable {
    private static final String FORM_SCHEMA_UPDATES_TOPIC_FORMAT = "forms/%s/schema/updated";
    private static final Pattern FORM_SCHEMA_UPDATES_TOPIC_PATTERN =
            Pattern.compile("forms/\\w+/schema/updated");
    private static final String FORM_MESSAGE_TOPIC_FORMAT = "forms/%s/message/published";
    private static final Pattern FORM_MESSAGE_TOPIC_PATTERN =
            Pattern.compile("forms/\\w+/message/published");
    public static final long DEFAULT_ID = -1;
    public static final int DEFAULT_QOS = -1;
    public final long id;
    public final Connection connection;
    public final String topic;
    public final int qos;
    public final boolean active;
    public final Date createdAt;
    public final Date updatedAt;

    public Subscription(long id, Connection connection, String topic, int qos, boolean active,
                        Date createdAt, Date updatedAt) {
        this.id = id;
        this.connection = connection;
        this.topic = topic;
        this.qos = qos;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Subscription(Connection connection, String topic, int qos, boolean active) {
        this(DEFAULT_ID, connection, topic, qos, active, Calendar.getInstance().getTime(), Calendar.getInstance().getTime());
    }

    public Subscription(Subscription subscription) {
        this.id = subscription.id;
        this.connection = subscription.connection;
        this.topic = subscription.topic;
        this.qos = subscription.qos;
        this.active = subscription.active;
        this.createdAt = subscription.createdAt;
        this.updatedAt = subscription.updatedAt;
    }

    public static List<Subscription> getSubscriptionsFromOdkForm(Connection connection,
                                                                 OdkForm odkForm)
            throws PushService.PushSystemNotFoundException {
        List<Subscription> subscriptions = new ArrayList<>();

        if (odkForm != null) {
            if (connection.pushService.getName().equals(MqttPushService.NAME)) {
                // Create subscription for schema updates
                String updatesTopic =
                        String.format(FORM_SCHEMA_UPDATES_TOPIC_FORMAT, odkForm.getPkid());
                subscriptions.add(new Subscription(
                        connection, updatesTopic, MqttPushService.QOS_EXACTLY_ONCE,
                        !odkForm.state.equals(OdkForm.STATE_FORM_DELETED)));

                // Create subscription for free-form messages
                String messagesTopic =
                        String.format(FORM_MESSAGE_TOPIC_FORMAT, odkForm.getPkid());
                subscriptions.add(new Subscription(
                        connection, messagesTopic, MqttPushService.QOS_EXACTLY_ONCE,
                        !odkForm.state.equals(OdkForm.STATE_FORM_DELETED)));
            } else {
                throw new PushService.PushSystemNotFoundException(
                        "Could not create topics for push service "
                                + connection.pushService.getName());
            }
        }

        return subscriptions;
    }

    public static boolean isFormSchemaUpdateSubscription(Subscription subscription) {
        Matcher matcher = FORM_SCHEMA_UPDATES_TOPIC_PATTERN.matcher(subscription.topic);
        return matcher.find();
    }
}
