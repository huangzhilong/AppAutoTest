package com.hago.startup.cmd;

/**
 * Created by huangzhilong on 18/9/6.
 */

public interface ICmdCallback<T> {

    void onFailed(String msg);

    void onSuccess(T data);
}
