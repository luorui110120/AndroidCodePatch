package com.coderstory.toolkit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.content.Context.USB_SERVICE;

public class AdbUsbAllow implements IXposedHookLoadPackage
{
    public static String mKey;
    public static String SYSTEMUI="com.android.systemui";

    public static XC_MethodHook usbDisconnectedRecivce= new XC_MethodHook()
    {
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable
        {
            super.beforeHookedMethod(param);
            //allowUsbDebugging keyong
            try {
                Intent intent01 = (Intent) param.args[1];
                intent01.putExtra("connected", false);
                Class<?> cls_sm = Class.forName("android.os.ServiceManager");
                Method getService = cls_sm.getMethod("getService", String.class);
                getService.setAccessible(true);
                IBinder iBinder = (IBinder)getService.invoke(null, USB_SERVICE);
                Class<?> cls_isub = Class.forName("android.hardware.usb.IUsbManager$Stub");
                Method asInterface = cls_isub.getDeclaredMethod("asInterface", IBinder.class);
                Object Ob_IUsbManager = asInterface.invoke(null, iBinder);
                Method Me_allowUsbDebugging = Ob_IUsbManager.getClass().getDeclaredMethod("allowUsbDebugging",boolean.class, String.class);
                ///  allowUsbDebugging 函数的第一个参数如果为 true 这个设备永久保存key
                Me_allowUsbDebugging.invoke(Ob_IUsbManager, true, mKey);

//                        IBinder b = ServiceManager.getService(USB_SERVICE);
//                        boolean connected  = false;
//                        IUsbManager service = IUsbManager.Stub.asInterface(b);
//                        service.allowUsbDebugging(true, mKey);
            } catch (Exception e) {
                XposedBridge.log("Exception:" + e);
            }

            //<<end

        }
        protected void afterHookedMethod(MethodHookParam param) throws Throwable
        {
            //   XposedBridge.log("usb return，你已被劫持:" + param.args[0]);
        }
    };
    public static XC_MethodHook usbDebuggingActivityOnCreate = new XC_MethodHook() {
        protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
        {
            super.beforeHookedMethod(param);
        }

        protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
        {
            Field privateStringField = null;
            try
            {
                privateStringField = param.thisObject.getClass().getDeclaredField("mKey");
                privateStringField.setAccessible(true); //设置权限
                mKey = (String) privateStringField.get(param.thisObject);
                XposedBridge.log("AdbAllowConnect: " + mKey);
            } catch (NoSuchFieldException e)
            {
                e.printStackTrace();
            }
        }
    };
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable
    {
 //       XposedBridge.log(" packageName:" + loadPackageParam.packageName);
        if (loadPackageParam.packageName.equals(SYSTEMUI))
        {
            XposedHelpers.findAndHookMethod("com.android.systemui.usb.UsbDebuggingActivity$UsbDisconnectedReceiver", loadPackageParam.classLoader, "onReceive", Context.class, Intent.class, usbDisconnectedRecivce);

            XposedHelpers.findAndHookMethod("com.android.systemui.usb.UsbDebuggingActivity", loadPackageParam.classLoader, "onCreate", Bundle.class, usbDebuggingActivityOnCreate);
        }
    }
}
