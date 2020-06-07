package com.meizu.statsapp.v3.lib.plugin.events;

import android.content.Context;

import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;

import java.util.Map;

/**
 * Created by huchen on 16-8-25.
 */
public class EventUtil {

    public static ActionXEvent buildActionXEvent(Context context,
                                          String eventName,
                                          String pageName,
                                          Map<String, String> properties) {
        String network;
        if (FlymeOSUtils.isBox(context)) {
            network = NetInfoUtils.getNetworkTypeForFlymeTv(context);
        } else {
            network = NetInfoUtils.getNetworkType(context);
        }
        ActionXEvent event = new ActionXEvent(eventName, network);
        event.setTime(System.currentTimeMillis());
        event.setPage(pageName);
        //0x01, 0x02, 0x03, \n是uxip数据分隔符，必须转义
        if (properties != null) {
            for (Map.Entry<String, String> element : properties.entrySet()) {
                String key = element.getKey();
                String value = element.getValue();
                if (value != null && value.indexOf('\n') >= 0) {
                    String replaceValue = value.replace('\n', (char) 0x00);
                    element.setValue(replaceValue);
                }
            }
        }
        event.setProperties(properties);
        event.setEvent_attrib(null);
        return event;
    }

    public static LogEvent buildLogEvent(Context context,
                                      String logName,
                                      Map<String, String> properties) {
        String network;
        if (FlymeOSUtils.isBox(context)) {
            network = NetInfoUtils.getNetworkTypeForFlymeTv(context);
        } else {
            network = NetInfoUtils.getNetworkType(context);
        }
        LogEvent event = new LogEvent(logName, network);
        event.setTime(System.currentTimeMillis());
        //0x01, 0x02, 0x03, \n是uxip数据分隔符，必须转义
        if (properties != null) {
            for (Map.Entry<String, String> element : properties.entrySet()) {
                String key = element.getKey();
                String value = element.getValue();
                if (value != null && value.indexOf('\n') >= 0) {
                    String replaceValue = value.replace('\n', (char) 0x00);
                    element.setValue(replaceValue);
                }
            }
        }
        event.setProperties(properties);
        return event;
    }

    public static PageEvent buildPageEvent(Context context,
                                       String pageName,
                                       String launch,
                                       String terminate) {
        String network;
        if (FlymeOSUtils.isBox(context)) {
            network = NetInfoUtils.getNetworkTypeForFlymeTv(context);
        } else {
            network = NetInfoUtils.getNetworkType(context);
        }
        PageEvent event = new PageEvent(pageName, network);
        event.setLaunch(launch);
        event.setTerminate(terminate);
        return event;
    }
}
