package com.coderstory.toolkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import eu.chainfire.libsuperuser.Shell;

public class BootBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)) {
//            Log.v("smile", "BootBroadcastReceiver");
//            Intent welcomeIntent = new Intent(context, StartDetectService.class);
//            context.startService(welcomeIntent);
            if(XPreferenceUtils.getConfigShare(context).getBoolean(XPreferenceUtils.BOOT_COMPLETED,true))
            {
                ///关闭 selinux
                Shell.SU.run("setenforce 0");
                Shell.SU.run("sh " + XPreferenceUtils.BOOT_SHELL_PATH);
            }

        }
    }

}
