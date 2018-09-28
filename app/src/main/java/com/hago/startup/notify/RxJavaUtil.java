package com.hago.startup.notify;

import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

import io.reactivex.MaybeEmitter;

/**
 * Created by huangzhilong on 18/9/28.
 */

public class RxJavaUtil {

    public static <T> void safeEmitterSuccess(MaybeEmitter<T> emitter, T data) {
        if (emitter == null || emitter.isDisposed()) {
            LogUtil.logI("Utils", "safeEmitterData emitter not available");
            return;
        }
        if (Utils.mCancelEmitter) {
            emitter.onError(new Exception("主动cancel Emitter"));
            return;
        }
        emitter.onSuccess(data);
    }

    public static void safeEmitterError(MaybeEmitter emitter, Throwable throwable) {
        if (emitter == null || emitter.isDisposed()) {
            LogUtil.logI("Utils", "safeEmitterError emitter not available");
            return;
        }
        if (throwable == null) {
            LogUtil.logI("Utils", "safeEmitterError throwable is null");
            throwable = new Exception("未知错误");
        }
        emitter.onError(throwable);
    }
}
