package com.coderstory.toolkit;


import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;

public class XPreferenceUtils
{

    public static final String AUTO_ALL ="auto_all";
    public static final String DEGRADING_CHECK ="degrading_check";
    public static final String SIGN_CHECK ="sign_check";
    public static final String COVER_CHECK ="cover_check";
    public static final String SSL_CHECK ="ssl_check";
    public static final String ADB_ALLOW ="adb_allow";
    public static final String HIDE_ICON ="hide_icon";
    public static final String DEFAULT ="DEFAULT";
    public static final String PLATFORM ="platform";
    public static final String PKG_NAME = "com.coderstory.toolkit";

    private static XSharedPreferences intance = null;

    public static XSharedPreferences getIntance(){
        if (intance == null){
            intance = new XSharedPreferences(PKG_NAME,"conf.xml");
            intance.makeWorldReadable();
        }else {
            intance.reload();
        }
        return intance;
    }

    public static boolean openAll(){
        return getIntance().getBoolean(AUTO_ALL,false);
    }
    public static boolean isDowngradeCheck(){
        return getIntance().getBoolean(DEGRADING_CHECK, true);
    }
    public static boolean isSignCheck(){
        return getIntance().getBoolean(SIGN_CHECK, true);
    }
    public static boolean isCoverCheck(){
        return getIntance().getBoolean(COVER_CHECK, false);
    }
    public static boolean isSslCheck(){
        XposedBridge.log(" isSslCheck:" + getIntance().getBoolean(ADB_ALLOW, true));
        return getIntance().getBoolean(SSL_CHECK, true);
    }
    public static boolean isAdbAllow(){
        XposedBridge.log(" isAdbAllow:" + getIntance().getBoolean(ADB_ALLOW, true));
        return getIntance().getBoolean(ADB_ALLOW, true);
    }
    public static String getPlatform(){
        return getIntance().getString(PLATFORM,DEFAULT);
    }
    public static boolean isHideIcon(){
        return getIntance().getBoolean(HIDE_ICON, false);
    }
}