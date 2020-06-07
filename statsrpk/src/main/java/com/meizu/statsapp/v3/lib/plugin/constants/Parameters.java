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
 * All of the keys for each type of event
 * that can be sent by the Tracker.
 */
public class Parameters {
    // DeviceInfo Context
    public static final String DEVICE = "device";
    public static final String IMEI = "imei";
    public static final String COUNTRY = "country";
    public static final String OPERATOR = "operator";
    public static final String INTERNATIONAL = "international";
    public static final String ROOT = "root";
    public static final String SN = "sn";
    public static final String FLYME_UID = "flyme_uid";
    public static final String FLYME_VER = "flyme_ver";
    public static final String MAC_ADDRESS = "mac_address";
    public static final String PRODUCT_MODEL = "product_model";
    public static final String BUILD_MASK = "build_mask";
    public static final String SRE = "sre";
    public static final String LLA = "lla";
    public static final String UMID = "umid";
    public static final String TER_TYPE = "ter_type";
    public static final String OS_TYPE = "os_type";
    public static final String BRAND = "brand";
    public static final String OS_VERSION = "os_version";
    public static final String OS = "os";
    public static final String ANDROID_ID = "android_id";
    public static final String ANDROID_AD_ID = "android_ad_id";
    public static final String IMSI1 = "imsi1";
    public static final String IMSI2 = "imsi2";
    public static final String DEBUG = "debug";
    public static final String CSEQ = "cseq"; //即event_id
    //AppInfo Context
    public static final String PKG_NAME = "pkg_name";
    public static final String SDK_VER = "sdk_ver";
    public static final String PKG_VER = "pkg_ver";
    public static final String PKG_VER_CODE = "pkg_ver_code";
    public static final String PKG_KEY = "pkg_key";
    public static final String PKG_TYPE = "pkg_type";
    //SessionInfo Context
    public static final String CHANNEL_ID = "channel_id";
    public static final String SOURCE = "source";
    public static final String SESSION_ID = "sid";
    //EventInfo Context
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIME = "time";
    public static final String VALUE = "value"; //事件自定义的属性
    //log event
    //action_x event
    public static final String NETWORK = "network";
    public static final String PAGE = "page";
    public static final String EVENT_ATTRIB = "event_attrib";
    //page event
    public static final String LAUNCH = "launch";
    public static final String TERMINATE = "terminate";

    //location time
    public static final String LOC_TIME = "loc_time";

    //UXIP request parameter
    public static final String UXIP_REQUEST_PARAM_TS = "ts";
    public static final String UXIP_REQUEST_PARAM_NONCE = "nonce";
    public static final String UXIP_REQUEST_PARAM_SIGN = "sign";

    //upload request parameter
    public static final String UPLOAD_REQUEST_PARAM_MD5 = "md5";
}
