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
    public static final String SHARED_FILE_NAME = "conf.xml";

    private static XSharedPreferences intance = null;

    public static XSharedPreferences getIntance(){
        if (intance == null){
            intance = new XSharedPreferences(getPkgName(),SHARED_FILE_NAME);
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
        return getIntance().getBoolean(SSL_CHECK, true);
    }
    public static boolean isAdbAllow(){
        return getIntance().getBoolean(ADB_ALLOW, true);
    }
    public static String getPlatform(){
        return getIntance().getString(PLATFORM,DEFAULT);
    }
    public static boolean isHideIcon(){
        return getIntance().getBoolean(HIDE_ICON, false);
    }
    public static String getPkgName()
    {
//        return "com.coderstory.toolkit";
        String cou = CorePatch.class.getCanonicalName();
        return cou.substring(0, cou.lastIndexOf('.'));
    }
}