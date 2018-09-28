package com.hago.startup;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class Constant {

    //app启动时间，调用之前保证app关闭
    public final static String CMD_START_APP = "am start --user 0 -W --ei cmdMonitor 1 com.yy.hiyo/.LaunchActivity";

    //关闭app
    //public final static String CMD_CLOSE_APP = "am force-stop com.yy.hiyo";  //目前对象app自己关闭

    public static final String GET_BUILD_URL = "https://ci.yy.com/jenkins2/view/android-app/job/hiyo-android/";

    public static final String GET_ALL_BRANCH = "https://ci.yy.com/jenkins2/view/android-app/job/hiyo-android/descriptorByName/net.uaznia.lukanus.hudson.plugins.gitparameter.GitParameterDefinition/fillValueItems?param=DEV_LINE";

    //用于匹配最新构建的包地址
    public static final String MATCHER_LAST_BUILD_TAG = "http://repo.yypm.com/dwbuild/mobile/android/hiyo/";

    public static final String DOWNLOAD_SUFFIX = "hiyo.apk";

    public static final String HIYO_PACKAGENAME = "com.yy.hiyo";

    public static final String MONITOR_PACKAGENAME = "com.hago.startup";

    public static final String EMPTYSTR = "";

    //每10分钟执行一次
    public static final long START_MONITOR_INTERVAL = 5 * 60 * 1000;

    //启动统计次数，第一次启动不算
    public static final int START_COUNT = 3;

    //-----------SharedPreferences key
    public static final String MAIL_TIMESTAMP = "sendMailTimestamp"; //上次发送邮件的时间戳
    public static final String MAIL_DATE = "sendMailDate"; //上次发送邮件的日期
    public static final String MONITOR_VERSION = "monitorVersion"; //上次自动化的版本号
    //-------------------------

    //-----------邮箱相关
    public final static String USER = "964123660@qq.com";
    public final static String PWD = "kihjwbrkdjjjbdhg";
    public final static String[] TO_ADDRESS = {"huangzhilong@yy.com"/*, "wuzhonglian@yy.com"*/};
    public static final int SEND_MAIL_TIME = 5; //每天几点发送邮件
    //------------

    //----------Monitor状态相关
    public final static int IDLE        = 1; //空闲
    public final static int RUN_AUTO    = 2; //在执行自动化测试
    public final static int WAIT_AUTO   = 3; //等待执行自动化测试，即runnable等待中
    public final static int RUN_TARGET  = 4; //在执行指定版本的测试
    //-------------
}
