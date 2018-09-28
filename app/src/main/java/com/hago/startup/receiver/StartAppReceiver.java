package com.hago.startup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hago.startup.util.LogUtil;
import com.hago.startup.notify.NotificationCenter;
import com.hago.startup.bean.StartAppInfo;

/**
 * Created by huangzhilong on 18/9/7.
 * 用于接收hago代码检测的启动时间
 */

public class StartAppReceiver extends BroadcastReceiver {

    private static final String TAG = "StartAppReceiver";
    public final static String ACTION = "com.yy.hiyo.startup.broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            long startTime = intent.getLongExtra("startupTime", 0);
            int memory = intent.getIntExtra("startupMemory", 0);
            boolean isDebug = intent.getBooleanExtra("isDebug", true);
            StartAppInfo startAppInfo = new StartAppInfo();
            startAppInfo.startTime = startTime;
            startAppInfo.startupMemory = memory;
            startAppInfo.isDebug = isDebug;
            LogUtil.logI(TAG, "startAppInfo: %s", startAppInfo);
            NotificationCenter.INSTANCE.emitterStartInfo(startAppInfo);
        }
    }
}
