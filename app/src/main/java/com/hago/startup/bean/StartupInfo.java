package com.hago.startup.bean;

/**
 * Created by huangzhilong on 18/9/11.
 * 每次app启动的结果，包括adb 和hago app自己计算的值
 */

public class StartupInfo {

    //adb启动时间
    public StartCmdInfo mStartCmdInfo;

    //hiyo返回的时间和内存啊
    public StartAppInfo mStartAppInfo;

    @Override
    public String toString() {
        return "StartupInfo{" +
                "mStartCmdInfo=" + mStartCmdInfo +
                ", mStartAppInfo=" + mStartAppInfo +
                '}';
    }
}
