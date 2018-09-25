package com.hago.startup.db.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by huangzhilong on 18/9/25.
 */


@DatabaseTable(tableName = "tb_monitor")
public class MonitorInfo {

    @DatabaseField(generatedId = true)
    public int id;

    //对于version的版本号
    @DatabaseField(columnName = "version")
    public long version;

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


    @Override
    public String toString() {
        return "MonitorInfo{" +
                "id=" + id +
                ", thisTime=" + thisTime +
                ", totalTime=" + totalTime +
                ", waitTime=" + waitTime +
                ", startTime=" + startTime +
                ", startupMemory=" + startupMemory +
                ", timestamp=" + timestamp +
                ", reserve1='" + reserve1 + '\'' +
                ", reserve2='" + reserve2 + '\'' +
                ", reserve3='" + reserve3 + '\'' +
                ", version=" + version +
                '}';
    }
}
