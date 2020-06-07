package com.meizu.statsapp.v3.lib.plugin.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.TerType;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.net.HttpSecureRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil;
import com.meizu.statsapp.v3.lib.plugin.utils.RequestFreqRestrict;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huchen on 16-8-3.
 */
public class UmidFetcher {
    private static final String TAG = "UmidFetcher";
    private static final Object sLock = new Object();
    private static UmidFetcher sInstance;

    //PREFERENCES
    public static final String PREFERENCES_UMID_NAME = "com.meizu.statsapp.v3.umid";
    public static final String PREFERENCES_KEY_UMID = "UMID";

    private Context mContext;
    private SharedPreferences mSP;
    private AtomicBoolean mFulled = new AtomicBoolean(false);

    //这里面不要做多余动作，在sdkInstanceImpl没有构造前就会调用到这
    private UmidFetcher(Context context) {
        this.mContext = context;
        this.mSP = context.getSharedPreferences(PREFERENCES_UMID_NAME, Context.MODE_PRIVATE);
    }

    public static UmidFetcher getInstance(Context context) {
        if (sInstance == null) {
            synchronized (sLock) {
                if (sInstance == null) {
                    sInstance = new UmidFetcher(context);
                }
            }
        }
        return sInstance;
    }

    public String readUmidFromLocal() {
        return mSP.getString(UmidFetcher.PREFERENCES_KEY_UMID, "");
    }

    //只有在发送event的时候才会被调用，因此这里不需要再被offline模式控制
    public synchronized String fetchOrRequestUMID() {
        String localUmid = readUmidFromLocal();
        if (localUmid.equals("")) {
            return getUmidFromServer();
        } else {
            if (mFulled.compareAndSet(false, true)) { //每次运行最多做一次补全
                fullUmidIdIf();
            }
            return localUmid;
        }
    }

    private String getUmidFromServer() {
        if (!NetInfoUtils.isOnline(mContext)) {
            Logger.d(TAG, "getUmidFromServer, network unavailable");
            return "";
        }
        String uri = buildGetUri(mContext);
        if (!RequestFreqRestrict.isAllow(mContext)) {
            return "";
        }
        Logger.d(TAG, "try getUmidFromServer... url: " + uri);
        NetResponse response = HttpSecureRequester.getInstance(mContext).stringPartRequest(uri, "GET", null, null);
        Logger.d(TAG, "getUmidFromServer, response: " + response);
        handleResponse(response);
        return mSP.getString(PREFERENCES_KEY_UMID, "");
    }

    private void fullUmidIdIf() {
        if (!NetInfoUtils.isOnline(mContext)) {
            Logger.d(TAG, "full UMID Ids, network unavailable");
            return;
        }
        boolean findNewImei;
        String imei = FlymeOSUtils.getDeviceId(mContext);
        String i1 = mSP.getString("imei", "");
        String i2 = mSP.getString("secondary_imei", "");
        if (!TextUtils.isEmpty(imei) && (imei.equals(i1)
                || imei.equals(i2))) {
            findNewImei = false;
        } else {
            Logger.d(TAG, "findNewImei true");
            findNewImei = true;
        }
        if (findNewImei && !TextUtils.isEmpty(imei)) {
            String uri = buildFullUri(mContext);
            Logger.d(TAG, "try fullUmidFromServer... url: " + uri);
            NetResponse response = HttpSecureRequester.getInstance(mContext).stringPartRequest(uri, "POST", null, null);
            Logger.d(TAG, "fullUmidIds, response: " + response);
            handleResponse(response);
        }
    }

