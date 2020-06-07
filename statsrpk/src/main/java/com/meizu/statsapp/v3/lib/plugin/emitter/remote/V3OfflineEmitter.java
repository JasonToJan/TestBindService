package com.meizu.statsapp.v3.lib.plugin.emitter.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.meizu.statsapp.v3.lib.plugin.IVccOfflineStatsCallback;
import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmittableEvent;
import com.meizu.statsapp.v3.lib.plugin.emitter.Emitter;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterMessageBuilder;
import com.meizu.statsapp.v3.lib.plugin.emitter.local.storage.LocalEventStore;
import com.meizu.statsapp.v3.lib.plugin.net.HttpSecureRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.sdk.UmidFetcher;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil;
import com.meizu.statsapp.v3.lib.plugin.utils.Utils;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by huchen on 16-8-22.
 * 仅仅是远端的代理，不具体做事
 */
public class V3OfflineEmitter extends Emitter implements V3RemoteServiceRequester.IRemoteConnCallback {
    private final static String TAG = "V3OfflineEmitter";
    private VccOfflineStatsCallback mVccOfflineStatsCallback;
    private ScheduledExecutorService mExecutorService;
    private SharedPreferences mSP;
    private SharedPreferences.Editor mEditor;
    private long rowId = 0;
    private String mPackageName; //近端包名
    private boolean emitterConfigUpdateSuccessful = false; //pkgKey是否已经设置过去，兼容老版本的dataservice

    public V3OfflineEmitter(Context context, String pkgKey) {
        super(context, pkgKey);
        mExecutorService = Executors.newScheduledThreadPool(1);
        mPackageName = context.getPackageName();
        try {
            String processName = FlymeOSUtils.getCurProcessName(context);
            if (processName != null && !processName.equals(context.getPackageName())) {
                mSP = mContext.getSharedPreferences("com.meizu.statsapp.v3.events_cache_" + processName, Context.MODE_PRIVATE);
            } else {
                mSP = mContext.getSharedPreferences("com.meizu.statsapp.v3.events_cache", Context.MODE_PRIVATE);
            }
            mEditor = mSP.edit();

            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (mSP.getAll().size() >= 500) { //本地缓存了500条，直接删除
                        mEditor.clear().commit();
                    }

                    Log.d(TAG, "init thread:" + Thread.currentThread().getName());

                    for (Map.Entry<String, ?> entry : mSP.getAll().entrySet()) {
                        long x = Integer.parseInt(entry.getKey());
                        if (rowId < x) {
                            rowId = x;
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        mVccOfflineStatsCallback = new VccOfflineStatsCallback();
        V3RemoteServiceRequester.getInstance(context).setRemoteConnCallback(this);
    }

    @Override
    public void init() {
        Logger.d(TAG, "remoteInit, packageName; " + mPackageName + ", config: " + emitterConfig);
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                emitterConfigUpdateSuccessful = V3RemoteServiceRequester.getInstance(mContext).emitterUpdateConfig(mPackageName, emitterConfig);
                migrateOldEventsIfNecessary();
            }
        });
    }

    private void migrateOldEventsIfNecessary() { //迁移老数据，此方法只执行一次
        File file = mContext.getDatabasePath("statsapp_v3.db");
        if (file.exists()) {
            try {
                Logger.d(TAG, "migrateOldEventsIfNecessary begin");
                List<Long> ids = new ArrayList<>();
                List<TrackerPayload> payloads = new ArrayList<>();
                LocalEventStore eventStore = new LocalEventStore(mContext);
                for (EmittableEvent event : eventStore.getEventsMax500()) {
                    ids.add(event.getId());
                    payloads.add(event.getPayload());
                    eventStore.removeEvent(event.getId());
                }
                Logger.d(TAG, "migrate ids: " + Arrays.toString(ids.toArray()));
                bulkAdd(ids, payloads);
                mContext.deleteDatabase("statsapp_v3.db");
                Logger.d(TAG, "migrateOldEventsIfNecessary end");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void updateConfig(boolean active,
                             boolean flushOnStart, boolean flushOnReconnect, boolean flushOnCharge,
                             long flushDelayInterval, int flushCacheLimit, long flushMobileTrafficLimit,
                             int neartimeInterval) {
        super.updateConfig(active,
                flushOnStart, flushOnReconnect, flushOnCharge,
                flushDelayInterval, flushCacheLimit, flushMobileTrafficLimit,
                neartimeInterval);
        Logger.d(TAG, "remoteUpdateConfig, packageName; " + mPackageName + ", config: " + emitterConfig);
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                emitterConfigUpdateSuccessful = V3RemoteServiceRequester.getInstance(mContext).emitterUpdateConfig(mPackageName, emitterConfig);
            }
        });
    }

