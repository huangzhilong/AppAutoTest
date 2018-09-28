package com.hago.startup.bean;

import android.support.annotation.IntDef;

import com.hago.startup.Constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by huangzhilong on 18/9/28.
 */

@Retention(RetentionPolicy.SOURCE)
@IntDef({Constant.IDLE, Constant.RUN_AUTO, Constant.WAIT_AUTO, Constant.RUN_TARGET})
public @interface MonitorState {
}
