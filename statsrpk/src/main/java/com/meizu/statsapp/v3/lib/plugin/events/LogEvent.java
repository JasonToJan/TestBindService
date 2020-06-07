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

package com.meizu.statsapp.v3.lib.plugin.events;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;

import java.util.Map;

/**
 * Constructs a Log event object.
 */
public class LogEvent extends Event {

    public static final String LOG_PACKAGE = "com.meizu.uxip.log";

    private long time;
    private Map<String, String> properties;

    protected LogEvent(String eventName, String network) {
        super(eventName, network);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public TrackerPayload generatePayload() {
        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameters.NAME, eventName);
        payload.add(Parameters.NETWORK, network);
        payload.add(Parameters.TYPE, EVENT_TYPE_LOG);
        payload.add(Parameters.TIME, this.time);
        payload.add(Parameters.VALUE, this.properties);
        return payload;
    }

}
