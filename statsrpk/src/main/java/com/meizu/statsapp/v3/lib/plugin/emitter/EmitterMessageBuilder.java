package com.meizu.statsapp.v3.lib.plugin.emitter;

import com.meizu.statsapp.v3.lib.plugin.constants.Parameters;
import com.meizu.statsapp.v3.lib.plugin.constants.UxipConstants;
import com.meizu.statsapp.v3.lib.plugin.payload.TrackerPayload;

import java.util.List;
import java.util.Map;

/**
 * Created by huchen on 16-8-26.
 */
public class EmitterMessageBuilder {
    protected static byte[] separate00 = new byte[]{0x00};
    protected static byte[] separate01 = new byte[]{0x01};
    protected static byte[] separate02 = new byte[]{0x02};
    protected static byte[] separate03 = new byte[]{0x03};
    protected static String fieldSeparate = new String(separate01);
    protected static String elementSeparate = new String(separate02);
    protected static String mapSeparate = new String(separate03);
    protected static String newlineSeparate = new String("\n");
    //protected static String newlineSeparate = new String(separate00);

    public static String buildEvents(List<TrackerPayload> payloads) {
        StringBuilder sb = new StringBuilder();
        sb.append(UxipConstants.EVENT_UPLOAD_MAJOR_VERSION);
        sb.append(UxipConstants.EVENT_UPLOAD_MIN_VERSION);
        for (TrackerPayload payload : payloads) {
            sb.append(parcelOneEvent(payload));
        }
        return sb.toString();
    }

    protected static String parcelOneEvent(TrackerPayload payload) {
        StringBuilder sb = new StringBuilder();
        sb.append(newlineSeparate);
        sb.append(UxipConstants.EVENT_UPLOAD_VARIANT_VERSION);

        parcelDevice(payload, sb);

        parcelApp(payload, sb);

        parcelEvent(payload, sb);

        return sb.toString();
    }

