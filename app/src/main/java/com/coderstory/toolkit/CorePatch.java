package com.coderstory.toolkit;


import android.content.Context;
import android.content.Intent;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;

import com.coderstory.sslkiller.SSLKiller;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;

import java.lang.reflect.Field;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class CorePatch extends XposedHelper implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    public void initZygote(StartupParam paramStartupParam) {
        // 7.0以上才支持, 6.0的系统可以使用幸运破解器
        if(android.os.Build.VERSION.SDK_INT > 23)
        {
            XposedHelpers.findAndHookMethod("java.security.MessageDigest", null, "isEqual", byte[].class, byte[].class, new XC_MethodHook()
            {
                protected void beforeHookedMethod(MethodHookParam methodHookParam)
                        throws Throwable
                {
                    if (XPreferenceUtils.isSignCheck())
                    {
                        methodHookParam.setResult(true);
                    }
                }
            });

            XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.org.conscrypt.OpenSSLSignature", null), "engineVerify", new XC_MethodHook()
            {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam)
                        throws Throwable
                {
                    if (XPreferenceUtils.isSignCheck())
                    {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });

            final Class ApkSignatureSchemeV2Verifier = XposedHelpers.findClass("android.util.apk.ApkSignatureSchemeV2Verifier", null);
            final Class packageParser = XposedHelpers.findClass("android.content.pm.PackageParser", null);
            final Class strictJarVerifier = XposedHelpers.findClass("android.util.jar.StrictJarVerifier", null);
            final Class packageClass = XposedHelpers.findClass("android.content.pm.PackageParser.Package", null);


            XposedBridge.hookAllMethods(packageParser, "getApkSigningVersion", XC_MethodReplacement.returnConstant(1));

            XposedBridge.hookAllConstructors(strictJarVerifier, new XC_MethodHook()
            {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable
                {

                    if (XPreferenceUtils.isSignCheck())
                    {
                        param.args[3] = false;
                    }
                }
            });

            XposedBridge.hookAllConstructors(ApkSignatureSchemeV2Verifier, new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable
                {
                    Object packageInfoLite = param.thisObject;
                    if (XPreferenceUtils.isSignCheck())
                    {
                        Field field = packageClass.getField(" SF_ATTRIBUTE_ANDROID_APK_SIGNED_ID");
                        field.setAccessible(true);
                        field.set(packageInfoLite, -1);
                    }
                }
            });
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam paramLoadPackageParam) {

        if (("android".equals(paramLoadPackageParam.packageName)) && (paramLoadPackageParam.processName.equals("android"))) {

            final Class localClass = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", paramLoadPackageParam.classLoader);
            final Class packageClass = XposedHelpers.findClass("android.content.pm.PackageParser.Package", paramLoadPackageParam.classLoader);

            XposedBridge.hookAllMethods(localClass, "checkDowngrade", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    super.beforeHookedMethod(methodHookParam);
                    Object packageInfoLite = methodHookParam.args[0];
                    if (XPreferenceUtils.isDowngradeCheck()){
                        Field fieldVersion = packageClass.getField("mVersionCode");
                        fieldVersion.setAccessible(true);
                        fieldVersion.set(packageInfoLite, 0);
                        ////////////// smile add 适配 android 9.0
                        Field fieldVersionMajor = packageClass.getField("mVersionCodeMajor");
                        fieldVersionMajor.setAccessible(true);
                        fieldVersionMajor.set(packageInfoLite, 0);
                    }
                }
            });

            XposedBridge.hookAllMethods(localClass, "verifySignaturesLP", new XC_MethodHook() {

                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (XPreferenceUtils.isSignCheck()) {
                        methodHookParam.setResult(true);
                    }
                }
            });

            XposedBridge.hookAllMethods(localClass, "compareSignatures", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (XPreferenceUtils.isCoverCheck()) {

                        String platform = XPreferenceUtils.getPlatform();

                        if (platform.equals("DEFAULT")){
                            XposedBridge.log("警告:核心破解上未初始化,请至少打开一次APP!");
                        }

                        Signature[] signatures = (Signature[]) methodHookParam.args[0];
                        if (signatures != null && signatures.length > 0) {
                            for (Signature signature : signatures) {
                                if (new String(Base64.encode(signature.toByteArray(), Base64.DEFAULT)).replaceAll("\n", "").equals(platform)) {
                                    return;
                                }
                            }
                        }

                        signatures = (Signature[]) methodHookParam.args[1];
                        if (signatures != null && signatures.length > 0) {
                            for (Signature signature : signatures) {
                                if (new String(Base64.encode(signature.toByteArray(), Base64.DEFAULT)).replaceAll("\n", "").equals(platform)) {
                                    return;
                                }
                            }
                        }

                        methodHookParam.setResult(0);
                    }
                }
            });

            XposedBridge.hookAllMethods(localClass, "compareSignaturesCompat", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam) {
                    if (XPreferenceUtils.isSignCheck()){
                        paramAnonymousMethodHookParam.setResult(0);
                    }
                }
            });
            XposedBridge.hookAllMethods(localClass, "compareSignaturesRecover", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam) {
                    if (XPreferenceUtils.isSignCheck()) {
                        paramAnonymousMethodHookParam.setResult(0);
                    }
                }
            });


            ///////// android 8.0 ~8.1 添加的
            XposedBridge.hookAllMethods(localClass, "shouldCheckUpgradeKeySetLP", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam) {
                    if (XPreferenceUtils.isSignCheck()) {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });
            XposedBridge.hookAllMethods(localClass, "checkUpgradeKeySetLP", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam) {
                    if (XPreferenceUtils.isSignCheck()) {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });
            ///////// android 9.0 添加的
            final Class keySetMageClass = XposedHelpers.findClass("com.android.server.pm.KeySetManagerService", paramLoadPackageParam.classLoader);
            XposedBridge.hookAllMethods(keySetMageClass, "checkUpgradeKeySetLocked", new XC_MethodHook() {

                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam) {
                    if (XPreferenceUtils.isSignCheck()) {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });
            XposedBridge.hookAllMethods(keySetMageClass, "shouldCheckUpgradeKeySetLocked", new XC_MethodHook() {
                protected void beforeHookedMethod(MethodHookParam paramAnonymousMethodHookParam) {
                    if (XPreferenceUtils.isSignCheck()) {
                        paramAnonymousMethodHookParam.setResult(true);
                    }
                }
            });

        }
        if(XPreferenceUtils.isSslCheck()) {
            XposedHelpers.findAndHookMethod(SSLSocketFactory.class, "getSocketFactory", SSLKiller.getSocketFactoryHook);
            XposedHelpers.findAndHookMethod(SSLContext.class, "init", "javax.net.ssl.KeyManager[]", "javax.net.ssl.TrustManager[]", SecureRandom.class, SSLKiller.sslContextInitHook);
            XposedHelpers.findAndHookMethod(SSLSocketFactory.class, "setHostnameVerifier", X509HostnameVerifier.class, SSLKiller.hostNameVerifierHook);
            XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setHostnameVerifier", HostnameVerifier.class, SSLKiller.hostNameVerifierHook);
            XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setDefaultHostnameVerifier", HostnameVerifier.class, SSLKiller.hostNameVerifierHook);
            XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setSSLSocketFactory", javax.net.ssl.SSLSocketFactory.class, SSLKiller.setSSLSocketFactoryHook);
            XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "setDefaultSSLSocketFactory", javax.net.ssl.SSLSocketFactory.class, SSLKiller.setSSLSocketFactoryHook);
            XposedHelpers.findAndHookMethod(HttpsURLConnection.class, "getDefaultSSLSocketFactory", SSLKiller.urlConnectionHook);
        }
        if (paramLoadPackageParam.packageName.equals(AdbUsbAllow.SYSTEMUI)
            && XPreferenceUtils.isAdbAllow()){
        //    XposedBridge.log("UsbDebuggingActivity hook!");
            XposedHelpers.findAndHookMethod("com.android.systemui.usb.UsbDebuggingActivity$UsbDisconnectedReceiver", paramLoadPackageParam.classLoader, "onReceive", Context.class, Intent.class, AdbUsbAllow.usbDisconnectedRecivce);
            XposedHelpers.findAndHookMethod("com.android.systemui.usb.UsbDebuggingActivity", paramLoadPackageParam.classLoader, "onCreate", Bundle.class, AdbUsbAllow.usbDebuggingActivityOnCreate);
        }
        if (XPreferenceUtils.isHideHttpProxy()){
            XposedHelpers.findAndHookMethod("java.lang.System", paramLoadPackageParam.classLoader, "getProperty", String.class, HideHttpProxy.httpChkProperty);
        }
    }
}
