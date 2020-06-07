package com.meizu.statsapp.v3.lib.plugin.tracker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by huchen on 16-10-21.
 */

public class EventFilter {
    private String name;
    private boolean active;
    private boolean realtime;
    private boolean neartime;

    public EventFilter(String name, boolean active, boolean realtime, boolean neartime) {
        this.name = name;
        this.active = active;
        this.realtime = realtime;
        this.neartime = neartime;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public boolean isNeartime() {
        return neartime;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("e_name", name);
            object.put("e_active", active);
            object.put("e_realtime", realtime);
            object.put("e_neartime", neartime);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static EventFilter fromString(String s) {
        try {
            JSONObject object = new JSONObject(s);
            return new EventFilter(object.getString("e_name"), object.getBoolean("e_active"), object.getBoolean("e_realtime"), object.getBoolean("e_neartime"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
