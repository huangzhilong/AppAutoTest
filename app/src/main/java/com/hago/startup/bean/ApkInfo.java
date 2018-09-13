package com.hago.startup.bean;

import android.text.TextUtils;

import com.hago.startup.Constant;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class ApkInfo {

    public String branch; //分支ø

    public String version; //版本号

    public ApkInfo() {
        branch = "";
        version = "";
    }

    @Override
    public String toString() {
        return "ApkInfo{" +
                "branch='" + branch + '\'' +
                ", version='" + version + '\'' +
                '}';
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
