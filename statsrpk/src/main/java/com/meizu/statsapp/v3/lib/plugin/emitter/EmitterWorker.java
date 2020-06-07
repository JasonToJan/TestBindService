package com.meizu.statsapp.v3.lib.plugin.emitter;

import android.content.Context;

/**
 * Created by huchen on 16-9-7.
 */
public abstract class EmitterWorker {
    private final static String TAG = EmitterWorker.class.getSimpleName();
    // Used across thread boundaries
    protected Context context;

    public EmitterWorker(Context context) {
        this.context = context;
    }

}
