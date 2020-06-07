package com.meizu.statsapp.v3.lib.plugin.emitter.remote;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.emitter.Emitter;
import com.meizu.statsapp.v3.lib.plugin.events.Event;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONStringer;

import java.util.Map;

/**
 * Created by huchen on 16-8-22.
 * 仅仅是远端的代理，不具体做事
 */
public class V2ProviderEmitter extends Emitter {
    private final static String TAG = V2ProviderEmitter.class.getSimpleName();

    public V2ProviderEmitter(Context context, String pkgKey) {
        super(context, pkgKey);
    }

    private static final String AUTHORITY = "com.meizu.usagestats";
    private static final String TABLE_EVENT = "event";
    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";
    private static final String EVENT_CONTENT_URI = CONTENT_AUTHORITY_SLASH + TABLE_EVENT;

    @Override
    public void init() {
    }

    @Override
    public void add(final TrackerPayload payload) {
        Logger.d(TAG, "add payload:" + payload.toString());
        if (emitterConfig.isActive()) {
            try {
                ContentValues contentValues = createEventValues(payload);
                Uri uri = Uri.parse(EVENT_CONTENT_URI);
                Uri retUri = mContext.getContentResolver().insert(uri, contentValues);
                if (retUri == null) {
                    contentValues = createOldEventValues(payload);
                    retUri = mContext.getContentResolver().insert(uri, contentValues);
                }
                Logger.d(TAG, "insert to experienceDataSync, retUrl: " + retUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addRealtime(final TrackerPayload payload) {
        Logger.d(TAG, "addRealtime payload:" + payload.toString());
        if (emitterConfig.isActive()) {
            add(payload);
        }
    }

    @Override
    public void addNeartime(final TrackerPayload payload) {
        Logger.d(TAG, "addNeartime payload:" + payload.toString());
        if (emitterConfig.isActive()) {
            addRealtime(payload);
        }
    }

    private static final String EVENT_NAME = "name";
    private static final String EVENT_TYPE = "type";
    private static final String EVENT_SESSIONID = "sessionid";
    private static final String EVENT_PACKAGE = "package";
    private static final String EVENT_TIME = "time";
    private static final String EVENT_PAGE = "page";
    private static final String EVENT_PROPERTIES = "properties";
    private static final String EVENT_NETWORK = "network";
    private static final String EVENT_CHANNEL = "channel";
    private static final String EVENT_FLYME_VERSION = "flyme_version";
    private static final String EVENT_PACKAGE_VERSION = "package_version";
    private static final String EVENT_SOURCE = "event_source";

    public ContentValues createEventValues(TrackerPayload payload) {
        ContentValues values = createOldEventValues(payload);
        Object source = payload.getMap().get(Parameters.SOURCE);
        if (source != null && source instanceof String) {
            values.put(EVENT_SOURCE, (String) source);
        }
        return values;
    }

    public ContentValues createOldEventValues(TrackerPayload payload) {
        if (null == payload || payload.getMap() == null) {
            return null;
        }
        ContentValues values = new ContentValues();

        Object eventName = payload.getMap().get(Parameters.NAME);
        if (eventName != null && eventName instanceof String) {
            values.put(EVENT_NAME, (String) eventName);
        }
        Object pkgName = payload.getMap().get(Parameters.PKG_NAME);
        if (pkgName != null && pkgName instanceof String) {
            values.put(EVENT_PACKAGE, (String) pkgName);
        }
        Object pkgVer = payload.getMap().get(Parameters.PKG_VER);
        if (pkgVer != null && pkgVer instanceof String) {
            values.put(EVENT_PACKAGE_VERSION, (String) pkgVer);
        }
        Object type = payload.getMap().get(Parameters.TYPE);
        if (type != null && type instanceof String) {
            if (type.equals(Event.EVENT_TYPE_ACTION_X)) {
                values.put(EVENT_TYPE, 1);
            } else if (type.equals(Event.EVENT_TYPE_PAGE)) {
                values.put(EVENT_TYPE, 2);
            } else if (type.equals(Event.EVENT_TYPE_LOG)) {
                values.put(EVENT_TYPE, 3);
            }
        }
        Object sid = payload.getMap().get(Parameters.SESSION_ID);
        if (sid != null && sid instanceof String) {
            values.put(EVENT_SESSIONID, (String) sid);
        }
        Object time = payload.getMap().get(Parameters.TIME);
        if (time != null && time instanceof Long) {
            values.put(EVENT_TIME, (Long) time);
        }
        Object page = payload.getMap().get(Parameters.PAGE);
        if (page != null && page instanceof String) {
            values.put(EVENT_PAGE, (String) page);
        }
        Object properties = payload.getMap().get(Parameters.VALUE); //事件的自定义属性
        if (properties != null && properties instanceof Map) {
            String propertiesStr = getPropertiesToJSONString((Map) properties);
            if (!TextUtils.isEmpty(propertiesStr)) {
                values.put(EVENT_PROPERTIES, propertiesStr);
            }
        }
        Object network = payload.getMap().get(Parameters.NETWORK);
        if (network != null && network instanceof String) {
            values.put(EVENT_NETWORK, (String) network);
        }
        Object channel = payload.getMap().get(Parameters.CHANNEL_ID);
        if (channel != null && channel instanceof String) {
            values.put(EVENT_CHANNEL, (String) channel);
        }
        Object flymeVer = payload.getMap().get(Parameters.FLYME_VER);
        if (flymeVer != null && flymeVer instanceof String) {
            values.put(EVENT_FLYME_VERSION, (String) flymeVer);
        }

        return values;
    }

    private String getPropertiesToJSONString(Map properties) {
        String result = "";
        try {
            if (properties instanceof Map && ((Map) properties).size() > 0) {
                JSONStringer stringer = new JSONStringer();
                stringer.object();
                for (Map.Entry<String, String> entry : ((Map<String, String>) properties).entrySet()) {
                    stringer.key(entry.getKey()).value(entry.getValue());
                }
                stringer.endObject();
                result = stringer.toString();
            } else {
                result = properties.toString();
            }
        } catch (JSONException e) {
        } catch (Exception e) {
        }
        return result;
    }

    @Override
    public void flush() {
    }

    @Override
    public void updateEventSource(String sessionId, String eventSource) {
    }

    @Override
    public void setEncrypt(boolean encrypt) {

    }

    @Override
    public String getUMID() {
        return "";
    }

}
