package com.hago.startup.db.bean;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class ResultInfo {

    public static final ResultInfo RESULT_EMPTY = new ResultInfo();

    public VersionInfo mVersionInfo;

    public List<MonitorInfo> mMonitorInfoList;

    public ResultInfo() {
        mVersionInfo = null;
        mMonitorInfoList = null;
    }

    public ResultInfo(ApkInfo apkInfo) {
        mVersionInfo = new VersionInfo();
        mVersionInfo.version = Utils.safeParseLong(apkInfo.version);
        mVersionInfo.branch = apkInfo.branch;
    }


    public ResultInfo(VersionInfo versionInfo, List<MonitorInfo> monitorInfos) {
        mVersionInfo = versionInfo;
        mMonitorInfoList = monitorInfos;
    }

    public ResultInfo(ApkInfo apkInfo, List<StartupInfo> startupInfoList) {
        mVersionInfo = getVersionInfoByResult(apkInfo, startupInfoList);
        mMonitorInfoList = getMonitorInfoByResult(startupInfoList);
    }

    private VersionInfo getVersionInfoByResult(ApkInfo apkInfo, List<StartupInfo> list) {
        VersionInfo info = new VersionInfo();
        info.branch = apkInfo.branch;
        info.size = apkInfo.size;
        info.version = Utils.safeParseLong(apkInfo.version);
        info.isDebug = list.get(0).mStartAppInfo.isDebug;
        return info;
    }

    private List<MonitorInfo> getMonitorInfoByResult(List<StartupInfo> startupInfoList) {
        List<MonitorInfo> results = new ArrayList<>(startupInfoList.size());
        for (int i = 0; i < startupInfoList.size(); i++) {
            StartupInfo startupInfo = startupInfoList.get(i);
            MonitorInfo monitorInfo = new MonitorInfo();
            monitorInfo.thisTime = startupInfo.mStartCmdInfo.thisTime;
            monitorInfo.totalTime = startupInfo.mStartCmdInfo.totalTime;
            monitorInfo.waitTime = startupInfo.mStartCmdInfo.waitTime;
            monitorInfo.startTime = startupInfo.mStartAppInfo.startTime;
            monitorInfo.startupMemory = startupInfo.mStartAppInfo.startupMemory;
            monitorInfo.timestamp = System.currentTimeMillis();
            monitorInfo.version = mVersionInfo.version;
            results.add(monitorInfo);
        }
        return results;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "mVersionInfo=" + mVersionInfo +
                ", mMonitorInfoList=" + mMonitorInfoList +
                '}';
    }
}
