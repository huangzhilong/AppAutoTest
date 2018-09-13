package com.hago.startup.cmd;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hago.startup.ExecutorsInstance;
import com.hago.startup.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by huangzhilong on 18/9/6.
 */

public abstract class BaseCmd<T> implements Runnable {

    protected String mCmd;

    protected String mTag;

    //此回调在主线程。需要自己操作结果时setCmdCallback
    private ICmdCallback<T> mCmdCallback = new ICmdCallback<T>() {
        @Override
        public void onFailed(String msg) {
            LogUtil.logI(mTag, "mCmdCallback failed: " + msg);
        }

        @Override
        public void onSuccess(T data) {
            LogUtil.logI(mTag, "mCmdCallback onSuccess: " + data);
        }
    };

    public void setCmdCallback(@NonNull ICmdCallback<T> cmdCallback) {
        mCmdCallback = cmdCallback;
    }

    private T result;


    public BaseCmd() {
        mTag = getClass().getSimpleName();
    }

    /**
     * 结果处理
     * @param data
     * @return
     */
    public abstract T parseResult(String data);

    @Override
    public void run() {
        try {
            if (TextUtils.isEmpty(mCmd)) {
                handlerError("mCmd is empty");
                return;
            }
            Process p = Runtime.getRuntime().exec(mCmd);
            StringBuilder stringBuilder = new StringBuilder();
            //有些命令错误也会是getInputStream，需要注意
            if (p != null && p.getInputStream() != null) {
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String data;
                while ((data = in.readLine()) != null && data.length() > 0) {
                    stringBuilder.append(data);
                    stringBuilder.append("\n");
                }
                in.close();
                if (stringBuilder.length() > 0) {
                    handlerSuccess(stringBuilder.toString());
                    return;
                }
            }
            if (p == null || p.getErrorStream() == null) {
                handlerError("p or error stream is null");
                return;
            }

            BufferedReader ie = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String error;
            while ((error = ie.readLine()) != null && error.length() > 0) {
                stringBuilder.append(error);
                stringBuilder.append("\n");
            }
            ie.close();
            handlerError(stringBuilder.toString());
        } catch (Exception e) {
            handlerError(e.getMessage());
        }
    }

    private void handlerError(final String error) {
        ExecutorsInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                mCmdCallback.onFailed(error);
            }
        });
    }

    private void handlerSuccess(String data) {
        result = parseResult(data);
        ExecutorsInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                mCmdCallback.onSuccess(result);
            }
        });
    }
}
