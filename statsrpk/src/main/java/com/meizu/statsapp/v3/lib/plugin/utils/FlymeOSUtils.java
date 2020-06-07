
package com.meizu.statsapp.v3.lib.plugin.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsapp.v3.utils.reflect.ReflectHelper;
import com.meizu.statsapp.v3.utils.reflect.SystemProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

/**
 * @author suojingchao
 */
public class FlymeOSUtils {
    private static final String TAG = "FlymeOSUtils";

    private static String sIMEI = "";
    private static String sSN = "";
    private static String sDeviceId = "";
    private static String sBUILD_MASK = "";
    private static String sDisplaySize = "";
    private static Boolean sIsBrandMeizu = null;
    private static Boolean sIsTablet = null;
    private static Boolean sIsBox = null;
    private static String sPRODUCT_MODEL;
    private static Boolean sIsRoot = null;
    private static Boolean sIsProductInternational = null;

    /*
     *  判断设备是否是平板
     */
    public static boolean isTablet(Context context) {
        if (sIsTablet != null) {
            return sIsTablet;
        }
        try {
            String product = SystemProperties.get("ro.target.product");
            return sIsTablet = !TextUtils.isEmpty(product) && "tablet".equalsIgnoreCase(product);
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return false;
    }

    /*
     *  判断设备是否是tv
     */
    public static boolean isBox(Context context) {
        if (sIsBox != null) {
            return sIsBox;
        }
        try {
            String product = SystemProperties.get("ro.target.product");
            return sIsBox = !TextUtils.isEmpty(product) && "box".equalsIgnoreCase(product);
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return false;
    }

    /*
     *  获取设备唯一标识id：如果是手机，返回imei，如果是pad 或tv，使用sn+mac
     */
    public static String getDeviceId(Context context) {
        //return readTestIds()[0];
        if (isTablet(context) || isBox(context)) {//平板或tv直接返回
            return "";
        } else {// 手机
            if (TextUtils.isEmpty(sDeviceId)) {
                sDeviceId = getIMEI(context);
                SharedPreferences sharedPreferences = context.getSharedPreferences("com.meizu.toolsfortablet", 0);
                if (TextUtils.isEmpty(sDeviceId)) { //如果从telephone manager获取成功，更新pref缓存。如果失败使用缓存
                    return sharedPreferences.getString("deviceId", "");
                } else {
                    sharedPreferences.edit().putString("deviceId", sDeviceId).apply();
                }
            }
            return sDeviceId;
        }
    }

    /*
     *  获取设备imei；如果开机过程中去获取，可能会获取不到
     */
    private static String getIMEI(Context context) {
        if (TextUtils.isEmpty(sIMEI)) {
            // android L
            try {
                final String MZ_T_M = "android.telephony.MzTelephonyManager";
                final String METHOD_GET_DEVICE_ID = "getDeviceId";
                sIMEI = (String) ReflectHelper.invokeStatic(MZ_T_M, METHOD_GET_DEVICE_ID, null, null);
            } catch (Exception e) {
                Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
            }

            if (TextUtils.isEmpty(sIMEI)) {
                // Flyme 4.0
                try {
                    final String MZ_T_M = "com.meizu.telephony.MzTelephonymanager";
                    final String METHOD_GET_DEVICE_ID = "getDeviceId";
                    sIMEI = (String) ReflectHelper.invokeStatic(MZ_T_M, METHOD_GET_DEVICE_ID, new Class<?>[]{Context.class, int.class}, new Object[]{context, 0});
                } catch (Exception ignore) {
                    Logger.w(TAG, "Exception: " + ignore.toString() + " - Cause: " + ignore.getCause());
                }
            }

            // 这个处理是非必须的，因为METHOD_GET_DEVICE_ID本身做了这个处理；为了运行在其它手机平台，这里兼容处理
            if (TextUtils.isEmpty(sIMEI)) {
                try {
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    sIMEI = tm.getDeviceId();
                } catch (Exception ignore) {
                    Logger.w(TAG, "Exception: " + ignore.toString() + " - Cause: " + ignore.getCause());
                }
            }
        }
        return sIMEI;
    }

    /*
     *  获取设备sn；
     */
    public static String getSN() {
        if (!TextUtils.isEmpty(sSN)) {
            return sSN;
        }

        if (Build.VERSION.SDK_INT >= 26) {
            try {
                sSN = (String) (ReflectHelper.invokeStatic("android.os.Build", "getSerial", new Object[]{}));
            } catch (Exception e) {
                Logger.e(TAG, e.getMessage());
            }
        } else {
            String sn = SystemProperties.get("ro.serialno");
            if (!TextUtils.isEmpty(sn)) {
                sSN = sn;
            }
        }
        return sSN;
    }

    public static String getBuildMask() {
        if (TextUtils.isEmpty(sBUILD_MASK)) {
            sBUILD_MASK = SystemProperties.get("ro.build.mask.id");
        }
        return sBUILD_MASK;
    }

    public static String getDisplaySize(Context context) {
        if (TextUtils.isEmpty(sDisplaySize)) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (display != null) {
                Point size = new Point();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    display.getSize(size);
                }
                int width = size.x;
                int height = size.y;
                sDisplaySize = width + "." + height;
            }
        }
        return sDisplaySize;
    }

    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 判断设备是否Flyme OS
     */
    public static boolean isBrandMeizu() {
        if (sIsBrandMeizu != null) {
            return sIsBrandMeizu;
        }
        try {
            String model = SystemProperties.get("ro.meizu.product.model");
            return sIsBrandMeizu = (!TextUtils.isEmpty(model)) || "meizu".equalsIgnoreCase(Build.BRAND) || "22c4185e".equalsIgnoreCase(Build.BRAND);
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return false;
    }

    /**
     * Get country.
     *
     * @param context
     */
    public static String getCountry(Context context) {
        try {
            if (null != context && null != context.getResources().getConfiguration()
                    && null != context.getResources().getConfiguration().locale) {
                return context.getResources().getConfiguration().locale.getCountry();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return "";
    }

    /**
     * Get flyme uid.
     * 获取账号权限需知：
     * 1、targetSdkVersion<26，判断逻辑和8.0之前的判断逻辑是一样的，会检查 Manifest.permission.GET_ACCOUNTS 的权限
     * （android6.0及以上是运行时权限，需动态申请）
     * 2、有权限 Manifest.permission.GET_ACCOUNTS_PRIVILEGED，只有priv/app目录下的app声明之后才会授予此权限
     * （不管targetSdkVersion<26，还是>=26，有此权限，都有getAccountsXXX的权限 ）
     * 3、和注册此帐号类型的authenticator app签名一致 Tip:魅族帐号中心用的也是platform签名
     * （同第二种情况，与targetSdkVersion无关，只要签名一致，即可在8.0的机器上有权限调用getAccountsXXX）
     * 4、caller app有权限Manifest.permission.READ_CONTACTS，该accountType的authenticator app要有Manifest.permission.WRITE_CONTACTS
     * （这两个都是dangerous permission，需要动态申请）
     *  根据Requesting Permissions才发现，read contacts，write contacts和get account这三个权限是属于同一个权限组的
     * @param context
     */
    @SuppressWarnings({"MissingPermission"})
    public static String getFlymeUid(Context context) {
        try {
            AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
            Account[] account = am.getAccountsByType("com.meizu.account");
            if (null != account && account.length > 0 && null != account[0]) {
                return account[0].name;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return "";
    }

    /**
     * Get the MCC+MNC (mobile country code + mobile network code) of the
     * provider of the SIM.
     *
     * @param context
     */
    public static String getOperator(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm && TelephonyManager.SIM_STATE_READY == tm.getSimState()) {
                return tm.getSimOperator();
            }
        } catch (Exception e) {
            Logger.e(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return "";
    }

    /**
     * 判断当前手机是否有ROOT权限
     *
     * @return
     */
    @SuppressWarnings("all")
    public static boolean isRoot(Context context) {
        if (sIsRoot != null) {
            Logger.d(TAG, "isRoot = " + sIsRoot);
            return sIsRoot;
        }
        try {
            if (new File("/system/bin/su").exists() || (new File("/system/xbin/su").exists())) {
                sIsRoot = true;
                Logger.d(TAG, "isRoot = " + sIsRoot);
                return sIsRoot;
            } else {
                Object deviceStateManager =
                        context.getSystemService("device_states");
                if (null == deviceStateManager) {
                    //在这里再获取一次就能兼容以前版本了
                    deviceStateManager =
                            context.getSystemService("deivce_states");
                    if (null == deviceStateManager) {
                        sIsRoot = false;
                        Logger.d(TAG, "isRoot = " + sIsRoot);
                        return sIsRoot;
                    }
                }
                Integer res = (Integer) ReflectHelper.invoke(deviceStateManager, "doCheckState", new Class[]{Integer.class}, new Object[]{1});
                if (null != res && 1 == res.intValue()) {
                    sIsRoot = true;
                    Logger.d(TAG, "isRoot = " + sIsRoot);
                    return sIsRoot;
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, "Exception:" + e.toString() + " - Cause:" + e.getCause());
        }

        sIsRoot = false;
        Logger.d(TAG, "isRoot = " + sIsRoot);
        return sIsRoot;
    }


    public static String getProductModel() {
        if (TextUtils.isEmpty(sPRODUCT_MODEL)) {
            sPRODUCT_MODEL = SystemProperties.get("ro.meizu.product.model");
            if (TextUtils.isEmpty(sPRODUCT_MODEL)) {
                sPRODUCT_MODEL = Build.MODEL;
            }
        }
        return sPRODUCT_MODEL;
    }

    public static String getLocationLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        String country = locale.getCountry();
        language = (null == language ? "" : language);
        country = (null == country ? "" : country);
        return language + "_" + country;
    }

    public static String getPackageVersion(String packageName, Context context) {
        if (TextUtils.isEmpty(packageName) || null == context) {
            return "";
        }
        PackageManager pm = context.getPackageManager();
        if (null == pm) {
            return "";
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        if (null != packageInfo) {
            return packageInfo.versionName;
        }
        return "";
    }

    public static int getPackageCode(String packageName, Context context) {
        if (TextUtils.isEmpty(packageName) || null == context) {
            return 0;
        }
        PackageManager pm = context.getPackageManager();
        if (null == pm) {
            return 0;
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        if (null != packageInfo) {
            return packageInfo.versionCode;
        }
        return 0;
    }

    public static String[] getImsi(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String imsi1 = telephonyManager.getSubscriberId();
//        if (imsi1 != null) {
//            return new String[]{imsi1};
//        }
//        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        String[] imsis = new String[2]; //双卡
        try {
            for (int i = 0; i < 2; i++) {
                Object tempSubIds = ReflectHelper.invokeStatic("android.telephony.SubscriptionManager", "getSubId", new Class[]{int.class}, new Object[]{i});
                if (null != tempSubIds && tempSubIds instanceof int[]) {
                    int[] subid = (int[]) tempSubIds;
                    if (subid.length != 0) {
                        Object tempSubscriberId = ReflectHelper.invoke(telephonyManager, "getSubscriberId", new Class[]{int.class}, new Object[]{subid[0]});
                        if (null != tempSubscriberId && tempSubscriberId instanceof String) {
                            imsis[i] = (String) tempSubscriberId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        return imsis;
    }

    /**
     * The function that actually fetches the Advertising ID.
     * - If called from the UI Thread will throw an Exception
     *
     * @param context the android context
     * @return the advertising id or null
     */
    public static String getAdvertisingId(Context context) {
        try {
            Object AdvertisingInfoObject = ReflectHelper.invokeStatic(
                    "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                    "getAdvertisingIdInfo", new Class[]{Context.class}, new Object[]{context});
            return (String) ReflectHelper.invoke(AdvertisingInfoObject, "getId", null);
        } catch (Exception e) {
//            Logger.w(TAG, "can't getting the Advertising ID: %s - Cause: %s",
//                    e.toString(), e.getCause());
            Logger.w(TAG, "can't getting the Advertising ID");
            return null;
        }
    }

    //ANDROID_ID似乎是获取Device ID的一个好选择，但它也有缺陷：在主流厂商生产的设备上，有一个很经常的bug，就是每个设备都会产生相同的ANDROID_ID：9774d56d682e549c 。
    // 同时刷机，或者重置ANDROID_ID的值都会变化。
    public static String getAndroidId(Context context) {
        //return readTestIds()[3];
        String ret = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(ret) && !(ret.toLowerCase().equals("9774d56d682e549c"))) {
            return ret;
        } else {
            return "";
        }
    }

    //获取当前进程名
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    /**
     * 反射获取是否是海外固件版本
     */
    public static boolean firmwareProductInternational() {
        if (sIsProductInternational != null) {
            Logger.d(TAG, "isProductInternational = " + sIsProductInternational);
            return sIsProductInternational;
        }
        try {
            sIsProductInternational = (Boolean) ReflectHelper.invokeStatic("android.os.BuildExt", "isProductInternational", null);
            return sIsProductInternational;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String[] readTestIds() {
        String ids[] = new String[4];
        File file = new File(Environment.getExternalStorageDirectory(),
                "fakeId_testx.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readline = "";
            int i = 0;
            while ((readline = br.readLine()) != null) {
                System.out.println("读取第" + (i + 1) + "行" + readline);
                if (i < 4) {
                    ids[i] = readline;
                    i++;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
    }


    /**
     * 是否cta固件
     * @return
     */
    public static boolean isCTA() {
        String res = SystemProperties.get("ro.build.cta");
        if (res != null && res.equalsIgnoreCase("CTA")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 开机向导是否点了确定，确定后完成权限声明
     * @param context
     * @return
     */
    public static boolean kaiJiXiangDao(Context context) {
        return Settings.Global.getInt(
                context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) == 1;
    }

}
