/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.meizu.statsapp.v3.lib.plugin.emitter;

import android.content.Context;
import android.content.SharedPreferences;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;

/**
 * Build an emitter object which controls the
 * sending of events to the Snowplow Collector.
 */
public abstract class Emitter {

    protected Context mContext;
    protected EmitterConfig emitterConfig;
    private SharedPreferences mSP;

    public Emitter(Context context, String pkgKey) {
        mContext = context;
        mSP = context.getSharedPreferences(UxipConstants.PREFERENCES_EMITTER_CONFIG_NAME, Context.MODE_PRIVATE);
        emitterConfig = new EmitterConfig(pkgKey);
        readConfigFromPreference();
    }

    public abstract void init();

    public void updateConfig(boolean active,
                                      boolean flushOnStart, boolean flushOnReconnect, boolean flushOnCharge,
                                      long flushDelayInterval, int flushCacheLimit, long flushMobileTrafficLimit, int neartimeInterval) {
        SharedPreferences.Editor editor = mSP.edit();
        editor.putBoolean("active", active);
        editor.putBoolean("flushOnStart", flushOnStart);
        editor.putBoolean("flushOnReconnect", flushOnReconnect);
        editor.putBoolean("flushOnCharge", flushOnCharge);
        editor.putLong("flushDelayInterval", flushDelayInterval);
        editor.putLong("flushMobileTrafficLimit", flushMobileTrafficLimit);
        editor.putInt("flushCacheLimit", flushCacheLimit);
        editor.putInt("neartimeInterval", neartimeInterval);
        editor.apply();
        readConfigFromPreference();
    }

    private void readConfigFromPreference() {
        emitterConfig.active = mSP.getBoolean("active", true);
        emitterConfig.flushOnStart = mSP.getBoolean("flushOnStart", true);
        emitterConfig.flushOnReconnect = mSP.getBoolean("flushOnReconnect", true);
        emitterConfig.flushOnCharge = mSP.getBoolean("flushOnCharge", true);
        emitterConfig.flushDelayInterval = mSP.getLong("flushDelayInterval", 30 * 60 * 1000);
        emitterConfig.flushCacheLimit = mSP.getInt("flushCacheLimit", 50);
        emitterConfig.flushMobileTrafficLimit = mSP.getLong("flushMobileTrafficLimit", 2 * 1024 * 1024);
        emitterConfig.neartimeInterval = mSP.getInt("neartimeInterval", 5);
    }

    /**
     * @param payload the payload to be added to
     *                the EventStore
     */
    public abstract void add(TrackerPayload payload);

    /**
     * @param payload the payload to be added to
     *                the EventStore realtime
     */
    public abstract void addRealtime(TrackerPayload payload);

    /**
     * @param payload the payload to be added to
     *                the EventStore neartime
     */
    public abstract void addNeartime(TrackerPayload payload);

    /**
     * Sends a batch in the database to the endpoint.
     */
    public abstract void flush();

    public abstract void updateEventSource(String sessionId, String eventSource);

    public abstract void setEncrypt(boolean encrypt);

    public abstract String getUMID();

}
