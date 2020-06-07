package com.meizu.statsrpk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.meizu.LogUtils;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.net.NetRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by jinhui on 18-7-26.
 */

public class RpkUsageStats {
    private static String TAG = RpkUsageStats.class.getSimpleName();
    private static final Object sLock = new Object();
    private static RpkUsageStats sInstance;
    private Context mContext;
    private RpkInfo mRpkInfo;
    private RpkInstanceImpl mRpkInstance;
    private SharedPreferences mSP;

    public static void init(Context context, String rpkPkgName, String rpkVer, int rpkVerCode) {
        LogUtils.d(TAG, "go to init");
        if (null == sInstance) {
            synchronized (sLock) {
                if (null == sInstance) {
                    sInstance = new RpkUsageStats(context, rpkPkgName, rpkVer, rpkVerCode);
                }
            }
        }
        LogUtils.d(TAG, "finished to init");
    }

    private RpkUsageStats(Context context, String rpkPkgName, String rpkVer, int rpkVerCode) {
        if (null == context) {
            throw new IllegalArgumentException("The context is null!");
        }
        mContext = context.getApplicationContext();
        long t1 = System.currentTimeMillis();
        mRpkInfo = new RpkInfo();
        mRpkInfo.rpkPkgName = rpkPkgName;
        mRpkInfo.rpkVer = rpkVer;
        mRpkInfo.rpkVerCode = rpkVerCode;
        findRelativeApp(rpkPkgName);
        Logger.d(TAG, "##### RpkUsageStats init complete, " + (System.currentTimeMillis() - t1));
    }

    public static RpkUsageStats getInstance() {
        return sInstance;
    }

    public void onEvent(final String eventName, final String pageName, final Map<String, String> properties) {
        Logger.d(TAG, "##### RpkUsageStats onEvent: " + eventName);
        RpkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mRpkInstance != null) {
                    mRpkInstance.onEvent(eventName, pageName, properties);
                }
            }
        });
    }

    public void onPageShow(final String pageName) {
        Logger.d(TAG, "##### RpkUsageStats onPageShow: " + pageName);
        RpkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mRpkInstance != null) {
                    mRpkInstance.onPageStart(pageName);
                }
            }
        });
    }

    public void onPageHide(final String pageName) {
        Logger.d(TAG, "##### RpkUsageStats onPageHide: " + pageName);
        RpkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mRpkInstance != null) {
                    mRpkInstance.onPageStop(pageName);
                }
            }
        });
    }

    /**
     * 通过业务传入的rpkPkgName去检查本地是否有保存其相对应的appkey和apkPkgName，若没有，则向服务端查询，查询到结果才能正常初始化
     *
     * @param rpkPkgName
     */
    private void findRelativeApp(final String rpkPkgName) {
        Logger.d(TAG, "##### RpkUsageStats findRelativeApp: " + rpkPkgName);
        RpkExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mSP = mContext.getSharedPreferences(RpkConstants.SP_FILE_RPK_CONFIG_PREFIX + rpkPkgName, Context.MODE_PRIVATE);
                mRpkInfo.appKey = mSP.getString("appKey", "");
                mRpkInfo.apkPkgName = mSP.getString("apkPkgName", "");
                if (TextUtils.isEmpty(mRpkInfo.appKey) || TextUtils.isEmpty(mRpkInfo.apkPkgName)) {
                    NetResponse response = null;
                    Uri.Builder builder = Uri.parse(UxipConstants.RPK_CONFIG_URL + mRpkInfo.rpkPkgName).buildUpon();
                    String buildUri = builder.toString();
                    Logger.d(TAG, "RpkUsageStats try cdn url: " + buildUri);
                    try {
                        response = NetRequester.getInstance(mContext).postNoGslb(buildUri, null);
                    } catch (IOException e) {
                        Logger.e(TAG, e.getMessage());
                    } catch (RuntimeException e) {
                        Logger.e(TAG, e.getMessage());
                    }
                    Logger.d(TAG, "RpkUsageStats getConfigFromServer response: " + response);
                    if (response != null && response.getResponseCode() == 200) {
                        String responseBody = response.getResponseBody();
                        if (responseBody != null) {
                            Logger.d(TAG, "RpkUsageStats successfully posted to " + buildUri);
                            try {
                                RpkUxipConfig.parseAppInfo(mContext, responseBody, mRpkInfo);
                                RpkUxipConfig.parseConfig(mContext, responseBody, mRpkInfo);
                            } catch (JSONException e) {
                                Logger.e(TAG, e.getMessage());
                            }
                        }
                    }
                }

                mRpkInstance = new RpkInstanceImpl(mContext, mRpkInfo);


//                if (!TextUtils.isEmpty(mRpkInfo.appKey) && !TextUtils.isEmpty(mRpkInfo.apkPkgName)) {
//
//                } else {
//                    Logger.d(TAG, "rpkInfo.appKey or rpkInfo.apkPkgName is empty，unable to initialize.");
//                }
            }
        });
    }

}
