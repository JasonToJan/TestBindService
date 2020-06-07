package com.meizu.statsrpk;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

/**
 * Created by jinhui on 18-7-31.
 */

public class RpkEvent implements Parcelable {
    public String type;
    public String eventName;
    public String pageName;
    public Map properties;
    public String sessionId;

    public RpkEvent() {}

    protected RpkEvent(Parcel in) {
        type = in.readString();
        eventName = in.readString();
        pageName = in.readString();
        properties = in.readHashMap(String.class.getClassLoader());
        sessionId = in.readString();
    }

    public static final Creator<RpkEvent> CREATOR = new Creator<RpkEvent>() {
        @Override
        public RpkEvent createFromParcel(Parcel in) {
            return new RpkEvent(in);
        }

        @Override
        public RpkEvent[] newArray(int size) {
            return new RpkEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(eventName);
        dest.writeString(pageName);
        dest.writeMap(properties);
        dest.writeString(sessionId);
    }

    @Override
    public String toString() {
        return "[" + type + "," + eventName + "," + pageName + "," + properties + "," + sessionId + "]";
    }
}
