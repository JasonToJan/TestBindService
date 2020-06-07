package com.meizu.statsapp.v3;

/**
 * Created by jinhui on 17-3-6.
 */

/**
 * 0:默认值，表示APP ；1:代理游戏； 2：flymeTV； 3:PAD
 */
public enum PkgType {
    APP(0), //app
    GAME(1), //代理游戏
    FLYME_TV(2), //flyme TV
    PAD(3); //pad

    private int type;

    PkgType(int type) {
        this.type = type;
    }

    public static PkgType fromValue(int value) {
        if (value == 0) {
            return APP;
        } else if (value == 1) {
            return GAME;
        } else if (value == 2) {
            return FLYME_TV;
        } else if (value == 3) {
            return PAD;
        }
        return null;
    }

    public int value() {
        return type;
    }
}