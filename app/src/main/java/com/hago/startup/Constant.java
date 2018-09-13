package com.hago.startup;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class Constant {

    //app启动时间，调用之前保证app关闭
    public final static String CMD_START_APP = "am start --user 0 -W --ei cmdMonitor 1 com.yy.hiyo/.LaunchActivity";

    //关闭app
    //public final static String CMD_CLOSE_APP = "am start force-stop com.yy.hiyo";  //这个关闭不了,目前对象app自己关闭



    public static final String GET_BUILD_URL = "https://ci.yy.com/jenkins2/view/android-app/job/hiyo-android/";

    //用于匹配最新构建的包地址
    public static final String MATCHER_LAST_BUILD_TAG = "http://repo.yypm.com/dwbuild/mobile/android/hiyo/";

    public static final String DOWNLOAD_SUFFIX = "hiyo.apk";

    public static final String HIYO_PACKAGENAME = "com.yy.hiyo";

    public static final String MONITOR_PACKAGENAME = "com.hago.startup";

    //每5分钟执行一次
    public static final long START_MONITOR_INTERVAL = 5 * 60 * 1000;

    //启动统计次数，第一次启动不算
    public static final int START_COUNT = 3;
}
