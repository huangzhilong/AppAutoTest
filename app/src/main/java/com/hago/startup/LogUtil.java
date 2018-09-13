package com.hago.startup;

import android.util.Log;

import java.util.Locale;

/**
 * Created by huangzhilong on 18/9/12.
 */

public class LogUtil {

    private final static String LOG_TAG = "HagoMonitor ";

    public static void logV(String tag, String log, Object... args) {
        android.util.Log.v(getLogTag(tag), getLog(log, null, args));
    }

    public static void logI(String tag, String log, Object... args) {
        android.util.Log.i(getLogTag(tag), getLog(log, null, args));
    }

    public static void logD(String tag, String log, Object... args) {
        android.util.Log.d(getLogTag(tag), getLog(log, null, args));
    }

    public static void logW(String tag, String log, Object... args) {
        android.util.Log.w(getLogTag(tag), getLog(log, null, args));
    }

    public static void logE(String tag, String log, Object... args) {
        android.util.Log.e(getLogTag(tag), getLog(log, null, args));
    }

    public static void logE(String tag, String format, Throwable t, Object... args) {
        android.util.Log.e(getLogTag(tag), getLog(format, t, args));
    }

    private static String getLogTag(String tag) {
        return LOG_TAG + tag;
    }

    private static String getLog(String format, Throwable t, Object... args) {
        String log = (args == null || args.length == 0) ? format : format(format, args);
        if (log == null) {
            log = "";
        }
        if (t != null) {
            log = log + " Exception occurs at " + Log.getStackTraceString(t);
        }
        return log;
    }

    private static String format(String messageFormat, Object... args) {
        try {
            return args == null || args.length == 0 ? messageFormat : formatWitUSLocal(messageFormat, args);
        } catch (Exception e) {
            return messageFormat;
        }
    }

    private static String formatWitUSLocal(String format, Object... args) {
        try {
            return String.format(Locale.US, format, args);
        } catch (Exception e) {
            return "";
        }
    }
}
