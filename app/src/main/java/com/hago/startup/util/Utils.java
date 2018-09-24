package com.hago.startup.util;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.hago.startup.Constant;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import io.reactivex.MaybeEmitter;

/**
 * Created by huangzhilong on 18/9/6.
 */

public class Utils {

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

    public static <T> void safeEmitterSuccess(MaybeEmitter<T> emitter, T data) {
        if (emitter == null || emitter.isDisposed()) {
            LogUtil.logI("Utils", "safeEmitterData emitter not available");
            return;
        }
        if (data == null) {
            LogUtil.logI("Utils", "safeEmitterData data is null");
            return;
        }
        emitter.onSuccess(data);
    }

    public static void safeEmitterError(MaybeEmitter emitter, Throwable throwable) {
        if (emitter == null || emitter.isDisposed()) {
            LogUtil.logI("Utils", "safeEmitterError emitter not available");
            return;
        }
        if (throwable == null) {
            LogUtil.logI("Utils", "safeEmitterError throwable is null");
            return;
        }
        emitter.onError(throwable);
    }

    public static int convertDpToPixel(int dp, Context context) {
        try {
            if (context == null) {
                return dp;
            }
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float px = dp * (metrics.densityDpi / 160f);
            return (int)px;
        } catch (Exception ex) {
            Log.e("ResolutionUtils", "Empty Catch on convertDpToPixel", ex);
        }

        return dp;
    }
}
