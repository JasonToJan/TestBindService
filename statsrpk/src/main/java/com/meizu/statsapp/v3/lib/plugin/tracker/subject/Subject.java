package com.meizu.statsapp.v3.lib.plugin.tracker.subject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.pm.PackageManager.GET_CONFIGURATIONS;

/**
 * Created by huchen on 16-8-1.
 */
public class Subject {
    private static String TAG = Subject.class.getSimpleName();
    private DeviceInfo deviceInfo;
    private AppInfo appInfo;
    private ConcurrentHashMap<String, Object> eventAttributePairs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> settingPropertyPairs = new ConcurrentHashMap<>();

    /**
     * Creates a Subject which will add extra data to each event.
     *
     * @param builder The builder that constructs a subject
     */
    private Subject(SubjectBuilder builder) {
        long t1 = System.currentTimeMillis();
        setDefaultDebug();
        setDefaultFlymeVersion();
        Logger.d(TAG, "##### Subject step 1, " + (System.currentTimeMillis() - t1));
        if (builder.context != null) {
            setContextualParams(builder.context);
            if (TextUtils.isEmpty(builder.replacePackage)) {
                setDefaultPackage(builder.context);
            } else {
                setReplacePackage(builder.context, builder.replacePackage);
            }
            setPkgKey(builder.pkgKey);
            setPkgType(builder.pkgType);
            setSdkVersion(builder.sdkVersion);
        }
        Logger.d(TAG, "##### Subject step 2, " + (System.currentTimeMillis() - t1));
        Logger.d(TAG, "Subject created successfully.");
    }

    /**
     * Builder for the Subject
     */
    public static class SubjectBuilder {
        private Context context = null; // Optional
        private String pkgKey;
        private int pkgType;
        private String sdkVersion;
        private String replacePackage;

        /**
         * @param context The android context to pass to the subject
         * @return itself
         */
        public SubjectBuilder context(Context context) {
            this.context = context;
            return this;
        }

        public SubjectBuilder pkgKey(String pkgKey) {
            this.pkgKey = pkgKey;
            return this;
        }

        public SubjectBuilder pkgType(int pkgType) {
            this.pkgType = pkgType;
            return this;
        }

        public SubjectBuilder sdkVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public SubjectBuilder replacePackage(String replacePackage) {
            this.replacePackage = replacePackage;
            return this;
        }

        /**
         * Creates a new Subject
         *
         * @return a new Subject object
         */
        public Subject build() {
            return new Subject(this);
        }
    }

    /**
     * Sets the contextually based parameters.
     *
     * @param context the android context
     */
    public void setContextualParams(Context context) {
        deviceInfo = new DeviceInfo.DeviceInfoBuilder().context(context).build();
        appInfo = new AppInfo.AppInfoBuilder().context(context).build();
    }


    /////////////////////////// Public Subject information setters

    /**
     * Sets the debug mode.
     * default false
     */
    private void setDefaultDebug() {
        addSettingProperty(Parameters.DEBUG, false);
    }

    /**
     * Sets the operating system version.
     */
    public void setDebug(boolean debug) {
        addSettingProperty(Parameters.DEBUG, debug);
    }

    /**
     * Sets the Flyme Version.
     */
    private void setDefaultFlymeVersion() {
        addSettingProperty(Parameters.FLYME_VER, Build.DISPLAY);
    }

    /**
     * Sets the Package.
     * default use context.getPackageName()
     */
    private void setDefaultPackage(Context context) {
        String packageName = context.getPackageName();
        addSettingProperty(Parameters.PKG_NAME, packageName);
        addSettingProperty(Parameters.PKG_VER, FlymeOSUtils.getPackageVersion(packageName, context));
        addSettingProperty(Parameters.PKG_VER_CODE, FlymeOSUtils.getPackageCode(packageName, context));
    }

