package com.hago.startup.bean;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class ResultInfo {

    public ApkInfo mApkInfo;

    public StartupInfo mStartupInfo;

    public ResultInfo(ApkInfo apkInfo, StartupInfo startupInfo) {
        mApkInfo = apkInfo;
        mStartupInfo = startupInfo;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "mApkInfo=" + mApkInfo +
                ", mStartupInfo=" + mStartupInfo +
                '}';
    }
}
