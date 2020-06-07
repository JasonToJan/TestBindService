package com.meizu.statsapp.v3;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jinhui on 17-3-3.
 */

public class InitConfig {
    public static boolean reportLocation = true; //采集地理经纬度
    public static boolean noBootUp = false;
    public static boolean offline = true; //和固件不绑定的离线模式
    public static boolean mainThreadInit = false;
    public static boolean noEncrypt = false; //本地数据不加密
    public static String replacePackage = "";
    public static boolean useInternationalDomain = false; //使用海外域名
    public static boolean sendEventSync = false; //同一时间是否只允许一个数据发送
    public static boolean printLog = false;//打印log

    public InitConfig() {}


    /**
     * 是否上报地理位置，默认是
     *
     * @param reportLocation 上报地理位置
     */
    public InitConfig setReportLocation(boolean reportLocation) {
        this.reportLocation = reportLocation;
        return this;
    }

    /**
     * 是否不生成bootUp事件
     *
     * @param noBootUp 不上报bootup
     */
    public InitConfig setNoBootUp(boolean noBootUp) {
        this.noBootUp = noBootUp;
        return this;
    }

    /**
     * 是否走和非固件集成的离线模式
     *
     * @param offline 离线模式
     */
    public InitConfig setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    /**
     * 是否由主线程初始化
     *
     * @param mainThreadInit 不加密
     */
    public InitConfig setMainThreadInit(boolean mainThreadInit) {
        this.mainThreadInit = mainThreadInit;
        return this;
    }

    /**
     * 本地数据库是否不加密
     *
     * @param noEncrypt 不加密
     */
    public InitConfig setNoEncrypt(boolean noEncrypt) {
        this.noEncrypt = noEncrypt;
        return this;
    }

    /**
     * 被替换的app应用包名，如果设置，那么不再上报接入sdk应用的包名/版本号，
     * 而是搜索替代的包名应用的包名/版本号
     *
     * @param pkgName 替代应用的包名
     */
    public InitConfig replacePackage(String pkgName) {
        Log.d("UsageStatsProxy3", "##### InitConfig replacePackage: " + pkgName);
        if (TextUtils.isEmpty(pkgName)) {
            throw new IllegalStateException("InitConfig - replacePackage can't be empty if set");
        }
        this.replacePackage = pkgName;
        return this;
    }

    /**
     * 使用海外域名
     *
     * @param use 是否使用
     */
    public InitConfig setUseInternationalDomain(boolean use) {
        this.useInternationalDomain = use;
        return this;
    }

    /**
     * 同一时间是否只允许一个数据发送
     *
     * @param yes 是否使用
     */
    public InitConfig setSendEventSync(boolean yes) {
        this.sendEventSync = yes;
        return this;
    }

    /**
     * 尽可能捕获一切异常，即使导致sdk完全无法工作
     * 已废弃，现在默认捕获一切了
     *
     * @param yes 是否使用
     */
    @Deprecated
    public InitConfig setCatchEveryExIfPossible(boolean yes) {
        return this;
    }

    /**
     * 该接口会输出日志文件到本地，谨调试时使用，业务方请勿私自调用。
     * @param yes
     * @return
     */
    public InitConfig setPrintLog(boolean yes) {
        this.printLog = yes;
        return this;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("reportLocation", reportLocation);
            object.put("noBootUp", noBootUp);
            object.put("offline", offline);
            object.put("mainThreadInit", mainThreadInit);
            object.put("noEncrypt", noEncrypt);
            object.put("replacePackage", replacePackage);
            object.put("useInternationalDomain", useInternationalDomain);
            object.put("sendEventSync", sendEventSync);
            object.put("printLog", printLog);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

}
