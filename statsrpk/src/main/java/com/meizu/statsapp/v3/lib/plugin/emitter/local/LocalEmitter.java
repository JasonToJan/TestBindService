package com.meizu.statsapp.v3.lib.plugin.emitter.local;

import android.content.Context;

import com.meizu.statsapp.v3.lib.plugin.emitter.Emitter;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.sdk.UmidFetcher;
import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Created by huchen on 16-8-22.
 */
public class LocalEmitter extends Emitter {
    private final static String TAG = "LocalEmitter";
    private LocalEmitterWorker emitterWorker;

    public LocalEmitter(Context context, String pkgKey) {
        super(context, pkgKey);
        this.emitterWorker = new LocalEmitterWorker(context, emitterConfig);
    }

    public void init() {
        Logger.d(TAG, "init");
        emitterWorker.init();
    }

    @Override
    public void updateConfig(boolean active,
                             boolean flushOnStart, boolean flushOnReconnect, boolean flushOnCharge,
                             long flushDelayInterval, int flushCacheLimit, long flushMobileTrafficLimit,
                             int neartimeInterval) {
        super.updateConfig(active,
                flushOnStart, flushOnReconnect, flushOnCharge,
                flushDelayInterval, flushCacheLimit, flushMobileTrafficLimit,
                neartimeInterval);
        emitterWorker.updateConfig(emitterConfig);
    }

    @Override
    public void add(final TrackerPayload payload) {
        Logger.d(TAG, "add payload: " + payload.toString());
        if (emitterConfig.isActive()) {
            emitterWorker.add(payload);
        }
    }

    @Override
    public void addRealtime(final TrackerPayload payload) {
        Logger.d(TAG, "addRealtime payload: " + payload.toString());
        if (emitterConfig.isActive()) {
            emitterWorker.addRealtime(payload);
        }
    }

    @Override
    public void addNeartime(final TrackerPayload payload) {
        Logger.d(TAG, "addNeartime payload: " + payload.toString());
        if (emitterConfig.isActive()) {
            emitterWorker.addNeartime(payload);
        }
    }

    @Override
    public void flush() {
        Logger.d(TAG, "flush");
        emitterWorker.flush();
    }

    @Override
    public void updateEventSource(String sessionId, String eventSource) {
        emitterWorker.updateEventSource(sessionId, eventSource);
    }

    @Override
    public void setEncrypt(boolean encrypt) {
        emitterWorker.setEncrypt(encrypt);
    }

    @Override
    public String getUMID() {
        return UmidFetcher.getInstance(mContext).readUmidFromLocal();
    }
}
