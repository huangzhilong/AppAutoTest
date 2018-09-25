package com.hago.startup.db.bean;

import java.util.List;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class ResultInfo {

    public static final ResultInfo RESULT_EMPTY = new ResultInfo(null, null);

    public VersionInfo mVersionInfo;

    public List<MonitorInfo> mMonitorInfoList;

    public ResultInfo(VersionInfo versionInfo, List<MonitorInfo> monitorInfos) {
        mVersionInfo = versionInfo;
        mMonitorInfoList = monitorInfos;
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "mVersionInfo=" + mVersionInfo +
                ", mMonitorInfoList=" + mMonitorInfoList +
                '}';
    }
}
