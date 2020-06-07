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

import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.utils.Preconditions;

/**
 * Base Event class which contains common
 * elements to all events:
 * - Custom Context: list of custom contexts or null
 * - Timestamp: user defined event timestamp or 0
 * - Event Id: a unique id for the event
 */
public abstract class Event {
    //events type
    public static final String EVENT_TYPE_PAGE = "page";
    public static final String EVENT_TYPE_LOG = "log";
    public static final String EVENT_TYPE_ACTION_X = "action_x";

    protected final String eventName;
    protected final String network;

    protected Event(String eventName, String network) {
        // Precondition checks
        Preconditions.checkNotNull(eventName);
        Preconditions.checkNotNull(network);
        this.eventName = eventName;
        this.network = network;
    }

    /**
     * @return the event name
     */
    public String getEventName() {
        return this.eventName;
    }

    /**
     * @return the event network
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Returns a TrackerPayload which can be stored into
     * the local database.
     *
     * @return the payload to be sent.
     */
    public abstract TrackerPayload generatePayload();

}
