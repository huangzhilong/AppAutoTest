package com.hago.startup.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.hago.startup.Constant.*;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by huangzhilong on 18/9/26.
 */

@Retention(SOURCE)
@IntDef({IDLE, RUN_AUTO, RUN_TARGET, WAIT})
public @interface MonitorState {
}