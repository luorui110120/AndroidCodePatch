package com.coderstory.toolkit;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Switch;

import java.io.File;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {
    private static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;
    //String ApplicationName = "com.coderstory.toolkit";
    public final String PREFS_FOLDER = " /data/data/" + XPreferenceUtils.getPkgName() + "/shared_prefs\n";
    public final String PREFS_FILE = " /data/data/" + XPreferenceUtils.getPkgName() + "/shared_prefs/" + XPreferenceUtils.SHARED_FILE_NAME + ".xml\n";
    private ProgressDialog dialog;
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.arg1 == 0) {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                normalDialog.setTitle("提示");
                normalDialog.setMessage("请先授权应用ROOT权限");
                normalDialog.setPositiveButton("确定",
                        (dialog, which) -> System.exit(0));
                // 显示
                normalDialog.show();

            } else if ((msg.arg1 == 1)) {
                dialog = ProgressDialog.show(MainActivity.this, "检测ROOT权限", "请在ROOT授权弹窗中给与ROOT权限,\n如果长时间无反应则请检查ROOT程序是否被\"省电程序\"干掉");
                dialog.show();
            } else if (msg.arg1 == 3) {
                final AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                normalDialog.setTitle("提示");
                normalDialog.setMessage("插件尚未启用，请开启后再次打开.实在搞不定，就删了xposed installer 重新安装一次");
                normalDialog.setPositiveButton("确定",
                        (dialog, which) -> System.exit(0));
                // 显示
                normalDialog.show();
            } else {
                if (dialog != null && dialog.isShowing()) {
                    dialog.cancel();
                    getEditor().putBoolean("isRooted", true).apply();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 获取  adb 调试状态
        boolean enableAdb = (Settings.Secure.getInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
        // 读取平台签名并保存
        new Thread(() -> {
            try {
                PackageInfo packageInfo = MainActivity.this.getPackageManager().getPackageInfo("android", PackageManager.GET_SIGNATURES);
                if (packageInfo.signatures[0] != null) {
                    String platform = new String(Base64.encode(packageInfo.signatures[0].toByteArray(), Base64.DEFAULT)).replaceAll("\n", "");
                    getEditor().putString(XPreferenceUtils.PLATFORM, platform);
                    getEditor().apply();
                    sudoFixPermissions();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }).start();

        $(R.id.id_downgrade).setOnClickListener(v -> {
            getEditor().putBoolean(XPreferenceUtils.DEGRADING_CHECK, ((Switch) v).isChecked());
            getEditor().apply();
            sudoFixPermissions();
        });

        $(R.id.id_sign).setOnClickListener(v -> {
            getEditor().putBoolean(XPreferenceUtils.SIGN_CHECK, ((Switch) v).isChecked());
            getEditor().apply();
            sudoFixPermissions();
        });

        $(R.id.id_cover).setOnClickListener(v -> {
            getEditor().putBoolean(XPreferenceUtils.COVER_CHECK, ((Switch) v).isChecked());
            getEditor().apply();
            sudoFixPermissions();
        });

        $(R.id.id_ssl).setOnClickListener(v -> {
            getEditor().putBoolean(XPreferenceUtils.SSL_CHECK, ((Switch) v).isChecked());
            getEditor().apply();
            sudoFixPermissions();
        });
        $(R.id.id_adbAllow).setOnClickListener(v -> {
            getEditor().putBoolean(XPreferenceUtils.ADB_ALLOW, ((Switch) v).isChecked());
            getEditor().apply();
            sudoFixPermissions();
        });
        $(R.id.id_adbSiwtch).setOnClickListener(v -> {
            if(((Switch) v).isChecked()){
                Shell.SU.run("settings put global adb_enabled 1");
            }
            else{
                Shell.SU.run("settings put global adb_enabled 0");
            }
        });

        ((Switch) $(R.id.id_downgrade)).setChecked(getPrefs().getBoolean(XPreferenceUtils.DEGRADING_CHECK, true));
        ((Switch) $(R.id.id_sign)).setChecked(getPrefs().getBoolean(XPreferenceUtils.SIGN_CHECK, true));
        ((Switch) $(R.id.id_cover)).setChecked(getPrefs().getBoolean(XPreferenceUtils.COVER_CHECK, true));
        ((Switch) $(R.id.id_ssl)).setChecked(getPrefs().getBoolean(XPreferenceUtils.SSL_CHECK, true));
        ((Switch) $(R.id.id_adbAllow)).setChecked(getPrefs().getBoolean(XPreferenceUtils.ADB_ALLOW, true));
        ((Switch) $(R.id.id_adbSiwtch)).setChecked(enableAdb);
        ((Switch) $(R.id.id_hide)).setChecked(getPrefs().getBoolean(XPreferenceUtils.HIDE_ICON, false));

        $(R.id.id_hide).setOnClickListener(v -> {
            getEditor().putBoolean(XPreferenceUtils.HIDE_ICON, ((Switch) v).isChecked());
            getEditor().apply();
            sudoFixPermissions();
            ComponentName localComponentName = new ComponentName(MainActivity.this, MainActivity.this.getClass());
            PackageManager localPackageManager = getPackageManager();
            localPackageManager.getComponentEnabledSetting(localComponentName);
            PackageManager packageManager = getPackageManager();
            ComponentName componentName = new ComponentName(MainActivity.this, MainActivity.this.getClass());

            if (((Switch) v).isChecked()) {
                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            } else {
                packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                        PackageManager.DONT_KILL_APP);
            }
        });
        $(R.id.alipay).setOnClickListener(view ->
                donateAlipay()
        );
        if (!getPrefs().getBoolean("isRooted", false)) {
            // 检测弹窗
            new Thread(() -> {
                Message msg = new Message();
                msg.arg1 = 1;
                myHandler.sendMessage(msg);
                if (!Shell.SU.available()) {
                    msg = new Message();
                    msg.arg1 = 0;
                    myHandler.sendMessage(msg);
                } else {
                    msg = new Message();
                    msg.arg1 = 2;
                    myHandler.sendMessage(msg);
                }
            }).start();
        }

    }

    protected <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    protected SharedPreferences.Editor getEditor() {
        if (editor == null) {
            editor = getPrefs().edit();
        }
        return editor;
    }

    protected void sudoFixPermissions() {
        new Thread(() -> {
            File pkgFolder = new File("/data/data/" + XPreferenceUtils.getPkgName());
            if (pkgFolder.exists()) {
                pkgFolder.setExecutable(true, false);
                pkgFolder.setReadable(true, false);
            }
            Shell.SU.run("chmod  755 " + PREFS_FOLDER);
            // Set preferences file permissions to be world readable
            Shell.SU.run("chmod  644 " + PREFS_FILE);
        }).start();
    }

    protected SharedPreferences getPrefs() {
        prefs = getSharedPreferences("conf.xml", Context.MODE_PRIVATE);
        return prefs;
    }

    private void donateAlipay() {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(MainActivity.this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(MainActivity.this, "FKX03884EYVUJKBZLWQTFA");
        }
    }
}
