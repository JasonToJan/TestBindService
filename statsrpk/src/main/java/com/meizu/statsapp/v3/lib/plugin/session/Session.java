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

package com.meizu.statsapp.v3.lib.plugin.session;

import android.content.Context;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A Session object which gets appended to each
 * event sent from the Tracker and changes based
 * on:
 * - Timeout of use while app is in foreground
 * - Timeout of use while app is in background
 * <p/>
 * Session data is maintained for the life of the
 * application being installed on a device.
 * <p/>
 * Essentially will update if it is not accessed within
 * a configurable timeout.
 */
public class Session {
    private static String TAG = Session.class.getSimpleName();

    // Session Variables
    private String id = null;
    private long startTime;

    /**
     * Creates a new Session object
     *
     * @param context           the android context
     */
    public Session(Context context) {
        this.id = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
        Logger.d(TAG, "Tracker Session Object created, id:" + this.id + ", startTime:" + this.startTime);
    }

    /**
     * Returns the properties for the session context.
     *
     * @return a map containing all session properties
     */
    public Map getProperties() {
        Logger.v(TAG, "Getting Session properties ...");
        Map<String, Object> sessionValues = new HashMap<>();
        sessionValues.put(Parameters.SESSION_ID, this.id);
        return sessionValues;
    }



    /**
     * @return the current session id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return the start time
     */
    public long getStartTime() {
        return this.startTime;
    }

}
