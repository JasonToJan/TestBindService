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

package com.meizu.statsapp.v3.lib.plugin.payload;

import android.os.Parcel;
import android.os.Parcelable;

import com.meizu.statsapp.v3.lib.plugin.utils.Utils;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns a standard Tracker Payload consisting of
 * many key - pair values.
 */
public class TrackerPayload implements Parcelable {

    private final static String TAG = TrackerPayload.class.getSimpleName();
    private HashMap<String, Object> payloadMap;

    public TrackerPayload() {
        payloadMap = new HashMap<>();
    }

    public void add(String key, Object value) {
        if (value == null) {
            Logger.v(TAG, "The keys value is empty, returning without add");
            return;
        }
//        Logger.v(TAG, "Adding new kv pair: " + key + "->" + value);
        payloadMap.put(key, value);
    }

    public void remove(String key) {
        Logger.v(TAG, "Removing key: " + key);
        payloadMap.remove(key);
    }

    public void addMap(Map<String, Object> map) {
        if (map == null) {
            Logger.v(TAG, "Map passed in is null, returning without adding map.");
            return;
        }
//        Logger.v(TAG, "Adding new map: %s", map);
        payloadMap.putAll(map);
    }

    public Map getMap() {
        return payloadMap;
    }

    @Override
    public String toString() {
        String ret = "";
        try {
            ret = (Utils.mapToJSONObject(payloadMap)).toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static TrackerPayload fromString(String s) {
        try {
            TrackerPayload trackerPayload = new TrackerPayload();
            trackerPayload.addMap(Utils.jsonObjectToMap(new JSONObject(s)));
            return trackerPayload;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Parcelling part
    public TrackerPayload(Parcel in) {
        payloadMap = in.readHashMap(HashMap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(payloadMap);
    }

    public static final Creator<TrackerPayload> CREATOR = new Creator<TrackerPayload>() {
        @Override
        public TrackerPayload createFromParcel(Parcel in) {
            return new TrackerPayload(in);
        }

        @Override
        public TrackerPayload[] newArray(int size) {
            return new TrackerPayload[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
