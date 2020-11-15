package com.coderstory.toolkit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/////// 先 添加一个开机启动的服务 未来用
public class StartDetectService extends Service
{

    private boolean canRun = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
     //   Toast.makeText(this, "WTF", Toast.LENGTH_SHORT).show();

    }
}
