package com.meizu.statsapp.v3.lib.plugin.tracker.subject;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.TerType;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.OSUtils;
import com.meizu.statsapp.v3.utils.log.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huchen on 16-8-1.
 */
class DeviceInfo {
    private final static String TAG = "DeviceInfo";

    private DeviceInfo(DeviceInfoBuilder builder) {
        setDevice();
        setSn();
        setProductModel();
        setBuildMask();
        setOsType();
        setBrand();
        setOsVersion();
        setOs();

        if (builder.context != null) {
            setContextualParams(builder.context);
        }

        Logger.v(TAG, "DeviceInfo created successfully.");
    }

    private HashMap<String, Object> devicesPairs = new HashMap<>();

    /**
     * Builder for the DeviceInfo
     */
    public static class DeviceInfoBuilder {
        private Context context = null; // Optional

        /**
         * @param context The android context to pass to the subject
         * @return itself
         */
        public DeviceInfoBuilder context(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Creates a new Subject
         *
         * @return a new Subject object
         */
        public DeviceInfo build() {
            return new DeviceInfo(this);
        }
    }

    /**
     * Sets the contextually based parameters.
     *
     * @param context the android context
     */
    public void setContextualParams(Context context) {
        setImei(context);
        setCountry(context);
        setOperator(context);
        setInternational(context);
        isRoot(context);
        setFlymeUid(context);
        setMacAddress(context);
        setSre(context);
        setAndroidId(context);
        setAndroidAdId(context);
        setTerType(context);
    }

    /**
     * Sets the device model.
     */
    private void setDevice() {
        String paramDevice = Build.MODEL;
        if (!TextUtils.isEmpty(paramDevice)) {
            paramDevice = paramDevice.replace("\n", "").replace("\r", "");
        }
        add(Parameters.DEVICE, paramDevice);
    }

    /**
     * Sets the imei.
     */
    private void setImei(Context context) {
        String imei = FlymeOSUtils.getDeviceId(context);
        add(Parameters.IMEI, imei);
        Logger.d(TAG, "deviceInfo set imei when init, imei: " + imei);
    }

    /**
     * Sets the country.
     */
    private void setCountry(Context context) {
        String paramCountry = FlymeOSUtils.getCountry(context);
        if (!TextUtils.isEmpty(paramCountry)) {
            paramCountry = paramCountry.replace("\n", "").replace("\r", "");
        }
        add(Parameters.COUNTRY, paramCountry);
    }

    /**
     * Sets the operator.
     */
    private void setOperator(Context context) {
        String paramOperator = FlymeOSUtils.getOperator(context);
        if (!TextUtils.isEmpty(paramOperator)) {
            paramOperator = paramOperator.replace("\n", "").replace("\r", "");
        }
        add(Parameters.OPERATOR, paramOperator);
    }

    private void setInternational(Context context) {
        add(Parameters.INTERNATIONAL, FlymeOSUtils.firmwareProductInternational());
    }

    /**
     * is Root.
     */
    private void isRoot(Context context) {
        add(Parameters.ROOT, FlymeOSUtils.isRoot(context));
    }

    /**
     * Sets the sn.
     */
    private void setSn() {
        add(Parameters.SN, FlymeOSUtils.getSN());
    }

    /**
     * Sets the Flyme Uid.
     */
    private void setFlymeUid(Context context) {
        if (FlymeOSUtils.isBrandMeizu()) {
            add(Parameters.FLYME_UID, FlymeOSUtils.getFlymeUid(context));
        }
    }

    /**
     * Sets the Mac Address.
     */
    private void setMacAddress(Context context) {
        add(Parameters.MAC_ADDRESS, NetInfoUtils.getMACAddress(context));
    }

    /**
     * Sets the PRODUCT_MODEL.
     */
    private void setProductModel() {
        String paramProductModel = FlymeOSUtils.getProductModel();
        if (!TextUtils.isEmpty(paramProductModel)) {
            paramProductModel = paramProductModel.replace("\n", "").replace("\r", "");
        }
        add(Parameters.PRODUCT_MODEL, paramProductModel);
    }

