package com.hago.startup.util;

import android.text.TextUtils;

import com.hago.startup.bean.ApkInfo;

/**
 * Created by huangzhilong on 18/9/11.
 */

public class ApkInfoUtil {

    private static final String TAG = "ApkInfoUtil";
    /**
     * 在下载地址提取分支和版本号
     *
     * @param url
     * @return
     */
    public static void getApkInfo(String url, ApkInfo apkInfo) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        String[] list = url.split("/");
        if (list == null || list.length == 0) {
            return;
        }
        apkInfo.filePath = url;
        for (int i = 0; i < list.length; i++) {
            String str = list[i];
            if (TextUtils.isEmpty(str)) {
                continue;
            }
            //分支
            if (str.contains("hiyo-android")) {
                apkInfo.branch = str;
                continue;
            }
            //版本号
            String[] version = str.split("-");
            if (version != null && version.length > 2) {
                apkInfo.version = version[1];
            }
        }
        LogUtil.logI(TAG, "getApkInfo :" + apkInfo);
    }

}