    private boolean handleResponse(NetResponse response) {
        if (response != null && response.getResponseCode() == 200) {
            if (response.getResponseBody() != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response.getResponseBody());
                    int code = jsonObject.getInt(UxipConstants.API_RESPONSE_CODE);
                    if (code == 200) {
                        Logger.d(TAG, "Successfully posted to " + UxipConstants.GET_UMID_URL);
                        JSONObject value = jsonObject.getJSONObject(UxipConstants.API_RESPONSE_VALUE);
                        String umid = value.getString(UxipConstants.UMID_RESPONSE_KEY_UMID);
                        if (!TextUtils.isEmpty(umid)) {
                            Logger.d(TAG, "new umid " + umid);
                            SharedPreferences.Editor editor = mSP.edit();
                            editor.putString(PREFERENCES_KEY_UMID, umid);
                            editor.putString("imei", value.getString("imei"));
                            editor.putString("secondary_imei", value.getString("secondary_imei"));
                            editor.putString("sn", value.getString("sn"));
                            editor.apply();
                            return true;
                        }
                    }
                } catch (JSONException e) {
                    Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
                }
            }
        }
        return false;
    }

    private String getMacWithoutColon() {
        String mac = NetInfoUtils.getMACAddress(mContext);
        if (mac != null) {
            mac = mac.replace(":", "");
            mac = mac.toUpperCase();
        }
        return mac;
    }

    private String getTerType() {
        if (FlymeOSUtils.isBox(mContext)) {
            return TerType.FLYME_TV.toString();
        } else if (FlymeOSUtils.isTablet(mContext)) {
            return TerType.PAD.toString();
        } else {
            return TerType.PHONE.toString();
        }
    }

    private String buildGetUri(Context context) {
        //http://172.16.177.140/api/v3/umid
        Uri.Builder builder = Uri.parse(UxipConstants.GET_UMID_URL).buildUpon();
        Map<String, String> uriParam = new HashMap<>();
        String terType = getTerType();
        builder.appendQueryParameter("ter_type", terType);
        uriParam.put("ter_type", terType);
        if (!FlymeOSUtils.isBox(context) && !FlymeOSUtils.isTablet(context)) { //只有手机有IMEI
            String imei = FlymeOSUtils.getDeviceId(context);
            builder.appendQueryParameter("imei", imei);
            uriParam.put("imei", FlymeOSUtils.getDeviceId(context));
        }
        builder.appendQueryParameter("os_type", UxipConstants.OS_TYPE);
        uriParam.put("os_type", UxipConstants.OS_TYPE);
        builder.appendQueryParameter("os_version", Build.VERSION.RELEASE);
        uriParam.put("os_version", Build.VERSION.RELEASE);
        String mac = getMacWithoutColon();
        builder.appendQueryParameter("mac", mac);
        uriParam.put("mac", mac);
        String sn = FlymeOSUtils.getSN();
        uriParam.put("sn", sn);
        builder.appendQueryParameter("sn", sn);
        String androidId = FlymeOSUtils.getAndroidId(context);
        builder.appendQueryParameter("android_id", androidId);
        uriParam.put("android_id", androidId);
        long tsValue = System.currentTimeMillis() / 1000;
        String ts = String.valueOf(tsValue);
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_TS, ts);
        uriParam.put(Parameters.UXIP_REQUEST_PARAM_TS, ts);
        String nonce = String.valueOf(tsValue + new Random().nextInt());
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_NONCE, nonce);
        uriParam.put(Parameters.UXIP_REQUEST_PARAM_NONCE, nonce);
        for (Map.Entry<String, String> entry : uriParam.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Logger.d(TAG, "buildGetUri, uriParam: " + key + "," + value);
        }
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_SIGN,
                NetRequestUtil.sign("GET", UxipConstants.GET_UMID_URL, uriParam, null));
        return builder.toString();
    }

    private String buildFullUri(Context context) {
        Uri.Builder builder = Uri.parse(UxipConstants.GET_UMID_URL).buildUpon();
        Map<String, String> uriParam = new HashMap<>();
        String terType = getTerType();
        builder.appendQueryParameter("ter_type", terType);
        uriParam.put("ter_type", terType);
        if (!FlymeOSUtils.isBox(context) && !FlymeOSUtils.isTablet(context)) { //只有手机有IMEI
            String imei = FlymeOSUtils.getDeviceId(context);
            builder.appendQueryParameter("imei", imei);
            uriParam.put("imei", FlymeOSUtils.getDeviceId(context));
        }
        builder.appendQueryParameter("os_type", UxipConstants.OS_TYPE);
        uriParam.put("os_type", UxipConstants.OS_TYPE);
        String androidId = FlymeOSUtils.getAndroidId(context);
        builder.appendQueryParameter("android_id", androidId);
        uriParam.put("android_id", androidId);
        long tsValue = System.currentTimeMillis() / 1000;
        String ts = String.valueOf(tsValue);
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_TS, ts);
        uriParam.put(Parameters.UXIP_REQUEST_PARAM_TS, ts);
        String nonce = String.valueOf(tsValue + new Random().nextInt());
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_NONCE, nonce);
        uriParam.put(Parameters.UXIP_REQUEST_PARAM_NONCE, nonce);
        String umid = mSP.getString(PREFERENCES_KEY_UMID, "");
        builder.appendQueryParameter("umid", umid);
        uriParam.put("umid", umid);
        for (Map.Entry<String, String> entry : uriParam.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Logger.d(TAG, "buildFullUri, uriParam: " + key + "," + value);
        }
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_SIGN,
                NetRequestUtil.sign("POST", UxipConstants.GET_UMID_URL, uriParam, null));
        return builder.toString();
    }
}
