package com.hago.startup.bean;

/**
 * Created by huangzhilong on 18/9/6.
 */

public class StartupTime {

    public long thisTime;

    public long totalTime;

    public long waitTime;

    public StartupTime() {
        thisTime = 0;
        totalTime = 0;
        waitTime = 0;
    }

    @Override
    public String toString() {
        return "StartupTime{" +
                "thisTime=" + thisTime +
                ", totalTime=" + totalTime +
                ", waitTime=" + waitTime +
                '}';
    }
}
