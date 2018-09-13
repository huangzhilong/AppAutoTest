package com.hago.startup;

/**
 * Created by huangzhilong on 18/9/6.
 */

public interface ICallback<T> {

    void onFailed(String msg);

    void onSuccess(T data);
}
