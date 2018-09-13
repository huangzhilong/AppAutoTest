package com.hago.startup.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hago.startup.util.LogUtil;
import com.hago.startup.NotificationCenter;
import com.hago.startup.bean.StartupData;

/**
 * Created by huangzhilong on 18/9/7.
 * 用于接收hago代码检测的启动时间
 */

public class StartupTimeReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupTimeReceiver";
    public final static String ACTION = "com.yy.hiyo.startup.broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION.equals(intent.getAction())) {
            long startTime = intent.getLongExtra("startupTime", 0);
            int memory = intent.getIntExtra("startupMemory", 0);
            StartupData startupData = new StartupData();
            startupData.startTime = startTime;
            startupData.startupMemory = memory;
            LogUtil.logI(TAG, "startupData: %s", startupData);
            NotificationCenter.INSTANCE.emitterStartData(startupData);
        }
    }
}
