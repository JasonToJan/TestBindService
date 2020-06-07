package com.meizu.statsapp.v3.lib.plugin.emitter.local;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.meizu.statsapp.v3.InitConfig;
import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmittableEvent;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterConfig;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterMessageBuilder;
import com.meizu.statsapp.v3.lib.plugin.emitter.EmitterWorker;
import com.meizu.statsapp.v3.lib.plugin.emitter.EventBean;
import com.meizu.statsapp.v3.lib.plugin.emitter.local.storage.LocalEventStore;
import com.meizu.statsapp.v3.lib.plugin.net.HttpSecureRequester;
import com.meizu.statsapp.v3.lib.plugin.net.NetResponse;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.receiver.EnvironmentReceiver;
import com.meizu.statsapp.v3.lib.plugin.sdk.UmidFetcher;
import com.meizu.statsapp.v3.lib.plugin.utils.FlymeOSUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetInfoUtils;
import com.meizu.statsapp.v3.lib.plugin.utils.NetRequestUtil;
import com.meizu.statsapp.v3.lib.plugin.utils.Utils;
import com.meizu.statsapp.v3.utils.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.meizu.statsapp.v3.lib.plugin.receiver.EnvironmentReceiver.CHANGE_NAME_NETWORKCONNECT;
import static com.meizu.statsapp.v3.lib.plugin.receiver.EnvironmentReceiver.CHANGE_NAME_POWER;

/**
 * Created by huchen on 16-9-7.
 */
public class LocalEmitterWorker extends EmitterWorker implements EnvironmentReceiver.IEnvListener {
    private final static String TAG = "LocalEmitterWorker";
    private LocalEventStore eventStore;
    private ScheduledExecutorService executorService;
    private EmitterConfig emitterConfig;

    private AtomicBoolean isBusy;
    private Handler normaSendHandler, realtimeSendHandler, neartimeSendHandler;
    private CopyOnWriteArrayList<Long> realTimeIds, nearTimeIds;
    private final int NORMASEND_START = 0x1;
    private final int NORMASEND_THRESHOLD = 0x2;
    private final int NORMASEND_NET = 0x3;
    private final int NORMASEND_POWER = 0x4;
    private final int NORMASEND_TIMER = 0x5;
    private final int NORMASEND_BACKGROUND = 0x6;
    private final int NEARTIME = 0x1;

