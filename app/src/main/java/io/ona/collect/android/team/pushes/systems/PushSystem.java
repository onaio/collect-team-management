package io.ona.collect.android.team.pushes.systems;

import android.content.Context;

/**
 * Created by Jason Rogena - jrogena@ona.io on 04/08/2017.
 */

public abstract class PushSystem {
    protected final Context context;

    protected PushSystem(Context context) {
        this.context = context;
    }

    public abstract boolean connect();

    public abstract boolean disconnect();
}
