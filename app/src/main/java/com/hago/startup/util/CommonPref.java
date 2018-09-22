package com.hago.startup.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.hago.startup.Constant;

/**
 * Created by huangzhilong on 18/9/21.
 */

public enum CommonPref {

    INSTANCE;

    public static final String COMMONREF_NAME = "CommonPref";

    private SharedPreferences mPreferences;

    public void init(Context context) {
        mPreferences = context.getSharedPreferences(COMMONREF_NAME, Context.MODE_PRIVATE);
    }

    public String getString(@NonNull String key) {
        return mPreferences.getString(key, Constant.EMPTYSTR);
    }

    public void putString(@NonNull String key, @NonNull String value) {
        mPreferences.edit().putString(key, value).commit();
    }

    public long getLong(@NonNull String key) {
        return mPreferences.getLong(key, 0);
    }

    public void putLong(@NonNull String key, long value) {
        mPreferences.edit().putLong(key, value).commit();
    }
}
