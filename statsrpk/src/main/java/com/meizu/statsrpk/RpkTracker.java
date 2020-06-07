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

package com.meizu.statsrpk;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds a Tracker object which is used to
 * send events to a Snowplow Collector.
 */
public class RpkTracker {
    private final static String TAG = RpkTracker.class.getSimpleName();
    private RpkEmitter rpkEmitter;
    private RpkInfo rpkInfo;
    private SharedPreferences sp;
    private RpkInstanceImpl rpkInstanceImpl;
    /**
     * Creates a new Snowplow Tracker.
     *
     * @param rpkEmitter
     */
    public RpkTracker(Context context, RpkEmitter rpkEmitter, RpkInfo rpkInfo) {
        this.rpkEmitter = rpkEmitter;
        this.rpkInfo = rpkInfo;
        sp = context.getSharedPreferences(RpkConstants.SP_FILE_RPK_CONFIG_PREFIX + rpkInfo.rpkPkgName, Context.MODE_PRIVATE);
        Logger.v(TAG, "RpkTracker created successfully.");
    }

    void init(RpkInstanceImpl rpkInstanceImpl) {
        this.rpkInstanceImpl = rpkInstanceImpl;
    }

    public void trackEvent(String eventName, String pageName, Map properties) {
        if (!sp.getBoolean("active", true)) {
            return;
        }
        int configType = filterEvent(eventName);
        if (configType == 0) {
            return;
        }
        RpkEvent rpkEvent = new RpkEvent();
        rpkEvent.type = "action_x";
        rpkEvent.eventName = eventName;
        rpkEvent.pageName = pageName;
        rpkEvent.properties = properties;
        rpkEvent.sessionId = rpkInstanceImpl.getRpkPageController().getOrGenerateSessionId();
        rpkEmitter.track(rpkEvent, rpkInfo);
    }

    public void trackPage(String pageName, long start, long end, long duration2) {
        if (!sp.getBoolean("active", true)) {
            return;
        }
        int configType = filterEvent(pageName);
        if (configType == UxipConstants.EVENT_INACTIVE) {
            return;
        }
        RpkEvent rpkEvent = new RpkEvent();
        rpkEvent.type = "page";
        rpkEvent.eventName = pageName;
        rpkEvent.pageName = pageName;
        Map<String, String> properties = new HashMap<>();
        properties.put("start", String.valueOf(start));
        properties.put("end", String.valueOf(end));
        properties.put("duration2", String.valueOf(duration2));
        rpkEvent.properties = properties;
        rpkEvent.sessionId = rpkInstanceImpl.getRpkPageController().getOrGenerateSessionId();
        rpkEmitter.track(rpkEvent, rpkInfo);
    }

    private int filterEvent(String eventName) {
        String event_filters = sp.getString("event_filters", "");
        if (!TextUtils.isEmpty(event_filters)) {
            String[] arr = event_filters.split(",");
            for (int i = 0; i < arr.length; i++) {
                String filter = arr[i];
                String event_name = filter.split(":")[0];
                int type = Integer.parseInt(filter.split(":")[1]);
                if (event_name.equals(eventName)) {
                    return type;
                }
            }
        }
        return 1;
    }
}