    @Override
    public void add(final TrackerPayload payload) {
        if (emitterConfig.isActive()) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "add thread:" + Thread.currentThread().getName());
                    if (!emitterConfigUpdateSuccessful) {
                        emitterConfigUpdateSuccessful = V3RemoteServiceRequester.getInstance(mContext).emitterUpdateConfig(mPackageName, emitterConfig);
                    }
                    rowId++;
                    Logger.d(TAG, "add rowId:" + rowId + ",payload:" + payload.toString());
                    boolean result = V3RemoteServiceRequester.getInstance(mContext).emitterAddEvent(mPackageName, rowId, payload);
                    mEditor.putString(String.valueOf(rowId), payload.toString()).commit();
                    if (mSP.getAll().size() >= 25) { //堆积了25条就自己发送了
                        sendCachedEventsIfNecessary();
                    }
                }
            });
        }
    }

    @Override
    public void addRealtime(final TrackerPayload payload) {
        if (emitterConfig.isActive()) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"addRealtime thread:"+Thread.currentThread().getName());
                    rowId++;
                    Logger.d(TAG, "addRealtime rowId:" + rowId + ",payload:" + payload.toString());
                    ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
                    emittableEvents.add(new EmittableEvent(null, rowId, payload));
                    boolean success = sendData(emittableEvents, UxipConstants.REALTIME_UPLOAD);
                    if (!success) {
                        Logger.d(TAG, "convert fail realtime event to batch event, eventId: " + rowId);
                        boolean result = V3RemoteServiceRequester.getInstance(mContext).emitterAddEvent(mPackageName, rowId, payload);
                        mEditor.putString(String.valueOf(rowId), payload.toString()).commit();
                    }
                }
            });
        }
    }

    @Override
    public void addNeartime(final TrackerPayload payload) {
        Logger.d(TAG, "addNeartime payload:" + payload.toString());
        addRealtime(payload);
    }

    @Override
    @Deprecated
    public void flush() {
        Logger.d(TAG, "flush");
        if (mSP.getAll().size() > 0) {
            Logger.d(TAG, "flush sp data");
            addCachedEventsToRemote(); //binder通信正常的情况下，尽量把近端的都发送过去
        }
    }

    @Override
    public void updateEventSource(final String sessionId, final String eventSource) {
        //更新远端数据库的eventSource
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                V3RemoteServiceRequester.getInstance(mContext).emitterUpdateEventSource(sessionId, eventSource);
            }
        });
    }

    @Override
    public void setEncrypt(boolean encrypt) {
    }

    @Override
    public String getUMID() {
        return V3RemoteServiceRequester.getInstance(mContext).emitterGetUmid(mPackageName);
    }

    @Override
    public void onServiceConnected() {
        V3RemoteServiceRequester.getInstance(mContext).setCallback(mPackageName, mVccOfflineStatsCallback);
        addCachedEventsToRemote();
    }

    @Override
    public void onServiceDisconnected() {
    }

    private class VccOfflineStatsCallback extends IVccOfflineStatsCallback.Stub {
        @Override
        public void onRealInsertEvent(String packageName, final long eventId) throws RemoteException {
            Logger.d(TAG, "onRealInsertEvent2Remote, eventId:" + eventId);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onRealInsertEvent thread:" + Thread.currentThread().getName());
                    mEditor.remove(String.valueOf(eventId)).commit(); //SharedPreferences是线程安全的
                }
            });
        }

        @Override
        public void onRealBulkInsertEvents(String packageName, final List eventIds) throws RemoteException {
            Logger.d(TAG, "onRealBulkInsertEvents, eventIds:" + (Arrays.toString(eventIds.toArray())));
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onRealBulkInsertEvents thread:" + Thread.currentThread().getName());
                    for (Long eventId : (List<Long>) eventIds) {
                        mEditor.remove(String.valueOf(eventId));
                    }
                    if (!eventIds.isEmpty()) {
                        mEditor.commit();
                    }
                }
            });
        }

        @Override
        public void onRealInsertH5Event(String packageName, long id) throws RemoteException {
//            Logger.d(TAG, "onRealInsertH5Event2Remote, id: " + id);
        }
    }

    private void sendCachedEventsIfNecessary() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "sendCachedEventsIfNecessary thread:" + Thread.currentThread().getName());
                ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
                int count = 0;
                for (Map.Entry<String, ?> entry : mSP.getAll().entrySet()) {
                    long x = Integer.parseInt(entry.getKey());
                    TrackerPayload payload = TrackerPayload.fromString((String) entry.getValue());
                    if (payload != null) {
                        emittableEvents.add(new EmittableEvent(null, x, payload));
                        count++;
                    }
                    if (count >= 200) { //一批次发送限制最多封装200个事件
                        break;
                    }
                }
                Logger.d(TAG, "number of cached events > 50, send " + emittableEvents.size() + " by myself");
                boolean success = sendData(emittableEvents, UxipConstants.BATCH_UPLOAD);
                if (success) {
                    for (EmittableEvent event : emittableEvents) {
                        mEditor.remove(String.valueOf(event.getId()));
                    }
                    if (!emittableEvents.isEmpty()) {
                        mEditor.commit();
                    }
                    Logger.d(TAG, "number of cached events > 50, sent successfully");
                }
            }
        });
    }

    private void addCachedEventsToRemote() { //把近端没有发送的数据传递到远端发送
        // FIFO Pattern for sending events
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"addCachedEventsToRemote thread:"+Thread.currentThread().getName());
                Logger.d(TAG, "addCachedEventsToRemote begin");
                Map<String, ?> allEntries = mSP.getAll();
                List<Long> ids = new ArrayList<>();
                List<TrackerPayload> payloads = new ArrayList<>();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    long x = Integer.parseInt(entry.getKey());
                    TrackerPayload payload = TrackerPayload.fromString((String) entry.getValue());
                    if (payload != null) {
                        ids.add(x);
                        payloads.add(payload);
                    }
                }
                bulkAdd(ids, payloads);
                Logger.d(TAG, "addCachedEventsToRemote end");
            }
        });
    }

    private void bulkAdd(List<Long> ids, List<TrackerPayload> payloads) {
        int j = 0;
        for (int i = 0; i < ids.size(); i++) {
            if (i != 0 && i % 10 == 0) {
                j++;
                int start = i - 10;
                boolean result = V3RemoteServiceRequester.getInstance(mContext).emitterBulkAddEvents(mPackageName, ids.subList(start, i), payloads.subList(start, i));
                Logger.d(TAG, "addCachedEventToRemote 1, eventIds:" + Arrays.toString(ids.subList(start, i).toArray()));
            }
        }
        int leftIndex = j * 10;
        if (leftIndex < ids.size()) {
            int end = ids.size();
            boolean result = V3RemoteServiceRequester.getInstance(mContext).emitterBulkAddEvents(mPackageName, ids.subList(leftIndex, end), payloads.subList(leftIndex, end));
            Logger.d(TAG, "addCachedEventToRemote 2, eventIds:" + Arrays.toString(ids.subList(leftIndex, end).toArray()));
        }
    }

    private boolean sendData(ArrayList<EmittableEvent> emittableEvents, String path) {
        //get umid
        String umid = UmidFetcher.getInstance(mContext).fetchOrRequestUMID();
        if (TextUtils.isEmpty(umid)) {
            Logger.d(TAG, "Not flushing data to Server because no umid");
            return false;
        }
        //get pkgKey
        String pkgKey = emitterConfig.getPkgKey();
        if (TextUtils.isEmpty(pkgKey)) {
            Logger.d(TAG, "Not flushing data to Server because no pkgKey");
            return false;
        }
        //add cseq for db _id
        ArrayList<Long> ids = new ArrayList<>();
        ArrayList<TrackerPayload> payloads = new ArrayList<>();
        for (int i = 0; i < emittableEvents.size(); i++) {
            EmittableEvent emittableEvent = emittableEvents.get(i);
            long cseq = emittableEvent.getId();
            emittableEvent.getPayload().add(Parameters.CSEQ, cseq);
            emittableEvent.getPayload().add(Parameters.UMID, umid);
            ids.add(emittableEvent.getId());
            payloads.add(emittableEvent.getPayload());
        }
        Logger.d(TAG, "sendData eventIds: " + Arrays.toString(ids.toArray()));
        String eventsData = EmitterMessageBuilder.buildEvents(payloads);
        byte[] origData = eventsData.getBytes();
        Logger.d(TAG, "origData size: " + origData.length);
        byte[] gzData = Utils.compress(origData);
        //send
        boolean success = false;
        String buildUri = buildUri(UxipConstants.UPLOAD_URL + pkgKey + path, gzData);
        Logger.d(TAG, "sendData buildUri " + buildUri);
        NetResponse response = HttpSecureRequester.getInstance(mContext).postMultipart(buildUri, null, gzData);
        if (null != response && response.getResponseBody() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.getResponseBody());
                int code = jsonObject.getInt(UxipConstants.API_RESPONSE_CODE);
                if (code == 200) {
                    success = true; // Delete events on any successful post, regardless of 1 or 0 response
                    Logger.d(TAG, "Successfully posted to " + UxipConstants.UPLOAD_URL + pkgKey + path);
                    Logger.d(TAG, "Response is: " + response);
                } else if (code == 415) {
                    Logger.d(TAG, "415 data error " + response);
                }
            } catch (JSONException e) {
                Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
            }
        }

        return success;
    }

    private String buildUri(String uri, byte[] eventsDataBytes) {
        Uri.Builder builder = Uri.parse(uri).buildUpon();
        Map<String, String> uriParam = new HashMap<>();

        String md5 = Utils.getMD5(eventsDataBytes);
        builder.appendQueryParameter(Parameters.UPLOAD_REQUEST_PARAM_MD5, md5);
        uriParam.put(Parameters.UPLOAD_REQUEST_PARAM_MD5, md5);

        //common
        long tsValue = System.currentTimeMillis() / 1000;
        String ts = String.valueOf(tsValue);
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_TS, ts);
        String nonce = String.valueOf(tsValue + new Random().nextInt());
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_NONCE, nonce);

        uriParam.put(Parameters.UXIP_REQUEST_PARAM_TS, ts);
        uriParam.put(Parameters.UXIP_REQUEST_PARAM_NONCE, nonce);
        builder.appendQueryParameter(Parameters.UXIP_REQUEST_PARAM_SIGN,
                NetRequestUtil.sign("POST", uri, uriParam, null));
        return builder.toString();
    }

}
