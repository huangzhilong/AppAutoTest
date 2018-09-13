package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.ExecutorsInstance;
import com.hago.startup.ICallback;

/**
 * Created by huangzhilong on 18/9/13.
 */

public abstract class AbsDbTask<T> implements Runnable {

    protected Context mContext;
    protected ICallback<T> mCallback;

    public AbsDbTask(Context context, ICallback<T> callback) {
        mContext = context;
        mCallback = callback;
    }

    //回调应该回到主线程
    protected void handleSuccess(final T data) {
        ExecutorsInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onSuccess(data);
                }
            }
        });
    }

    protected void handleFailed(final Exception e) {
        ExecutorsInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onFailed(e.getMessage());
                }
            }
        });
    }
}