    private void setReplacePackage(Context context, String replacePackage) {
        Logger.d(TAG, "### setReplacePackage, replacePackage: " + replacePackage);
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(replacePackage, GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            addSettingProperty(Parameters.PKG_NAME, packageInfo.packageName);
            addSettingProperty(Parameters.PKG_VER, packageInfo.versionName);
            addSettingProperty(Parameters.PKG_VER_CODE, packageInfo.versionCode);
            Logger.d(TAG, "setReplacePackage, packageInfo: " + packageInfo);
        } else {
            addSettingProperty(Parameters.PKG_NAME, "");
            addSettingProperty(Parameters.PKG_VER, "");
            addSettingProperty(Parameters.PKG_VER_CODE, 0);
        }
        String myPackageName = context.getPackageName();
        eventAttributePairs.put("_my_pkg_name_", myPackageName);
        eventAttributePairs.put("_my_pkg_ver_", FlymeOSUtils.getPackageVersion(myPackageName, context));
        eventAttributePairs.put("_my_pkg_ver_code_", "" + FlymeOSUtils.getPackageCode(myPackageName, context));
    }

    /**
     * Sets the PkgKey
     */
    private void setPkgKey(String pkgKey) {
        addSettingProperty(Parameters.PKG_KEY, pkgKey);
    }

    /**
     * Sets the PkgType
     */
    private void setPkgType(int pkgType) {
        addSettingProperty(Parameters.PKG_TYPE, pkgType);
    }

    /**
     * Sets SDK version
     */
    private void setSdkVersion(String sdkVersion) {
        addSettingProperty(Parameters.SDK_VER, sdkVersion); //插件的版本号
    }

    private void addSettingProperty(String key, String value) {
        if (key != null && !key.isEmpty() && value != null) {
            this.settingPropertyPairs.put(key, value);
        }
    }

    private void addSettingProperty(String key, int value) {
        if (key != null && !key.isEmpty()) {
            this.settingPropertyPairs.put(key, value);
        }
    }

    private void addSettingProperty(String key, boolean value) {
        if (key != null && !key.isEmpty()) {
            this.settingPropertyPairs.put(key, value);
        }
    }

    /////////////////////////////////eventAttributePairs

    /**
     * add eventAttributePairs
     *
     * @param key   a key
     * @param value a value
     */
    public void addEventAttributePairs(String key, Object value) {
        if (key != null && !key.isEmpty() && value != null) {
            eventAttributePairs.put(key, value);
        }
    }

    /**
     * remove all eventAttributePairs
     */
    public void clearEventAttributePairs() {
        eventAttributePairs = new ConcurrentHashMap<>();
    }

    /**
     * 某些公共参数在运行过程中可能会改变，每次都需要都新值
     *
     * @param context
     * @return
     */
    public Map<String, Object> getVolatileProperty(Context context) {
        Map<String, Object> volatileProperty = new HashMap<>();
        String[] imsis = FlymeOSUtils.getImsi(context);
        if (imsis != null) {
            if (imsis[0] != null) {
                volatileProperty.put(Parameters.IMSI1, imsis[0]);
            }
            if (imsis[1] != null) {
                volatileProperty.put(Parameters.IMSI2, imsis[1]);
            }
        }
        /**
         * Sets the Lla.
         */
        volatileProperty.put(Parameters.LLA, FlymeOSUtils.getLocationLanguage(context));

        //做下保护，对于随开机启动的app，开机时刻拿不到imei，后续要再尝试获取
        String deviceId = FlymeOSUtils.getDeviceId(context);
        volatileProperty.put(Parameters.IMEI, deviceId);
        String mac = NetInfoUtils.getMACAddress(context);
        volatileProperty.put(Parameters.MAC_ADDRESS, mac);

        Logger.d(TAG, "getVolatileProperty ..." + volatileProperty);
        return volatileProperty;
    }

    /**
     * @return the DeviceInfo pairs
     */
    public Map<String, Object> getDeviceInfo() {
        return deviceInfo.getMap();
    }

    public Map<String, Object> getAppInfo() {
        return appInfo.getMap();
    }

    public Map<String, Object> getEventAttributePairs() {
        return eventAttributePairs;
    }

    public Map<String, Object> getSettingProperty() {
        return settingPropertyPairs;
    }
}
