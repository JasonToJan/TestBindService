package com.meizu.statsapp.v3.lib.plugin.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.meizu.statsapp.v3.GlobalExecutor;
import com.meizu.statsapp.v3.InitConfig;
import com.meizu.statsapp.v3.lib.plugin.constants.TerType;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.net.NetRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.lib.plugin.tracker.EventFilter;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.RequestFreqRestrict;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil.HEADER_If_Modified_Since;
import static com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil.HEADER_If_None_Match;

/**
 * Created by huchen on 16-9-29.
 */

public class ConfigController {
    private static final String TAG = "ConfigController";

    private Context mContext;
    private String pkgKey;
    private SDKInstanceImpl mSdkInstance;

    private Handler mHandler;
    private final int CHECK_UPDATE = 0x1;
    private final static String WORK_THREAD_NAME = "com.meizu.statsapp.v3.ConfigControllerWorker";

    private SharedPreferences mSP;
    private SharedPreferences.Editor mEditor;

    public ConfigController(Context context, String pkgKey) {
        this.mContext = context;
        this.pkgKey = pkgKey;
        this.mSP = mContext.getSharedPreferences(UxipConstants.PREFERENCES_SERVER_CONFIG_NAME, Context.MODE_PRIVATE);
        this.mEditor = mSP.edit();

        HandlerThread thread = new HandlerThread(WORK_THREAD_NAME, Thread.NORM_PRIORITY);
        thread.start();
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CHECK_UPDATE) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    String lastGetTime = mSP.getString(UxipConstants.PREFERENCES_KEY_GET_TIME, "");
                    long lastGet = 0;
                    try {
                        lastGet = dateFormat.parse(lastGetTime).getTime();
                    } catch (ParseException e) {
                        //e.printStackTrace();
                    }
                    long now = System.currentTimeMillis();
                    int randomMin = randInt(2 * 60, 4 * 60); //2-4小时内的分钟随机
                    if (now - lastGet > randomMin * 60 * 1000) {
                        boolean success = getConfigFromServer();
                        if (success) {
                            String getTime = dateFormat.format(new Date(now));
                            mEditor.putString(UxipConstants.PREFERENCES_KEY_GET_TIME, getTime);
                            mEditor.commit();
                        }
                    }
                }
            }
        };
    }

    void init(SDKInstanceImpl sdkInstance) { //init已经被全局executor包裹
        this.mSdkInstance = sdkInstance;
        Receiver receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        try {
            mContext.registerReceiver(receiver, filter);
        } catch (Exception e) {
            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
        }
        checkUpdate(1000);
    }

    /* package */ void checkUpdate(int delayInMills) {
        if (mHandler.hasMessages(CHECK_UPDATE)) {
            mHandler.removeMessages(CHECK_UPDATE);
        }
        mHandler.sendEmptyMessageDelayed(CHECK_UPDATE, delayInMills);
    }

    private int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        return rand.nextInt((max - min) + 1) + min;
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

    private boolean getConfigFromServer() {
        if (InitConfig.offline) {
            Logger.d(TAG, "getConfigFromServer, sdk offline mode");
            return false;
        }

        if (!FlymeOSUtils.kaiJiXiangDao(mContext)) {
            Logger.d(TAG, "getConfigFromServer --> 还未完成开机向导");
            return false;
        }

        if (!NetInfoUtils.isOnline(mContext)) {
            Logger.d(TAG, "getConfigFromServer, network unavailable");
            return false;
        }
        Logger.d(TAG, "getConfigFromServer, now: " + System.currentTimeMillis() + ", last get time: " + mSP.getString(UxipConstants.PREFERENCES_KEY_GET_TIME, ""));
        boolean success = false;
        Map<String, String> header = new HashMap<>();
        String modifiedSince = mSP.getString("lastModified", "");
        header.put(HEADER_If_Modified_Since, modifiedSince);
        String noneMath = mSP.getString("ETag", "");
        header.put(HEADER_If_None_Match, noneMath);
        NetResponse response = null;
        Uri.Builder builder = Uri.parse(UxipConstants.GET_CONFIG_URL + pkgKey).buildUpon(); //CDN模式获取uxip配置
        String buildUri = builder.toString();
        Logger.d(TAG, "try local... cdn url: " + buildUri + ", header: " + header);
        if (!RequestFreqRestrict.isAllow(mContext)) {
            return false;
        }
        try {
            response = NetRequester.getInstance(mContext).postNoGslb(buildUri, header);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        Logger.d(TAG, "getConfigFromServer response: " + response);
        if (response != null && response.getResponseCode() == 200) {
            String responseBody = response.getResponseBody();
            if (responseBody != null) {
                try {
                    Logger.d(TAG, "Successfully posted to " + buildUri);
                    mEditor.putString(UxipConstants.PREFERENCES_KEY_RESPONSE, responseBody);
                    mEditor.commit();
                    parseAndApply(responseBody);
                    success = true;
                } catch (JSONException e) {
                    Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
                } catch (NumberFormatException e) {
                    Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
                }
            }
        } else if (response != null && response.getResponseCode() == 304) {
            Logger.d(TAG, "config in server has no change");
            success = true;
        }
        return success;
    }

    /**
     * @param response: {
     *                    "code": 200,
     *                    "message": "OK",
     *                    "value": {
     *                    "version": 8, //配置版本号，带在后续请求的头里
     *                    "active": true,
     *                    "ttl": 4320, //当前配置存活时间，超过时间才可以去后端获取新的配置，分钟为单位。
     *                    "sampling": false,
     *                    "positioningInterval": 240, //间隔多长时间重新取定位数据，分钟为单位
     *                    "heartbeatInterval": 300, //间隔多长时间提交心跳数据，以秒为单位，设为0代表不发心跳
     *                    "uploadPolicy": {
     *                    "onStart": true,
     *                    "onCharge": true,
     *                    "onReconnect": true,
     *                    "interval": 30, //间隔多长时间重新取定位数据，分钟为单位
     *                    "mobileQuota": 1048576, /移动网络下单日数据最大量, 以byte为单位，负数表示没有限制
     *                    "cacheCapacity": 50 //本地最大缓存多少上传，负数表示没有设置
     *                    },
     *                    "events": [
     *                    {
     *                    "name": "lorem",
     *                    "active": true,
     *                    "realtime": true
     *                    }
     *                    ]
     *                    }
     *                    }
     * @throws JSONException
     */
    private void parseAndApply(String response) throws JSONException {
        Logger.d(TAG, "parseConfigJson 1");
        JSONObject jsonObject = new JSONObject(response);
        Logger.d(TAG, "parseConfigJson 2, config json:" + jsonObject.toString());

        //版本号
        int version = jsonObject.getInt(UxipConstants.RESPONSE_KEY_VERSION);
        mEditor.putInt("version", version);
        mEditor.commit();

        //上报策略
        boolean active = jsonObject.getBoolean(UxipConstants.RESPONSE_KEY_ACTIVE);
        JSONObject uploadPolicy = jsonObject.getJSONObject(UxipConstants.RESPONSE_KEY_UPLOADPOLICY);
        boolean flushOnStart = uploadPolicy.getBoolean(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_ONSTART);
        boolean flushOnReconnect = uploadPolicy.getBoolean(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_ONRECONNECT);
        boolean flushOnCharge = uploadPolicy.getBoolean(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_ONCHARGE);
        long flushDelayInterval = uploadPolicy.getLong(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_INTERVAL) * 60 * 1000;
        long flushMobileTrafficLimit = uploadPolicy.getLong(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_MOBILEQUOTA);
        int flushCacheLimit = uploadPolicy.getInt(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_CACHECAPACITY);
        int neartimeInterval = uploadPolicy.getInt(UxipConstants.RESPONSE_KEY_UPLOADPOLICY_NEARTIME_INTERVAL);
        mSdkInstance.getEmitter().updateConfig(active,
                flushOnStart, flushOnCharge, flushOnReconnect,
                flushDelayInterval, flushCacheLimit, flushMobileTrafficLimit, neartimeInterval);

        //事件过滤
        Map<String, EventFilter> eventFilterMap = new HashMap<>();
        JSONArray events = jsonObject.getJSONArray(UxipConstants.RESPONSE_KEY_EVENTS);
        for (int i = 0; i < events.length(); i++) {
            JSONObject event = events.getJSONObject(i);
            String eventName = event.getString(UxipConstants.RESPONSE_KEY_EVENTS_NAME);
            boolean eventActive = event.getBoolean(UxipConstants.RESPONSE_KEY_EVENTS_ACTIVE);
            boolean eventRealtime = event.getBoolean(UxipConstants.RESPONSE_KEY_EVENTS_REALTIME);
            boolean eventNeartime = event.getBoolean(UxipConstants.RESPONSE_KEY_EVENTS_NEARTIME);
            eventFilterMap.put(eventName, new EventFilter(eventName, eventActive, eventRealtime, eventNeartime));
        }
        mSdkInstance.getTracker().setEventFilterMap(eventFilterMap);

        //获取地理位置间隔
        long positioningInterval = jsonObject.getLong(UxipConstants.RESPONSE_KEY_POSITIONING_INTERVAL) * 60 * 1000;
        mSdkInstance.getLocationFetcher().setInterval(positioningInterval);
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                //boolean isWifi = NetInfoUtils.isWiFiWorking(context);
                GlobalExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        boolean isOnline = NetInfoUtils.isOnline(context);
                        Logger.d(TAG, "CONNECTIVITY_ACTION, isOnline = " + isOnline);
                        if (isOnline) {
                            checkUpdate(1000);
                        }
                    }
                });
            }
        }
    }

}
