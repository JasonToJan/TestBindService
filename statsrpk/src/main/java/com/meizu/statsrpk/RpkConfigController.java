package com.meizu.statsrpk;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.net.NetRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.RequestFreqRestrict;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil.HEADER_If_Modified_Since;
import static com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil.HEADER_If_None_Match;

/**
 * Created by huchen on 16-9-29.
 */

public class RpkConfigController {
    private static final String TAG = "RpkConfigController";

    private Context mContext;
    private RpkInfo mRpkInfo;

    private Handler mHandler;
    private final int CHECK_UPDATE = 0x1;
    private final static String WORK_THREAD_NAME = "rpk.ConfigControllerWorker";

    private SharedPreferences mSP;

    public RpkConfigController(Context context, RpkInfo rpkInfo) {
        this.mContext = context;
        this.mRpkInfo = rpkInfo;
        mSP = mContext.getSharedPreferences(RpkConstants.SP_FILE_RPK_CONFIG_PREFIX + rpkInfo.rpkPkgName, Context.MODE_PRIVATE);

        HandlerThread thread = new HandlerThread(WORK_THREAD_NAME, Thread.NORM_PRIORITY);
        thread.start();
        mHandler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == CHECK_UPDATE) {
                    long lastGet = 0;
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        lastGet = dateFormat.parse(mSP.getString(UxipConstants.PREFERENCES_KEY_GET_TIME, "")).getTime();
                    } catch (ParseException e) {
                        //e.printStackTrace();
                    }
                    long now = System.currentTimeMillis();
                    int randomMin = randInt(24 * 60, 48 * 60); //24-48小时内的分钟随机
                    if (now - lastGet > randomMin * 60 * 1000) {
                       getConfigFromServer();
                    }
                }
            }
        };
    }

    void init(RpkInstanceImpl rpkInstanceImpl) { //init已经被全局executor包裹
        checkUpdate(1000);
    }

    private void checkUpdate(int delayInMills) {
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

    private void getConfigFromServer() {
        Logger.d(TAG, "getConfigFromServer, now: " + System.currentTimeMillis() + ", last get time: " + mSP.getString(UxipConstants.PREFERENCES_KEY_GET_TIME, ""));
        if (!NetInfoUtils.isOnline(mContext)) {
            Logger.d(TAG, "getConfigFromServer, network unavailable");
            return;
        }
        Map<String, String> header = new HashMap<>();
        String modifiedSince = mSP.getString("lastModified", "");
        header.put(HEADER_If_Modified_Since, modifiedSince);
        String noneMath = mSP.getString("ETag", "");
        header.put(HEADER_If_None_Match, noneMath);
        NetResponse response = null;
        Uri.Builder builder = Uri.parse(UxipConstants.GET_CONFIG_URL + "rpk/" + mRpkInfo.rpkPkgName).buildUpon(); //CDN模式获取uxip配置
        String buildUri = builder.toString();
        Logger.d(TAG, "try local... cdn url: " + buildUri + ", header: " + header);
        if (!RequestFreqRestrict.isAllow(mContext)) {
            return;
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
                    RpkUxipConfig.parseAppInfo(mContext, responseBody, mRpkInfo);
                    RpkUxipConfig.parseConfig(mContext, responseBody, mRpkInfo);
                } catch (JSONException e) {
                    Logger.e(TAG, e.getMessage());
                }
            }
        } else if (response != null && response.getResponseCode() == 304) {
            Logger.d(TAG, "config in server has no change");
        }
    }

}