    protected static void parcelDevice(TrackerPayload payload, StringBuilder sb) {
        Object brand = payload.getMap().get(Parameters.BRAND);
        if (brand != null && brand instanceof String) {
            sb.append(brand);
        }
        sb.append(fieldSeparate);
        Object device = payload.getMap().get(Parameters.DEVICE);
        if (device != null && device instanceof String) {
            sb.append(device);
        }
        sb.append(fieldSeparate);
        Object product_model = payload.getMap().get(Parameters.PRODUCT_MODEL);
        if (product_model != null && product_model instanceof String) {
            sb.append(product_model);
        }
        sb.append(fieldSeparate);
        Object os_type = payload.getMap().get(Parameters.OS_TYPE);
        if (os_type != null && os_type instanceof String) {
            sb.append(os_type);
        }
        sb.append(fieldSeparate);
        Object os_version = payload.getMap().get(Parameters.OS_VERSION);
        if (os_version != null && os_version instanceof String) {
            sb.append(os_version);
        }
        sb.append(fieldSeparate);
        Object os = payload.getMap().get(Parameters.OS);
        if (os != null && os instanceof String) {
            sb.append(os);
        }
        sb.append(fieldSeparate);
        Object flyme_ver = payload.getMap().get(Parameters.FLYME_VER);
        if (flyme_ver != null && flyme_ver instanceof String) {
            sb.append(flyme_ver);
        }
        sb.append(fieldSeparate);
        Object build_mask = payload.getMap().get(Parameters.BUILD_MASK);
        if (build_mask != null && build_mask instanceof String) {
            sb.append(build_mask);
        }
        sb.append(fieldSeparate);
        Object umid = payload.getMap().get(Parameters.UMID);
        if (umid != null && umid instanceof String) {
            sb.append(umid);
        }
        sb.append(fieldSeparate);
        Object imei = payload.getMap().get(Parameters.IMEI);
        if (imei != null && imei instanceof String) {
            sb.append(imei);
        }
        sb.append(fieldSeparate);
        Object mac_address = payload.getMap().get(Parameters.MAC_ADDRESS);
        if (mac_address != null && mac_address instanceof String) {
            sb.append(mac_address);
        }
        sb.append(fieldSeparate);
        Object sn = payload.getMap().get(Parameters.SN);
        if (sn != null && sn instanceof String) {
            sb.append(sn);
        }
        sb.append(fieldSeparate);
        Object android_id = payload.getMap().get(Parameters.ANDROID_ID);
        if (android_id != null && android_id instanceof String) {
            sb.append(android_id);
        }
        sb.append(fieldSeparate);
        Object android_ad_id = payload.getMap().get(Parameters.ANDROID_AD_ID);
        if (android_ad_id != null && android_ad_id instanceof String) {
            sb.append(android_ad_id);
        }
        sb.append(fieldSeparate);
        Object imsi1 = payload.getMap().get(Parameters.IMSI1);
        if (imsi1 != null && imsi1 instanceof String) {
            sb.append(imsi1);
        }
        sb.append(fieldSeparate);
        Object imsi2 = payload.getMap().get(Parameters.IMSI2);
        if (imsi2 != null && imsi2 instanceof String) {
            sb.append(imsi2);
        }
        sb.append(fieldSeparate);
        Object ter_type = payload.getMap().get(Parameters.TER_TYPE);
        if (ter_type != null && ter_type instanceof Integer) {
            sb.append(ter_type);
        }
        sb.append(fieldSeparate);
        Object sre = payload.getMap().get(Parameters.SRE);
        if (sre != null && sre instanceof String) {
            sb.append(sre);
        }
        sb.append(fieldSeparate);
        Object lla = payload.getMap().get(Parameters.LLA);
        if (lla != null && lla instanceof String) {
            sb.append(lla);
        }
        sb.append(fieldSeparate);
        Object root = payload.getMap().get(Parameters.ROOT);
        if (root != null && root instanceof Boolean) {
            sb.append(booleanToInt((Boolean) root));
        }
        sb.append(fieldSeparate);
        Object flyme_uid = payload.getMap().get(Parameters.FLYME_UID);
        if (flyme_uid != null && flyme_uid instanceof String) {
            sb.append(flyme_uid);
        }
        sb.append(fieldSeparate);
        Object country = payload.getMap().get(Parameters.COUNTRY);
        if (country != null && country instanceof String) {
            sb.append(country);
        }
        sb.append(fieldSeparate);
        Object operator = payload.getMap().get(Parameters.OPERATOR);
        if (operator != null && operator instanceof String) {
            sb.append(operator);
        }
        sb.append(fieldSeparate);
        Object international = payload.getMap().get(Parameters.INTERNATIONAL);
        if (international != null && international instanceof Boolean) {
            sb.append(booleanToInt((Boolean) international));
        }
        sb.append(fieldSeparate);
    }

    protected static void parcelApp(TrackerPayload payload, StringBuilder sb) {
        Object pkg_type = payload.getMap().get(Parameters.PKG_TYPE);
        if (pkg_type != null && pkg_type instanceof Integer) {
            sb.append(pkg_type);
        }
        sb.append(fieldSeparate);
        Object pkg_name = payload.getMap().get(Parameters.PKG_NAME);
        if (pkg_name != null && pkg_name instanceof String) {
            sb.append(pkg_name);
        }
        sb.append(fieldSeparate);
        Object pkg_ver = payload.getMap().get(Parameters.PKG_VER);
        if (pkg_ver != null && pkg_ver instanceof String) {
            sb.append(pkg_ver);
        }
        sb.append(fieldSeparate);
        Object pkg_ver_code = payload.getMap().get(Parameters.PKG_VER_CODE);
        if (pkg_ver_code != null && pkg_ver_code instanceof Integer) {
            sb.append(pkg_ver_code);
        }
        sb.append(fieldSeparate);
        Object sdk_ver = payload.getMap().get(Parameters.SDK_VER);
        if (sdk_ver != null && sdk_ver instanceof String) {
            sb.append(sdk_ver);
        }
        sb.append(fieldSeparate);
        Object channel_id = payload.getMap().get(Parameters.CHANNEL_ID);
        if (channel_id != null && channel_id instanceof String) {
            sb.append(channel_id);
        }
        sb.append(fieldSeparate);
    }