    /**
     * Sets the BUILD_MASK.
     */
    private void setBuildMask() {
        String paramBuildMask = FlymeOSUtils.getBuildMask();
        if (!TextUtils.isEmpty(paramBuildMask)) {
            paramBuildMask = paramBuildMask.replace("\n", "").replace("\r", "");
        }
        add(Parameters.BUILD_MASK, paramBuildMask);
    }

    /**
     * Sets the Sre.
     */
    private void setSre(Context context) {
        add(Parameters.SRE, FlymeOSUtils.getDisplaySize(context));
    }


    /**
     * Sets the TER_TYPE.
     * Defaults too 1(phone) currently.
     */
    private void setTerType(Context context) {
        if (FlymeOSUtils.isTablet(context)) {
            add(Parameters.TER_TYPE, TerType.PAD.value());
        } else if (FlymeOSUtils.isBox(context)) {
            add(Parameters.TER_TYPE, TerType.FLYME_TV.value());
        } else  {
            add(Parameters.TER_TYPE, TerType.PHONE.value());
        }
    }

    /**
     * Set operating system type.
     * Defaults too 'Android' currently.
     */
    private void setOsType() {
        add(Parameters.OS_TYPE, UxipConstants.OS_TYPE);
    }

    /**
     * Set brand.
     */
    private void setBrand() {
        String paramBrand = FlymeOSUtils.getBrand();
        if (!TextUtils.isEmpty(paramBrand)) {
            paramBrand = paramBrand.replace("\n", "").replace("\r", "");
        }
        add(Parameters.BRAND, paramBrand);
    }

    /**
     * Sets the operating system version.
     */
    private void setOsVersion() {
        String paramOsVersion = Build.VERSION.RELEASE;
        if (!TextUtils.isEmpty(paramOsVersion)) {
            paramOsVersion = paramOsVersion.replace("\n", "").replace("\r", "");
        }
        add(Parameters.OS_VERSION, paramOsVersion);
    }

    /**
     * Sets the operating system.
     */
    private void setOs() {
        boolean isBrandMeizu = FlymeOSUtils.isBrandMeizu();
        Logger.d(TAG, "isBrandMeizu:" + isBrandMeizu);
        if (isBrandMeizu) {
            add(Parameters.OS, "Flyme");
        } else {
            add(Parameters.OS, OSUtils.getOtherBrandOs());
        }
    }

    /**
     * Sets the android.
     */
    private void setAndroidId(Context context) {
        String androidId = FlymeOSUtils.getAndroidId(context);
        Logger.d(TAG, "Android ID:" + androidId);
        add(Parameters.ANDROID_ID, androidId);
    }

    /**
     * Sets the advertising id of the
     * device.
     *
     * @param context the android context
     */
    private void setAndroidAdId(final Context context) {
        String playAdId = FlymeOSUtils.getAdvertisingId(context);
        Logger.d(TAG, "Advertising ID:" + playAdId);
        add(Parameters.ANDROID_AD_ID, playAdId);
    }

    /**
     * Inserts a value into the mobilePairs
     * subject storage.
     * <p/>
     * NOTE: Avoid putting null or empty
     * values in the map
     *
     * @param key   a key value
     * @param value the value associated with
     *              the key
     */
    private void add(String key, String value) {
        if (key != null && !key.isEmpty() && value != null) {
            this.devicesPairs.put(key, value);
        }
    }

    /**
     * Inserts a value into the devicesPairs
     * subject storage.
     * <p/>
     * NOTE: Avoid putting null or empty
     * values in the map
     *
     * @param key   a key value
     * @param value the value associated with
     *              the key
     */
    private void add(String key, Object value) {
        if (key != null && !key.isEmpty() && value != null) {
            this.devicesPairs.put(key, value);
        }
    }

    public Map getMap() {
        return devicesPairs;
    }

}
