package com.coderstory.toolkit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HideHttpProxy
{
    public  final  static String httpKey="http";
    public  final  static String proxyKey="proxy";
    public  final  static String hostKey="host";
    private  static int matchKey(String key){
        int nCount =0;
        if(key.toLowerCase().indexOf(httpKey) >= 0){
            nCount++;
        }
        if(key.toLowerCase().indexOf(proxyKey) >= 0){
            nCount++;
        }
        if(key.toLowerCase().indexOf(hostKey) >= 0){
            nCount++;
        }
        return nCount;
    }
    public static XC_MethodHook httpChkProperty= new XC_MethodHook()
    {
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
            super.beforeHookedMethod(param);
            String key = (String)param.args[0];
            if(!key.isEmpty()){
//                XposedBridge.log("System.getProperty key:" + key + " match :" + matchKey(key));
                if(matchKey(key) > 1){
                    param.setResult(null);
                }
            }
        }
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {

        }
    };

}
