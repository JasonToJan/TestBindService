package com.meizu.statsapp.v3.lib.plugin.emitter;

import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;

/**
 * Created by jinhui on 17-12-14.
 */

public class EmittableEvent {
    private String packageName = null;
    private long eventId;
    private TrackerPayload eventPayload = null;

    public EmittableEvent(String pkgName, long id, TrackerPayload payload) {
        packageName = pkgName;
        eventId = id;
        eventPayload = payload;
    }

    public long getId() {
        return eventId;
    }

    public TrackerPayload getPayload() {
        return eventPayload;
    }
}
