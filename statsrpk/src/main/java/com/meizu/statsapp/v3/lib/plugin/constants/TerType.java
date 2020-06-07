package com.meizu.statsapp.v3.lib.plugin.constants;

/**
 * Created by jinhui on 16-12-19.
 */

public enum TerType {
    PHONE(1), //手机
    FLYME_TV(2), //Flyme TV
    PAD(3); //pad

    private final int type;

    TerType(int type) {
        this.type = type;
    }

    public int value() {
        return type;
    }

    @Override
    public String toString() {
        return String.valueOf(type);
    }
}
