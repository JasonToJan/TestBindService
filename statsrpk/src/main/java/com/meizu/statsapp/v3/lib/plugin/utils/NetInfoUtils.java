package com.meizu.statsapp.v3.lib.plugin.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.meizu.statsapp.v3.utils.CommonUtils;
import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsapp.v3.utils.reflect.SystemProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jinhui on 17-11-16.
 */

public class NetInfoUtils {
    private final static String TAG = "NetInfoUtils";

    /**
     * Checks whether or not the device
     * is online and able to communicate
     * with the outside world.
     *
     * @param context the android context
     * @return whether the tracker is online
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            boolean connected = ni != null && ni.isConnected();
            Logger.d(TAG, "isOnline:" + connected);
            return connected;
        } catch (SecurityException e) {
            Logger.e(TAG, "Security exception:" + e.toString());
            return true;
        }
    }

    public static boolean isWiFiWorking(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null == connectivityManager) {
                return false;
            }
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (null != wifiInfo && NetworkInfo.State.CONNECTED == wifiInfo.getState()) {
                return true;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return false;
    }

    /*
   *  获取设备mac地址；
   */

    public static String getMACAddress(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.meizu.toolsfortablet", 0);
        if (sharedPreferences.contains("mac_address")) {
            sharedPreferences.edit().remove("mac_address").apply();
        }
        String macAddr = "";
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //Android 6.0以下
                macAddr = getMacAndroid6_(context);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < 24) { //Android 6+, Android 7以下
                macAddr = getMacAndroid6_7(context);
            } else if (Build.VERSION.SDK_INT >= 24) {
                macAddr = getMacAndroid7(context);
            }
        } catch (Exception ignore) {
            Logger.w(TAG, "Exception: " + ignore.toString() + " - Cause: " + ignore.getCause());
        }
        return macAddr;
    }

    @SuppressWarnings({"MissingPermission"})
    private static String getMacAndroid6_(Context context) {
        String ret = "";
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (null != wifiManager) {
            try {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                ret = wifiInfo == null ? null : wifiInfo.getMacAddress();
            } catch (SecurityException e) {
                Logger.e(TAG, "Security exception:" + e.toString());
            }
        }
        Logger.d(TAG, "6_ " + ret);
        return ret;
    }

    private static String getMacAndroid6_7(Context context) {
        String ifName = SystemProperties.get("wifi.interface", "wlan0");
        String ret = getMacAddressWithIfName(ifName);
        Logger.d(TAG, "6_7 " + ret);
        return ret;
    }

    @TargetApi(24)
    private static String getMacAndroid7(Context context) {
//        return FlymeOSUtils.readTestIds()[2];
        String ret = "";
        Logger.d(TAG, "7_ 1. " + MacAndroid7.getMacAddress());
        Logger.d(TAG, "7_ 2. " + MacAndroid7.getMachineHardwareAddress());
        Logger.d(TAG, "7_ 3. " + MacAndroid7.getLocalMacAddressFromBusybox());
        ret = MacAndroid7.getMacAddress();
        if (TextUtils.isEmpty(ret)) {
            ret = MacAndroid7.getMachineHardwareAddress();
        }
        if (TextUtils.isEmpty(ret)) {
            ret = MacAndroid7.getLocalMacAddressFromBusybox();
        }
        return ret;
    }

    private static String getMacAddressWithIfName(String name) {
        String address = "";
        InputStream in = null;
        try {
            in = new FileInputStream("/sys/class/net/" + name + "/address");
            byte[] buf = new byte[1024];
            int len = in.read(buf);
            if (len > 0) {
                address = (new String(buf)).trim();
            }
        } catch (FileNotFoundException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        } catch (IOException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        } finally {
            CommonUtils.closeQuietly(in);
        }
        return address.toUpperCase();
    }

    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        return "wifi";
                    } else {
                        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS
                                || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
                            return "2g";
                        } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                            return "4g";
                        } else {
                            return "3g";
                        }
                    }
                } else {
                    return "off";
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return "unknown";
    }

    public static String getNetworkTypeForFlymeTv(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                        return "ethernet";
                    } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        return "wifi";
                    }
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return "unknown";
    }
}
