package com.hago.startup;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by huangzhilong on 18/9/6.
 */

public class MonitorTaskInstance {

    private static final MonitorTaskInstance ourInstance = new MonitorTaskInstance();

    public static MonitorTaskInstance getInstance() {
        return ourInstance;
    }

    //只需要一个线程就好啦
    private Executor mExecutor;

    private MonitorTaskInstance() {
        mExecutor = Executors.newSingleThreadExecutor();
    }

    //子线程
    public void executeRunnable(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mExecutor.execute(runnable);
    }

    private static Handler mMainHandler = new Handler(Looper.getMainLooper());

    public void postToMainThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mMainHandler.post(runnable);
    }

    public void postToMainThreadDelay(Runnable runnable, long delay) {
        if (runnable == null) {
            return;
        }
        mMainHandler.postDelayed(runnable, delay);
    }

    public void clearMsgMainThread() {
        mMainHandler.removeCallbacksAndMessages(null);
    }
}