    protected static void parcelEvent(TrackerPayload payload, StringBuilder sb) {
        Object source = payload.getMap().get(Parameters.SOURCE);
        if (source != null && source instanceof String) {
            sb.append(source);
        }
        sb.append(fieldSeparate);
        Object sessionId = payload.getMap().get(Parameters.SESSION_ID);
        if (sessionId != null && sessionId instanceof String) {
            sb.append(sessionId);
        }
        sb.append(fieldSeparate);
        Object network = payload.getMap().get(Parameters.NETWORK);
        if (network != null && network instanceof String) {
            sb.append(network);
        }
        sb.append(fieldSeparate);
        Object longitude = payload.getMap().get(Parameters.LONGITUDE);
        if (longitude != null && longitude instanceof Double) {
            sb.append(longitude);
        }
        sb.append(fieldSeparate);
        Object latitude = payload.getMap().get(Parameters.LATITUDE);
        if (latitude != null && latitude instanceof Double) {
            sb.append(latitude);
        }
        sb.append(fieldSeparate);
        Object page = payload.getMap().get(Parameters.PAGE);
        if (page != null && page instanceof String) {
            sb.append(page);
        }
        sb.append(fieldSeparate);
        Object launch = payload.getMap().get(Parameters.LAUNCH);
        if (launch != null && launch instanceof String) {
            sb.append(launch);
        }
        sb.append(fieldSeparate);
        Object type = payload.getMap().get(Parameters.TYPE);
        if (type != null && type instanceof String) {
            sb.append(type);
        }
        sb.append(fieldSeparate);
        Object name = payload.getMap().get(Parameters.NAME);
        if (name != null && name instanceof String) {
            sb.append(name);
        }
        sb.append(fieldSeparate);
        Object value = payload.getMap().get(Parameters.VALUE); //事件的自定义属性
        if (value != null && value instanceof Map) {
            boolean first = true;
            Map<String, String> valueMap = (Map<String, String>) value;
            for (Map.Entry<String, String> element : valueMap.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(elementSeparate);
                }
                if (element.getKey() != null) {
                    sb.append(element.getKey());
                }
                sb.append(mapSeparate);
                if (element.getValue() != null) {
                    sb.append(element.getValue());
                }
            }
        }
        sb.append(fieldSeparate);
        Object event_attrib = payload.getMap().get(Parameters.EVENT_ATTRIB);
        if (event_attrib != null && event_attrib instanceof Map) {
            boolean first = true;
            Map<String, String> event_attribMap = (Map<String, String>) event_attrib;
            for (Map.Entry<String, String> element : event_attribMap.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(elementSeparate);
                }
                if (element.getKey() != null) {
                    sb.append(element.getKey());
                }
                sb.append(mapSeparate);
                if (element.getValue() != null) {
                    sb.append(element.getValue());
                }
            }
        }
        sb.append(fieldSeparate);
        Object terminate = payload.getMap().get(Parameters.TERMINATE);
        if (terminate != null && terminate instanceof String) {
            sb.append(terminate);
        }
        sb.append(fieldSeparate);
        Object time = payload.getMap().get(Parameters.TIME);
        if (time != null && time instanceof Long) {
            sb.append(time);
        }
        sb.append(fieldSeparate);
        Object cseq = payload.getMap().get(Parameters.CSEQ);
        if (cseq != null && cseq instanceof Long) {
            sb.append(cseq);
        }
        sb.append(fieldSeparate);
        Object debug = payload.getMap().get(Parameters.DEBUG);
        if (debug != null && debug instanceof Boolean) {
            sb.append(booleanToInt((Boolean) debug));
        }
        sb.append(fieldSeparate);
        Object locTime = payload.getMap().get(Parameters.LOC_TIME);
        if (locTime != null && locTime instanceof Long) {
            sb.append(locTime);
        }
    }

    protected static int booleanToInt(boolean value) {
        if (value) {
            return 1;
        } else {
            return 0;
        }
    }

}
