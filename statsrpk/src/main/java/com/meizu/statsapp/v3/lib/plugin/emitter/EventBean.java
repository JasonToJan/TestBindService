package com.meizu.statsapp.v3.lib.plugin.emitter;


import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;
import com.meizu.statsapp.v3.lib.plugin.secure.SimpleCryptoAES;
import com.meizu.statsapp.v3.utils.log.Logger;

/**
 * Created by jinhui on 16-11-9.
 */

public class EventBean {
    private static String TAG = EventBean.class.getSimpleName();

    private final static String masterPassword = "76!t5#x04&^to3ek";

    long id;
    int encrypt;
    String sessionId;
    String eventSource;
    String eventData;
    String dateCreated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getEncrypt() {
        return encrypt;
    }

    public void setEncrypt(int encrypt) {
        this.encrypt = encrypt;
    }

    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public static TrackerPayload toPayload(EventBean eventBean) {
        String jsonString = null;
        int encryptType = eventBean.getEncrypt();
        if (encryptType != 0) {
            try {
                jsonString = SimpleCryptoAES.get().decrypt(masterPassword, eventBean.getEventData(), encryptType);
                Logger.d(TAG, "SimpleCryptoAES decrypt[" + encryptType + "] done");
            } catch (Exception e) {
                Logger.w(TAG, "SimpleCryptoAES decrypt[" + encryptType + "], Exception: " + e.toString() + " - Cause: " + e.getCause());
            }
        } else {
            jsonString = eventBean.getEventData();
        }
        TrackerPayload payload = TrackerPayload.fromString(jsonString);
        if (payload != null) {
            payload.add(Parameters.SESSION_ID, eventBean.getSessionId());
            payload.add(Parameters.SOURCE, eventBean.getEventSource());
            return payload;
        }
        return null;
    }

    /**
     * 从payload生成eventBean，生成的bean不含event_id
     *
     * @param encryptType
     * @param payload
     * @return
     */
    public static EventBean fromPayload(int encryptType, TrackerPayload payload) {
        EventBean eventBean = new EventBean();
        eventBean.setEncrypt(encryptType);
        String sessionId = (String) payload.getMap().get(Parameters.SESSION_ID);
        String eventSource = (String) payload.getMap().get(Parameters.SOURCE);
        eventBean.setSessionId(sessionId);
        eventBean.setEventSource(eventSource);
        //临时移除
        payload.getMap().remove(Parameters.SESSION_ID);
        payload.getMap().remove(Parameters.SOURCE);
        String eventData = null;
        int _encryptType = eventBean.getEncrypt();
        if (_encryptType != 0) {
            try {
                eventData = SimpleCryptoAES.get().encrypt(masterPassword, payload.toString(), _encryptType);
                Logger.d(TAG, "SimpleCryptoAES encrypt[" + _encryptType + "] done");
            } catch (Exception e) {
                Logger.w(TAG, "SimpleCryptoAES encrypt[" + _encryptType + "], Exception: " + e.toString() + " - Cause: " + e.getCause());
            }
        } else {
            eventData = payload.toString();
        }
        eventBean.setEventData(eventData);
        //加回去
        payload.add(Parameters.SESSION_ID, sessionId);
        payload.add(Parameters.SOURCE, eventSource);
        return eventBean;
    }

//    /**
//     * Converts an event map to a byte
//     * array for storage.
//     *
//     * @param map the map containing all
//     *            the event parameters
//     * @return the byte array or null
//     */
//    private static byte[] serialize(Map<String, String> map) {
//        try {
//            ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
//            ObjectOutputStream out = new ObjectOutputStream(mem_out);
//            out.writeObject(map);
//            out.close();
//            mem_out.close();
//            return mem_out.toByteArray();
//        } catch (IOException e) {
//            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
//        }
//        return null;
//    }

//    /**
//     * Converts a byte array back into an
//     * event map for sending.
//     *
//     * @param bytes the bytes to be converted
//     * @return the Map or null
//     */
//    @SuppressWarnings("unchecked")
//    private static Map<String, String> deserializer(byte[] bytes) {
//        try {
//            ByteArrayInputStream mem_in = new ByteArrayInputStream(bytes);
//            ObjectInputStream in = new ObjectInputStream(mem_in);
//            Map<String, String> map = (HashMap<String, String>) in.readObject();
//            in.close();
//            mem_in.close();
//            return map;
//        } catch (ClassNotFoundException | IOException e) {
//            Logger.w(TAG, "Exception: " + e.toString() + " - Cause: " + e.getCause());
//        }
//        return null;
//    }

}
