package com.meizu.statsapp.v3.lib.plugin.net;

import android.content.Context;
import android.text.TextUtils;

import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsapp.v3.utils.reflect.ReflectHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinhui on 17-2-21.
 */

public class GslbWrapper {
    private final String TAG = "GslbWrapper";
    private static GslbWrapper sInstance;
    //private GslbManager manager;
    private Object mGslbManager; //Object must be GslbManager
    private Map<String, Object> mIPMap; //Object must be DomainIpInfo

    public static synchronized GslbWrapper getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GslbWrapper(context);
        }
        return sInstance;
    }

    private GslbWrapper(Context context) {
        try {
            mGslbManager = ReflectHelper.reflectConstructor("com.meizu.gslb2.GslbManager", new Class[]{Context.class}, new Object[]{context});
            if (mGslbManager != null) {
                Logger.d(TAG, "### gslb manager constructed");
            }
            mIPMap = new HashMap<>();
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    public String convert(String domain) {
        if (mGslbManager != null) {
            try {
                Object domainIpInfo = ReflectHelper.invoke(mGslbManager, "convert", new Class[]{String.class}, new Object[]{domain});
                if (domainIpInfo != null) {
                    String availableIp = (String) ReflectHelper.invoke(domainIpInfo, "getAvailableIp", null);
                    if (!TextUtils.isEmpty(availableIp)) {
                        mIPMap.put(availableIp, domainIpInfo);
                        Logger.d(TAG, "### gslb convert return: " + availableIp);
                        return availableIp;
                    }
                }
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        } else {
            Logger.d(TAG, "### gslb manager not found");
        }
        Logger.d(TAG, "### gslb convert return: " + domain);
        return domain;
    }

    public void onResponse(String ip, int code) {
        if (mGslbManager != null) {
            Logger.d(TAG, "### gslb  onResponse, ip: " + ip + ", code: " + code);
            Object domainIpInfo = mIPMap.get(ip);
            if (domainIpInfo != null) {
                try {
                    ReflectHelper.invoke(mGslbManager, "onResponseSuccess", new Class[]{domainIpInfo.getClass(), int.class}, new Object[]{domainIpInfo, code});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Logger.d(TAG, "### gslb manager not found");
        }
    }
}
