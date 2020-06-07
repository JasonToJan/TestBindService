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
 * Constructs a ActionX event object.
 */
public class ActionXEvent extends Event {
    private long time;
    private String page;
    private Map<String, String> properties;
    private Map<String, String> event_attrib;

    protected ActionXEvent(String eventName, String network) {
        super(eventName, network);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setEvent_attrib(Map<String, String> event_attrib) {
        this.event_attrib = event_attrib;
    }

    public TrackerPayload generatePayload() {
        TrackerPayload payload = new TrackerPayload();
        payload.add(Parameters.NAME, eventName);
        payload.add(Parameters.NETWORK, network);
        payload.add(Parameters.TYPE, EVENT_TYPE_ACTION_X);
        payload.add(Parameters.TIME, this.time);
        payload.add(Parameters.PAGE, this.page);
        payload.add(Parameters.VALUE, this.properties);
        payload.add(Parameters.EVENT_ATTRIB, this.event_attrib);
        return payload;
    }
}
