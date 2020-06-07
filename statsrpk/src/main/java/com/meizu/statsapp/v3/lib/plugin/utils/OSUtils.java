package com.meizu.statsapp.v3.lib.plugin.utils;

import android.os.Build;

/**
 * 获取非meizu的其他厂商手机系统信息
 */
public class OSUtils {

    public enum ROM {

        HUAWEI("huawei", "EMUI"), // 华为
        XIAOMI("xiaomi", "MIUI"), // 小米
        OPPO("oppo", "ColorOS"), // OPPO
        VIVO("vivo", "FuntouchOS"), // vivo
        GOOGLE("google", "Google"), // 原生
        SAMSUNG("samsung", "SamSung"), // 三星
        SMARTISAN("smartisan", "SmartisanOS"), // 锤子
        LETV("letv", "EUI"), // 乐视
        HTC("htc", "Sense"), // HTC
        ZTE("zte", "MiFavor"),//中兴
        ONEPLUS("oneplus", "H2OS"), // 一加
        YULONG("yulong", "YuLong"), // 酷派
        SONY("sony", "Sony"), // 索尼
        LENOVO("lenovo", "Lenovo"), // 联想
        LG("lg", "LG"), // LG
        OTHER("other", "UNKNOWN"); // CyanogenMod, Lewa OS, 百度云OS, Tencent OS, 深度OS, IUNI OS, Tapas OS, Mokee

        private String os;
        private String brand;

        ROM(String brand, String os) {
            this.os = os;
            this.brand = brand;
        }
    }

    public static String getOtherBrandOs() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        if (manufacturer.contains(ROM.HUAWEI.brand)) {//华为
            return ROM.HUAWEI.os;
        } else if (manufacturer.contains(ROM.XIAOMI.brand)) {//小米
            return ROM.XIAOMI.os;
        } else if (manufacturer.contains(ROM.OPPO.brand)) {//oppo
            return ROM.OPPO.os;
        } else if (manufacturer.contains(ROM.VIVO.brand)) {//vivo
            return ROM.VIVO.os;
        } else if (manufacturer.contains(ROM.SAMSUNG.brand)) {//三星
            return ROM.SAMSUNG.os;
        } else if (manufacturer.contains(ROM.SMARTISAN.brand)) {//锤子
            return ROM.SMARTISAN.os;
        } else if (manufacturer.contains(ROM.LG.brand)) {//LG
            return ROM.LG.os;
        } else if (manufacturer.contains(ROM.LETV.brand)) {//乐视
            return ROM.LETV.os;
        } else if (manufacturer.contains(ROM.ZTE.brand)) {//中兴
            return ROM.ZTE.os;
        } else if (manufacturer.contains(ROM.YULONG.brand)) {//酷派
            return ROM.YULONG.os;
        } else if (manufacturer.contains(ROM.LENOVO.brand)) {//联想
            return ROM.LENOVO.os;
        } else if (manufacturer.contains(ROM.SONY.brand)) {//索尼
            return ROM.SONY.os;
        } else if (manufacturer.contains(ROM.GOOGLE.brand)) {//原生
            return ROM.GOOGLE.os;
        } else if (manufacturer.contains(ROM.ONEPLUS.brand)) {//一加
            return ROM.ONEPLUS.os;
        } else if (manufacturer.contains(ROM.HTC.brand)) {//htc
            return ROM.HTC.os;
        } else {
            return ROM.OTHER.os;
        }
    }
}
