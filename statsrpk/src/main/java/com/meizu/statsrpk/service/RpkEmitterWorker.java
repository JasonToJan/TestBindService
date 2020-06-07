package com.meizu.statsrpk.service;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmittableEvent;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterMessageBuilder;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterWorker;
import com.meizu.statsapp.v3.lib.plugin.net.HttpSecureRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.receiver.EnvironmentReceiver;
import com.meizu.statsapp.v3.lib.plugin.sdk.UmidFetcher;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil;
import com.meizu.statsapp.v3.lib.plugin.utils.Utils;
import com.meizu.statsapp.v3.utils.log.Logger;
import com.meizu.statsrpk.storage.RpkEventStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.meizu.statsapp.v3.lib.plugin.receiver.EnvironmentReceiver.CHANGE_NAME_NETWORKCONNECT;
import static com.meizu.statsapp.v3.lib.plugin.receiver.EnvironmentReceiver.CHANGE_NAME_POWER;

/**
 * Created by huchen on 16-9-7.
 */
public class RpkEmitterWorker extends EmitterWorker implements EnvironmentReceiver.IEnvListener {
    private final static String TAG = RpkEmitterWorker.class.getSimpleName();
    private RpkEventStore eventStore;
    private ScheduledExecutorService executorService;

    public RpkEmitterWorker(Context context, int threadCount) {
        super(context);
        this.executorService = Executors.newScheduledThreadPool(threadCount);
        this.eventStore = new RpkEventStore(context);
        EnvironmentReceiver.getInstance(context).addEnvListener(this);

        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                flushQueueInternalByTimer();
            }
        }, 30 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    public void add(final String appKey, final String rpkPkgName, final TrackerPayload payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                enqueueEventsInternal(appKey, rpkPkgName, payload);
            }
        });
    }

    public void environmentChanged(final String changeName) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                environmentChangedInternal(changeName);
            }
        });
    }

    private void environmentChangedInternal(String changeName) {
        Logger.d(TAG, "environmentChanged. changeName: " + changeName);
        List<String> appKeys = eventStore.getAppKeys();
        if (CHANGE_NAME_NETWORKCONNECT.equals(changeName)) {
            for (String appKey : appKeys) {
                flushQueueInternalWhenEnvChanged(appKey);
            }
        } else if (CHANGE_NAME_POWER.equals(changeName)) {
            for (String appKey : appKeys) {
                flushQueueInternalWhenEnvChanged(appKey);
            }
        }
    }

    private void enqueueEventsInternal(String appKey, String rpkPkgName, TrackerPayload payload) {
        if (appKey == null || payload == null) {
            return;
        }
        Logger.d(TAG, "Queuing event for sending later, appKey:" + appKey + ", payload:" + payload);
        eventStore.clearOldEventsIfNecessary();
        long inserted = eventStore.insertEvent(appKey, rpkPkgName, payload);
        if (inserted > 0 && cacheCheck(appKey)) {
            ArrayList<EmittableEvent> emittableEvents = eventStore.getEmittableEvents(appKey);
            sendDataBatch(appKey, emittableEvents);
        }
    }

    private void sendDataBatch(String appKey, ArrayList<EmittableEvent> emittableEvents) {
        if (emittableEvents == null || emittableEvents.size() == 0 || TextUtils.isEmpty(appKey)) {
            return;
        }

        //get umid
        String umid = UmidFetcher.getInstance(context).fetchOrRequestUMID();
        if (TextUtils.isEmpty(umid)) {
            Logger.d(TAG, "Not flushing data to Server because no umid");
            return;
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
        Logger.d(TAG, "sendBatch eventIds: " + Arrays.toString(ids.toArray()));

        //sendCheck
        String eventsData = EmitterMessageBuilder.buildEvents(payloads);
        byte[] gzData = Utils.compress(eventsData.getBytes());

        //send
        boolean deleteEvents = false;
        String buildUri = buildUri(UxipConstants.UPLOAD_URL + appKey + UxipConstants.BATCH_UPLOAD, gzData);
        Logger.d(TAG, "sendDataBatch buildUri " + buildUri);
        NetResponse response = HttpSecureRequester.getInstance(context).postMultipart(
                buildUri,
                null, gzData);
        if (null != response && response.getResponseBody() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.getResponseBody());
                int code = jsonObject.getInt(UxipConstants.API_RESPONSE_CODE);
                if (code == 200) {
                    deleteEvents = true; // Delete events on any successful post, regardless of 1 or 0 response
                    Logger.d(TAG, "Successfully posted to " +
                            UxipConstants.UPLOAD_URL + appKey + UxipConstants.BATCH_UPLOAD);
                    Logger.d(TAG, "Response is: " + response);
                } else if (code == 415) {
                    deleteEvents = true; // Delete events on any successful post, regardless of 1 or 0 response
                    Logger.d(TAG, "415 data error " + response);
                }
            } catch (JSONException e) {
                Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
            }
        }
        if (deleteEvents) {
            Logger.d(TAG, "deleting sent events from DB.");
            for (long sentId : ids) {
                eventStore.removeEvent(appKey, sentId);
            }
        } else {
            Logger.d(TAG, "Response is null or failed, unexpected failure posting to " +
                    UxipConstants.UPLOAD_URL + appKey + UxipConstants.BATCH_UPLOAD);
        }
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

    /**
     * 环境变更（网络，充电）或者手工触发时，上报事件
     *
     * @param appKey
     */
    private void flushQueueInternalWhenEnvChanged(String appKey) {
        Logger.d(TAG, "flushQueueInternalWhenEnvChanged, appKey: " + appKey);
        eventStore.clearOldEventsIfNecessary();
        ArrayList<EmittableEvent> emittableEvents = eventStore.getEmittableEvents(appKey);
        sendDataBatch(appKey, emittableEvents);
    }

    /**
     * 计时器到点或者手工触发时，上报事件
     */
    private void flushQueueInternalByTimer() {
        Logger.d(TAG, "flushQueueInternalByTimer");
        List<String> appKeys = eventStore.getAppKeys();
        for (String appKey : appKeys) {
            eventStore.clearOldEventsIfNecessary();
            ArrayList<EmittableEvent> emittableEvents = eventStore.getEmittableEvents(appKey);
            sendDataBatch(appKey, emittableEvents);
        }

        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                flushQueueInternalByTimer();
            }
        }, 30 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查是否已经缓存到足够条数去批量上报，默认是单个appkey有5条信息就进行上报
     *
     * @return
     */
    private boolean cacheCheck(String appKey) {
        long eventSize = eventStore.getEventsCountForAppKey(appKey);
        int flushCacheLimit = 5;
        boolean networkAvailable = NetInfoUtils.isOnline(context);
        Logger.d(TAG, "cacheCheck appKey:" + appKey + " ------------------ eventSize:" + eventSize + ", flushCacheLimit:" + flushCacheLimit + ", networkAvailable:" + networkAvailable);
        return eventSize >= flushCacheLimit && networkAvailable;
    }
}
