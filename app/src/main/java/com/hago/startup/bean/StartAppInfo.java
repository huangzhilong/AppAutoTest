package com.hago.startup.bean;

/**
 * Created by huangzhilong on 18/9/11.
 * hiyo app通过广播返回的值
 */

public class StartAppInfo {

    //app自己计算的时间
    public long startTime;

    //app启动10s后取次内存
    public int startupMemory;

    //app是否是debug包
    public boolean isDebug;

    @Override
    public String toString() {
        return "StartAppInfo{" +
                "startTime=" + startTime +
                ", startupMemory=" + startupMemory +
                ", isDebug=" + isDebug +
                '}';
    }
}
