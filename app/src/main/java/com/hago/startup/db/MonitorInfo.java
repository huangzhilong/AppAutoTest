package com.hago.startup.db;

import com.hago.startup.bean.ResultInfo;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by huangzhilong on 18/9/13.
 */

@DatabaseTable(tableName = "tb_monitor")
public class MonitorInfo {

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "branch")
    public String branch; //分支

    @DatabaseField(columnName = "version")
    public String version; //版本号

    @DatabaseField(columnName = "size")
    public long size; //包大小

    @DatabaseField(columnName = "thisTime")
    public long thisTime; //adb thisTime

    @DatabaseField(columnName = "totalTime")
    public long totalTime; //adb totalTime

    @DatabaseField(columnName = "waitTime")
    public long waitTime; //adb waitTime

    @DatabaseField(columnName = "startTime")
    public long startTime; //app自己计算的时间

    @DatabaseField(columnName = "startupMemory")
    public int startupMemory; //app启动10s后取次内存

    @DatabaseField(columnName = "timestamp")
    public long timestamp; //当前时间

    @DatabaseField(columnName = "reserve1")
    public String reserve1; //保留字段未使用

    @DatabaseField(columnName = "reserve2")
    public String reserve2; //保留字段未使用

    @DatabaseField(columnName = "reserve3")
    public String reserve3; //保留字段未使用

    @DatabaseField(columnName = "reserve4")
    public String reserve4; //保留字段未使用

    @DatabaseField(columnName = "reserve5")
    public String reserve5; //保留字段未使用

    public MonitorInfo(ResultInfo resultInfo) {
        branch = resultInfo.mApkInfo.branch;
        version = resultInfo.mApkInfo.version;
        size = resultInfo.mApkInfo.size;
        thisTime = resultInfo.mStartupInfo.mStartCmdInfo.thisTime;
        totalTime = resultInfo.mStartupInfo.mStartCmdInfo.totalTime;
        waitTime = resultInfo.mStartupInfo.mStartCmdInfo.waitTime;
        startTime = resultInfo.mStartupInfo.mStartAppInfo.startTime;
        startupMemory = resultInfo.mStartupInfo.mStartAppInfo.startupMemory;
        timestamp = System.currentTimeMillis();
    }

    public MonitorInfo() {
        timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "MonitorInfo{" +
                "id=" + id +
                ", branch='" + branch + '\'' +
                ", version='" + version + '\'' +
                ", size=" + size +
                ", thisTime=" + thisTime +
                ", totalTime=" + totalTime +
                ", waitTime=" + waitTime +
                ", startTime=" + startTime +
                ", startupMemory=" + startupMemory +
                ", timestamp=" + timestamp +
                ", reserve1='" + reserve1 + '\'' +
                ", reserve2='" + reserve2 + '\'' +
                ", reserve3='" + reserve3 + '\'' +
                ", reserve4='" + reserve4 + '\'' +
                ", reserve5='" + reserve5 + '\'' +
                '}';
    }
}
