package io.ona.collect.android.team.persistence.carriers;

import java.util.Date;

/**
 * Holds subscription data (theoretically stored by
 * {@link io.ona.collect.android.team.persistence.sqlite.databases.tables.SubscriptionTable})
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 15/08/2017.
 */

public class Subscription {
    public static final int DEFAULT_ID = -1;
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

    public Subscription(Subscription subscription) {
        this.id = subscription.id;
        this.connection = subscription.connection;
        this.topic = subscription.topic;
        this.qos = subscription.qos;
        this.active = subscription.active;
        this.createdAt = subscription.createdAt;
        this.updatedAt = subscription.updatedAt;
    }
}
