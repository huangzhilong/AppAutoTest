package com.hago.startup.bean;

import android.text.TextUtils;

import com.hago.startup.Constant;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class ApkInfo {

    public String branch; //分支

    public String version; //版本号

    public long size; //包大小

    public String filePath; //下载地址或本地apk地址

    public ApkInfo() {
        branch = "";
        version = "";
        size = 0;
    }

    @Override
    public String toString() {
        return "ApkInfo{" +
                "branch='" + branch + '\'' +
                ", version='" + version + '\'' +
                ", size=" + size +
                ", filePath='" + filePath + '\'' +
                '}';
    }

    public void reset() {
        branch = "";
        version = "";
        size = 0;
        filePath = "";
    }

    public String getApkName() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!TextUtils.isEmpty(branch)) {
            stringBuilder.append(branch);
            stringBuilder.append("-");
        }
        if (!TextUtils.isEmpty(version)) {
            stringBuilder.append(version);
            stringBuilder.append("-");
        }
        stringBuilder.append(Constant.DOWNLOAD_SUFFIX);
        return stringBuilder.toString();
    }
}
