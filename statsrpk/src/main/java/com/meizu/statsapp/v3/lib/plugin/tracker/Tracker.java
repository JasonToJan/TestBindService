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

package com.meizu.statsapp.v3.lib.plugin.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.emitter.Emitter;
import com.meizu.statsapp.v3.lib.plugin.events.Event;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.sdk.SDKInstanceImpl;
import com.meizu.statsapp.v3.lib.plugin.session.SessionController;
import com.meizu.statsapp.v3.lib.plugin.tracker.subject.Subject;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Builds a Tracker object which is used to
 * send events to a Snowplow Collector.
 */
public class Tracker {
    private final static String TAG = Tracker.class.getSimpleName();
    private Emitter emitter;
    private Subject subject;
    private Context context;
    private boolean debug;
    private SDKInstanceImpl sdkInstance;

    private Map<String, EventFilter> eventFilterMap;
    private SharedPreferences sp;

    public void init(SDKInstanceImpl sdkInstance) {
        this.sdkInstance = sdkInstance;
    }

    /**
     * Builder for the Tracker
     */
    @SuppressWarnings("unchecked")
    public static class TrackerBuilder {
        private final Emitter emitter; // Required
        private final Context context; // Required
        private Subject subject = null; // Optional
        private boolean debug;

        /**
         * @param emitter Emitter to which events will be sent
         * @param context The Android application context
         */
        public TrackerBuilder(Emitter emitter, Context context) {
            this.emitter = emitter;
            this.context = context;
        }

        /**
         * @param subject Subject to be tracked
         * @return itself
         */
        public TrackerBuilder subject(Subject subject) {
            this.subject = subject;
            return this;
        }

        public TrackerBuilder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * Creates a new Tracker or throws an
         * Exception of we cannot find a suitable
         * extensible class.
         *
         * @return the new Tracker object
         */
        public Tracker build() {
            return new Tracker(this);
        }
    }

    /**
     * Creates a new Snowplow Tracker.
     *
     * @param builder The builder that constructs a tracker
     */
    public Tracker(TrackerBuilder builder) {
        this.emitter = builder.emitter;
        this.subject = builder.subject;
        this.context = builder.context;
        this.debug = builder.debug;
        this.subject.setDebug(builder.debug);
        this.eventFilterMap = new HashMap<>();
        this.sp = builder.context.getSharedPreferences("com.meizu.statsapp.v3.event_filter", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sp.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            EventFilter f = EventFilter.fromString(entry.getValue().toString());
            if (f != null) {
                eventFilterMap.put(f.getName(), f);
            }
        }
        Logger.v(TAG, "Tracker created successfully.");
    }

    /**
     * Builds and adds a finalized payload by adding in extra
     * information to the payload:
     * - The event contexts
     * - The Tracker Subject
     * - The Tracker parameters
     *
     * @param payload Payload the raw event payload to be
     *                decorated.
     */
    private void addCommon(TrackerPayload payload) {
        // Add session context
        SessionController sessionController = sdkInstance.getSessionController();
        if (sessionController != null) {
            payload.add(Parameters.SESSION_ID, sessionController.getOrGenerateSessionId());
            payload.add(Parameters.SOURCE, sessionController.getSource());
        }

        // If there is a subject present for the Tracker add it
        if (subject != null) {
            payload.addMap(subject.getDeviceInfo());
            payload.addMap(subject.getAppInfo());
            payload.addMap(subject.getSettingProperty());
            payload.addMap(subject.getVolatileProperty(context));
            payload.add("event_attrib", subject.getEventAttributePairs());
        }

        //location
        LocationFetcher locationFetcher = sdkInstance.getLocationFetcher();
        if (locationFetcher != null) {
            Location location = locationFetcher.getLocation();
            if (location != null) {
                payload.add(Parameters.LONGITUDE, location.getLongitude());
                payload.add(Parameters.LATITUDE, location.getLatitude());
                payload.add(Parameters.LOC_TIME, location.getTime());
            } else {
                payload.add(Parameters.LONGITUDE, 0);
                payload.add(Parameters.LATITUDE, 0);
                payload.add(Parameters.LOC_TIME, 0);
            }
        }
    }

    public void setEventFilterMap(Map<String, EventFilter> filterMap) {
        eventFilterMap = filterMap;
        SharedPreferences.Editor edit = sp.edit();
        edit.clear();
        Iterator<Map.Entry<String, EventFilter>> entries = eventFilterMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, EventFilter> entry = entries.next();
            edit.putString(entry.getKey(), entry.getValue().toString());
        }
        edit.commit();
    }

    /**
     * @return the trackers subject object
     */
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * @return the emitter associated with the tracker
     */
    public Emitter getEmitter() {
        return this.emitter;
    }

    public void track(Event event) {
        track(event, UxipConstants.SEND_NORMA);
    }

    //发送方式， 1-非实时批量， 2-单个实时， 3-批量delay实时
    public void track(Event event, int sendType) {
        TrackerPayload payload = event.generatePayload();
        addCommon(payload);
        send(payload, sendType);
    }

    public void trackX(Event event, int sendType, Map<String, Object> replaceMap) {
        TrackerPayload payload = event.generatePayload();
        addCommon(payload);
        if (replaceMap != null) {
            for (Map.Entry<String, Object> entry : replaceMap.entrySet()) {
                payload.add(entry.getKey(), entry.getValue());
            }
        }
        send(payload, sendType);
    }

    private void send(TrackerPayload payload, int sendType) {
        int configType = filterEvent(payload);
        if (configType == UxipConstants.EVENT_INACTIVE) {
            return;
        }
        int type = sendType > configType ? sendType : configType;
        if (debug) {
            type = UxipConstants.SEND_REALTIME;
        }
        if (type == UxipConstants.SEND_REALTIME) {
            emitter.addRealtime(payload);
        } else if (type == UxipConstants.SEND_NEARTIME) {
            emitter.addNeartime(payload);
        } else {
            emitter.add(payload);
        }
    }

    private int filterEvent(TrackerPayload payload) {
        if (eventFilterMap != null) {
            EventFilter eventFilter = eventFilterMap.get(payload.getMap().get(Parameters.NAME));
            if (eventFilter != null) {
                if (!eventFilter.isActive()) {
                    Logger.i(TAG, "eventFilterMap, Not Tracking for false active");
                    return UxipConstants.EVENT_INACTIVE;
                } else {
                    if (eventFilter.isRealtime()) {
                        return UxipConstants.SEND_REALTIME;
                    } else if (eventFilter.isNeartime()) {
                        return UxipConstants.SEND_NEARTIME;
                    }
                }
            }
        }
        return UxipConstants.SEND_NORMA;
    }

    public void updateSessionSource(String sessionId, String source) {
        emitter.updateEventSource(sessionId, source);
    }
}
