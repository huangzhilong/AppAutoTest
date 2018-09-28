package com.hago.startup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.hago.startup.Constant;
import com.hago.startup.util.LogUtil;
import com.hago.startup.notify.NotificationCenter;

/**
 * Created by huangzhilong on 18/9/11.
 */

public class AppInstallReceiver extends BroadcastReceiver {

    private static final String TAG = "AppInstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }
        Uri uri = intent.getData();
        String packageName = uri.getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            if (Constant.HIYO_PACKAGENAME.equals(packageName)) {
                LogUtil.logI(TAG, "hago app install");
                NotificationCenter.INSTANCE.emitterInstall();
            }
            return;
        }
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            if (Constant.HIYO_PACKAGENAME.equals(packageName)) {
                LogUtil.logI(TAG, "hago app unInstall");
                NotificationCenter.INSTANCE.emitterUnInstall();
            }
        }
    }
}
