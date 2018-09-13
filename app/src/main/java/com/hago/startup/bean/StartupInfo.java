package com.hago.startup.bean;

/**
 * Created by huangzhilong on 18/9/11.
 */

public class StartupInfo {

    //adb启动时间
    public StartupTime mStartupTime;

    //hiyo返回的时间和内存啊
    public StartupData mStartupData;

    @Override
    public String toString() {
        return "StartupInfo{" +
                "mStartupTime=" + mStartupTime +
                ", mStartupData=" + mStartupData +
                '}';
    }
}
