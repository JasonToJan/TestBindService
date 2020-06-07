package com.meizu.statsapp.v3;

import android.webkit.JavascriptInterface;

import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Created by shaorui on 16-12-12.
 * 提供给H5埋点的接口
 */
public class MzUsageStatsJavascriptInterface {
    private final String TAG = "MzUsageStatsJavascriptInterface";

    public static MzUsageStatsJavascriptInterface getInstance() {
        return new MzUsageStatsJavascriptInterface();
    }
    /**
     * 获取埋点umid
     */
    @JavascriptInterface
    public String getUMID() {
        Logger.d(TAG, "getUMID" );
        return UsageStatsProxy3.getInstance().getUMID();
    }

    /**
     * 获取flyme帐号
     */
    @JavascriptInterface
    public String getFlymeUid() {
        Logger.d(TAG, "getFlymeUid" );
        return UsageStatsProxy3.getInstance().getFlymeUID();
    }
//
//    /**
//     * 普通公共上报(实时上报)
//     *
//     * @param json 事件内容
//     * @return
//     */
//    @JavascriptInterface
//    public void getTongjiMC(String json) {
//        Logger.d(TAG, "getTongjiMC, json:"+json );
//        UsageStatsProxy3.getInstance().onEventH5(json, "mc");
//    }
//
//    /**
//     * js异常(实时上报)
//     *
//     * @param json 事件内容
//     * @return
//     */
//    @JavascriptInterface
//    public void getTongjiJE(String json) {
//        Logger.d(TAG, "getTongjiJE, json:"+json );
//        UsageStatsProxy3.getInstance().onExceptionH5(json, "je");
//    }
//
//    /**
//     * 页面停留时间(实时上报)
//     *
//     * @param json 事件内容
//     * @return
//     */
//    @JavascriptInterface
//    public void getTongjiST(String json) {
//        Logger.d(TAG, "getTongjiST, json:"+json );
//        UsageStatsProxy3.getInstance().onPageH5(json, "st");
//    }
}

//getTongjiMC:function(json){} //普通公共上报
//getTongjiJE:function(){} // js异常
//getTongjiST:function(){} // 页面停留时间
// json={
//      data:'{"key1":"value1","key2":"value2"}'
// }