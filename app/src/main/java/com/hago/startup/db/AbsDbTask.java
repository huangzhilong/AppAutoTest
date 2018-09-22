package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.MonitorTaskInstance;
import com.hago.startup.ICallback;

/**
 * Created by huangzhilong on 18/9/13.
 */

public abstract class AbsDbTask<T> implements Runnable {

    protected ICallback<T> mCallback;
    protected int retryTime = 1; //失败自动重试一次

    public AbsDbTask(ICallback<T> callback) {
        mCallback = callback;
    }

    protected abstract T operateDb() throws Exception;

    @Override
    public void run() {
        while (retryTime >= 0) {
            try {
                T data = operateDb();
                handleSuccess(data);
                break;
            } catch (Exception e) {
                retryTime--;
                if (retryTime < 0) {
                    handleFailed(e);
                    break;
                }
            }
        }
    }

    //回调应该回到主线程
    protected void handleSuccess(final T data) {
        MonitorTaskInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onSuccess(data);
                }
            }
        });
    }

    protected void handleFailed(final Exception e) {
        MonitorTaskInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onFailed(e.getMessage());
                }
            }
        });
    }
}