    public LocalEmitterWorker(final Context context, EmitterConfig config) {
        super(context);
        this.emitterConfig = config;
        this.executorService = Executors.newScheduledThreadPool(1);
        long t1 = System.currentTimeMillis();
        this.eventStore = new LocalEventStore(context);
        Logger.d(TAG, "##### LocalEmitterWorker, " + (System.currentTimeMillis() - t1));

        isBusy = new AtomicBoolean(false);
        realTimeIds = new CopyOnWriteArrayList<>(new ArrayList<Long>());
        nearTimeIds = new CopyOnWriteArrayList<>(new ArrayList<Long>());
        HandlerThread thread1 = new HandlerThread("LocalEmitterWorker.normaSend");
        thread1.start();
        normaSendHandler = new Handler(thread1.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                eventStore.clearOldEventsIfNecessary();
                if (!FlymeOSUtils.kaiJiXiangDao(context)) {
                    Logger.d(TAG, "EmitterWorker NORMASEND --> 还未完成开机向导");
                    return;
                }

                if (!NetInfoUtils.isOnline(context)) {
                    Logger.d(TAG, "EmitterWorker NORMASEND no network");
                    return;
                }

                if (msg.what == NORMASEND_START) {
                    ArrayList<EmittableEvent> emittableEvents = eventStore.getEmittableEvents();
                    if (emittableEvents.size() >= 6) { //功耗优化，避免一个_bootup_事件触发一次上报
                        normalSend(emittableEvents);
                        Logger.d(TAG, "EmitterWorker NORMASEND_" + msg.what);
                    }
                } else if (msg.what == NORMASEND_THRESHOLD) {
                    if (cacheCheck()) { //到底阈值上报
                        ArrayList<EmittableEvent> emittableEvents = eventStore.getEmittableEvents();
                        normalSend(emittableEvents);
                        Logger.d(TAG, "EmitterWorker NORMASEND_" + msg.what);
                    }
                } else if (msg.what == NORMASEND_NET || msg.what == NORMASEND_POWER
                        || msg.what == NORMASEND_TIMER || msg.what == NORMASEND_BACKGROUND) {
                    ArrayList<EmittableEvent> emittableEvents = eventStore.getEmittableEvents();
                    normalSend(emittableEvents);
                    Logger.d(TAG, "EmitterWorker NORMASEND_" + msg.what);
                }
                normaSendHandler.removeMessages(NORMASEND_TIMER);
                if (emitterConfig.getFlushDelayInterval() > 0) {
                    normaSendHandler.sendEmptyMessageDelayed(NORMASEND_TIMER, emitterConfig.getFlushDelayInterval());
                }
            }
        };
        HandlerThread thread2 = new HandlerThread("LocalEmitterWorker.realtimeSend");
        thread2.start();
        realtimeSendHandler = new Handler(thread2.getLooper());
        HandlerThread thread3 = new HandlerThread("LocalEmitterWorker.neartimeSend");
        thread3.start();
        neartimeSendHandler = new Handler(thread3.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (!FlymeOSUtils.kaiJiXiangDao(context)) {
                    Logger.d(TAG, "EmitterWorker NEARTIME SEND --> 还未完成开机向导");
                    return;
                }
                if (msg.what == NEARTIME) {
                    ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
                    ArrayList<Long> ids = new ArrayList<>();
                    for (long id : nearTimeIds) {
                        EventBean eventBean = eventStore.getEventByRowId(id);
                        if (eventBean == null) continue;
                        TrackerPayload payload = EventBean.toPayload(eventBean);
                        if (payload != null) {
                            emittableEvents.add(new EmittableEvent("", id, payload));
                            ids.add(id);
                        }
                    }
                    if (InitConfig.sendEventSync) {
                        syncSendData(emittableEvents, false, UxipConstants.REALTIME_UPLOAD);
                    } else {
                        sendData(emittableEvents, false, UxipConstants.REALTIME_UPLOAD);
                    }
                    nearTimeIds.removeAll(ids);
                    Logger.d(TAG, "EmitterWorker NEARTIME SEND");
                }
            }
        };

        EnvironmentReceiver.getInstance(context).addEnvListener(this);
        resetMobileTrafficIf();
        Logger.d(TAG, "##### LocalEmitterWorker 2, " + (System.currentTimeMillis() - t1));
    }

    public void init() {
        Logger.d(TAG, "EmitterWorker init");
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (emitterConfig.isFlushOnStart()) {
                    normaSendHandler.sendEmptyMessage(NORMASEND_START);
                }
                if (emitterConfig.getFlushDelayInterval() > 0) {
                    normaSendHandler.sendEmptyMessageDelayed(NORMASEND_TIMER, emitterConfig.getFlushDelayInterval());
                }
            }
        });
    }

    public void updateConfig(final EmitterConfig config) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                emitterConfig = config;
                normaSendHandler.removeMessages(NORMASEND_TIMER);
                if (emitterConfig.getFlushDelayInterval() > 0) {
                    normaSendHandler.sendEmptyMessageDelayed(NORMASEND_TIMER, emitterConfig.getFlushDelayInterval());
                }
            }
        });
    }

    public void add(final TrackerPayload payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                eventStore.insertEvent(payload);
                normaSendHandler.sendEmptyMessage(NORMASEND_THRESHOLD);
            }
        });
    }

    public void addRealtime(final TrackerPayload payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long id = eventStore.insertEvent(payload);
                realTimeIds.add(id);
                Logger.d(TAG, "insert realtime event id:" + id);

                if (!FlymeOSUtils.kaiJiXiangDao(context)) {
                    Logger.d(TAG, "EmitterWorker REALTIME SEND --> 还未完成开机向导");
                    return;
                }
                realtimeSendHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (long id : realTimeIds) {
                            ArrayList<EmittableEvent> emittableEvents = new ArrayList<>();
                            EventBean eventBean = eventStore.getEventByRowId(id);
                            if (eventBean == null)
                                continue;
                            emittableEvents.add(new EmittableEvent("", eventBean.getId(), payload));
                            Logger.d(TAG, "realtime send");
                            if (InitConfig.sendEventSync) {
                                syncSendData(emittableEvents, false, UxipConstants.REALTIME_UPLOAD);
                            } else {
                                sendData(emittableEvents, false, UxipConstants.REALTIME_UPLOAD);
                            }
                            realTimeIds.remove(id);
                        }

                        //realTimeIds.clear();
                    }
                });
            }
        });
    }

    public void addNeartime(final TrackerPayload payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                long id = eventStore.insertEvent(payload);
                nearTimeIds.add(id);
                if (!neartimeSendHandler.hasMessages(NEARTIME)) {
                    neartimeSendHandler.sendEmptyMessageDelayed(NEARTIME, emitterConfig.getNeartimeInterval() * 1000);
                }
            }
        });
    }

    public void flush() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                normaSendHandler.sendEmptyMessage(NORMASEND_BACKGROUND);
            }
        });
    }

    @Override
    public void environmentChanged(final String changeName) {
        Logger.d(TAG, "environmentChanged. changeName: " + changeName);
        if (CHANGE_NAME_NETWORKCONNECT.equals(changeName)) {
            if (emitterConfig.isFlushOnReconnect()) {
                normaSendHandler.sendEmptyMessage(NORMASEND_NET);
            }
        } else if (CHANGE_NAME_POWER.equals(changeName)) {
            if (emitterConfig.isFlushOnCharge()) {
                normaSendHandler.sendEmptyMessage(NORMASEND_POWER);
            }
        }
    }

    private void normalSend(ArrayList<EmittableEvent> emittableEvents) {
        //移除实时发送的事件id
        Iterator<EmittableEvent> it = emittableEvents.iterator();
        while (it.hasNext()) {
            EmittableEvent emittableEvent = it.next();
            if (realTimeIds.contains(emittableEvent.getId()) || nearTimeIds.contains(emittableEvent.getId())) {
                it.remove();
            }
        }
        Logger.d(TAG, "normalSend");
        if (InitConfig.sendEventSync) {
            syncSendData(emittableEvents, true, UxipConstants.BATCH_UPLOAD);
        } else {
            sendData(emittableEvents, true, UxipConstants.BATCH_UPLOAD);
        }
    }

    private void syncSendData(ArrayList<EmittableEvent> emittableEvents, boolean mobileCheck, String path) {
        if (isBusy.compareAndSet(false, true)) {
            sendData(emittableEvents, mobileCheck, path);
            isBusy.compareAndSet(true, false);
        }
    }

    private void sendData(ArrayList<EmittableEvent> emittableEvents, boolean mobileCheck, String path) {
        if (emittableEvents == null || emittableEvents.size() == 0) {
            return;
        }
        //get umid
        String umid = UmidFetcher.getInstance(context).fetchOrRequestUMID();
        if (TextUtils.isEmpty(umid)) {
            Logger.d(TAG, "Not flushing data to Server because no umid");
            return;
        }
        //get pkgKey
        String pkgKey = emitterConfig.getPkgKey();
        if (TextUtils.isEmpty(pkgKey)) {
            Logger.d(TAG, "Not flushing data to Server because no pkgKey");
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
        Logger.d(TAG, "sendData eventIds: " + Arrays.toString(ids.toArray()));
        //mobileTrafficCheck
        String eventsData = EmitterMessageBuilder.buildEvents(payloads);
        byte[] origData = eventsData.getBytes();
        Logger.d(TAG, "origData size: " + origData.length);
        byte[] gzData = Utils.compress(origData);
        if (mobileCheck && !mobileTrafficCheck(gzData)) {
            return;
        }
        //send
        boolean deleteEvents = false;
        String buildUri = buildUri(UxipConstants.UPLOAD_URL + pkgKey + path, gzData);
        Logger.d(TAG, "sendData buildUri " + buildUri);
        NetResponse response = HttpSecureRequester.getInstance(context).postMultipart(buildUri, null, gzData);
        if (null != response && response.getResponseBody() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.getResponseBody());
                int code = jsonObject.getInt(UxipConstants.API_RESPONSE_CODE);
                if (code == 200) {
                    deleteEvents = true; // Delete events on any successful post, regardless of 1 or 0 response
                    Logger.d(TAG, "Successfully posted to " + UxipConstants.UPLOAD_URL + pkgKey + path);
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
                eventStore.removeEvent(sentId);
            }
        } else {
            Logger.d(TAG, "Response is null or failed, unexpected failure posting to " +
                    UxipConstants.UPLOAD_URL + pkgKey + path);
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
     * 检查是否已经缓存到足够条数去批量上报
     *
     * @return
     */
    private boolean cacheCheck() {
        long eventSize = eventStore.getEventsCount(null);
        int flushCacheLimit = emitterConfig.getFlushCacheLimit();
        Logger.d(TAG, "cacheCheck ------------------ eventSize:" + eventSize + ", flushCacheLimit:" + flushCacheLimit);
        return eventSize >= flushCacheLimit;
    }

    /**
     * 重置流量如果条件满足
     */
    private void resetMobileTrafficIf() {
        //reset traffic
        long now = System.currentTimeMillis();
        long lastResetTime = eventStore.getLastResetTime();
        long intervalTime = Math.abs(now - lastResetTime);
        long resetTrafficInterval = UxipConstants.DAILY_MILLISENCOND;
        Logger.d(TAG, "beforeFlush ------------------ now:" + now + ", lastResetTime:" + lastResetTime + ", intervalTime:" + intervalTime + ", resetTrafficInterval:" + resetTrafficInterval);
        if (intervalTime >= resetTrafficInterval) {
            Logger.d(TAG, "do reset traffic");
            eventStore.updateTraffic(0);
            eventStore.updateLastResetTime(now);
        }
    }

    /**
     * 检查移动网络下流量是否超标
     *
     * @param eventsDataBytes
     * @return
     */
    private boolean mobileTrafficCheck(byte[] eventsDataBytes) {
        int flushSize = eventsDataBytes.length;
        Logger.d(TAG, "mobileTrafficCheck ------------------ flushSize:" + flushSize);
        if (flushSize == 0) {
            Logger.d(TAG, "Not flushing data to Server because no flush data");
            return false;
        }
        boolean isWifi = NetInfoUtils.isWiFiWorking(context);
        int currentTraffic = eventStore.getTraffic();
        long limit = emitterConfig.getFlushMobileTrafficLimit();
        Logger.d(TAG, "mobileTrafficCheck ------------------ isWifi:" + isWifi + ", currentTraffic:" + currentTraffic + ", mobileTrafficLimit:" + limit);
        //traffic check in
        if (!isWifi) {
            if (limit < 0) { //负数表示没有限制
                return true;
            } else if (currentTraffic + flushSize > limit) {
                Logger.d(TAG, "Not flushing data to server because exceed mobileTrafficLimit");
                return false;
            } else {
                eventStore.updateTraffic(currentTraffic + flushSize);
                Logger.d(TAG, "flushing data to server currentTraffic:" + currentTraffic + ", flushSize:" + flushSize);
            }
        } else {
            Logger.d(TAG, "flushing data to server in WiFi mode");
        }
        return true;
    }

    boolean updateEventSource(String sessionId, String eventSource) {
        return eventStore.updateEventSource(sessionId, eventSource);
    }

    public void setEncrypt(boolean encrypt) {
        eventStore.setEncrypt(encrypt);
    }

}
