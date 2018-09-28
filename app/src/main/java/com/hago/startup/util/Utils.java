package com.hago.startup.util;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.accessibility.AccessibilityManager;

import com.hago.startup.Constant;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import io.reactivex.MaybeEmitter;

/**
 * Created by huangzhilong on 18/9/6.
 */

public class Utils {

    /**
     * 用于控制中止测试的链式,中止后在测试时记得设为false
     */
    public static volatile boolean mCancelEmitter = false;

    public static long safeParseLong(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        long value = 0;
        try {
            value = Long.valueOf(str);
        } catch (Throwable e) {
            LogUtil.logE("Utils", "safeParseLong " + str);
        }
        return value;
    }

    public static boolean empty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static int getCollectionSize(Collection collection) {
        return collection == null ? 0 : collection.size();
    }

    public static String getCurDay() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; //0开始算
        int day = calendar.get(Calendar.DATE);
        String today = year + "-" + month + "-" + day;
        return today;
    }
    /**
     * 检测并提示开启文件读写权限
     * @param context
     */
    public static void checkFilePermission(Activity context) {
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            String[] filePermission = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(context, filePermission, 1);
        }
    }

    /**
     * 判断是否开启了辅助功能
     * @param context
     * @return
     */
    public static boolean isStartAccessibilityService(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfoList = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        if (!Utils.empty(serviceInfoList)) {
            for (int i = 0; i < serviceInfoList.size(); i++) {
                AccessibilityServiceInfo info = serviceInfoList.get(i);
                if (info != null && info.getId() != null && info.getId().contains(Constant.MONITOR_PACKAGENAME)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static String getExternalDir() throws IOException {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + Constant.MONITOR_PACKAGENAME);
        if (!file.mkdirs()) {
            file.createNewFile();
        }
        String savePath = file.getAbsolutePath();
        return savePath;
    }

}
