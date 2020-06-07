/*
 * Copyright (c) 2015 Snowplow Analytics Ltd. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.meizu.statsapp.v3.lib.plugin.constants;

/**
 * Constants which apply to schemas, event types
 * and sending protocols.
 */
public class UxipConstants {
    //OS TYPE
    public static final String OS_TYPE = "android";

    //time
    public static final long THREE_DAYS_MILLISENCOND = 3 * 24 * 60 * 60 * 1000;
    public static final long DAILY_MILLISENCOND = 24 * 60 * 60 * 1000;
    public static final long HOUR_MILLISENCOND = 60 * 60 * 1000;

    //emitterThreadCount
    public static final int EMITTER_SERVER_THREAD_COUNT = 1;

    //api address
    public static String UPLOAD_URL = "http://uxip.meizu.com/api/v3/event/";
    public static final String BATCH_UPLOAD = "/batch";
    public static final String REALTIME_UPLOAD = "/realtime";
    public static String GET_UMID_URL = "http://uxip-config.meizu.com/api/v3/umid";
    public static String GET_CONFIG_URL = "http://uxip-res.meizu.com/resource/v3/config/"; //CDN获取配置模式
    //rpk url
    public static final String RPK_CONFIG_URL = "http://uxip-res.meizu.com/resource/v3/config/rpk/";
    public static final String API_RESPONSE_CODE = "code";
    public static final String API_RESPONSE_VALUE = "value";

    //umid request constants
    public static final String UMID_SECRET_KEY = "OjUiuYe80AUYnbgBNT6";
    public static final String UMID_RESPONSE_KEY_UMID = "umid";
    public static final String UMID_RESPONSE_KEY_IMEI = "imei";

    //uxip version
    public static final String EVENT_UPLOAD_MAJOR_VERSION = "03";
    public static final String EVENT_UPLOAD_MIN_VERSION = "2";
    public static final String EVENT_UPLOAD_VARIANT_VERSION = "0";

    //PREFERENCES common
    public static final String PREFERENCES_COMMON_NAME = "com.meizu.statsapp.v3.common";
    public final static String PREFERENCES_KEY_DAILY_ACTIVED_LAST = "DAILY_ACTIVED_LAST";
    public final static String PREFERENCES_KEY_GLOBAL_ACTIVED = "GLOBAL_ACTIVED";
    public static final String PREFERENCES_KEY_POSITION_INTERVAL = "POSITION_INTERVAL";

    //config response constants
    public final static String PREFERENCES_SERVER_CONFIG_NAME = "com.meizu.statsapp.v3.serverconfig";
    public final static String PREFERENCES_KEY_RESPONSE = "response";
    public final static String PREFERENCES_KEY_GET_TIME = "getTime";
    public static final String RESPONSE_KEY_VERSION = "version";
    public static final String RESPONSE_KEY_ACTIVE = "active";
    public static final String RESPONSE_KEY_SAMPLING = "sampling";
    public static final String RESPONSE_KEY_UPLOADPOLICY = "uploadPolicy";
    public static final String RESPONSE_KEY_UPLOADPOLICY_ONSTART = "onStart";
    public static final String RESPONSE_KEY_UPLOADPOLICY_ONCHARGE = "onCharge";
    public static final String RESPONSE_KEY_UPLOADPOLICY_ONRECONNECT = "onReconnect";
    public static final String RESPONSE_KEY_UPLOADPOLICY_INTERVAL = "interval";
    public static final String RESPONSE_KEY_UPLOADPOLICY_MOBILEQUOTA = "mobileQuota";
    public static final String RESPONSE_KEY_UPLOADPOLICY_CACHECAPACITY = "cacheCapacity";
    public static final String RESPONSE_KEY_UPLOADPOLICY_NEARTIME_INTERVAL = "neartimeInterval";
    public static final String RESPONSE_KEY_EVENTS = "events";
    public static final String RESPONSE_KEY_EVENTS_NAME = "name";
    public static final String RESPONSE_KEY_EVENTS_ACTIVE = "active";
    public static final String RESPONSE_KEY_EVENTS_REALTIME = "realtime";
    public static final String RESPONSE_KEY_EVENTS_NEARTIME = "neartime";
    public static final String RESPONSE_KEY_POSITIONING_INTERVAL = "positioningInterval";

    //emitter config PREFERENCES
    public final static String PREFERENCES_EMITTER_CONFIG_NAME = "com.meizu.statsapp.v3.emitterconfig";

    //发送方式， 1-非实时批量， 2-单个实时， 3-批量delay实时
    public static final int EVENT_INACTIVE = -1;
    public static final int SEND_NORMA = 0x1;
    public static final int SEND_REALTIME = 0x2;
    public static final int SEND_NEARTIME = 0x3;

//    http://uxip.in.meizu.com
//    http://uxip-config.in.meizu.com
//    http://uxip-res.in.meizu.com
}
