package com.hago.startup.cmd.list;

import android.text.TextUtils;

import com.hago.startup.Constant;
import com.hago.startup.LogUtil;
import com.hago.startup.Utils;
import com.hago.startup.bean.StartupTime;
import com.hago.startup.cmd.BaseCmd;

/**
 * Created by huangzhilong on 18/9/6.
 */

public class StartAppTimeCmd extends BaseCmd<StartupTime> {

    private String mThisTimeTag = "ThisTime: ";
    private String mTotalTimeTag = "TotalTime: ";
    private String mWaitTimeTag = "WaitTime: ";

    public StartAppTimeCmd() {
        mCmd = Constant.CMD_START_APP;
    }

    @Override
    public StartupTime parseResult(String data) {
        LogUtil.logI(mTag, "parseResult data: " + data);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        StartupTime startupTime = new StartupTime();
        //获取thisTime
        int index = data.indexOf(mThisTimeTag);
        if (index > 0) {
            String num = getNumStr(index + mThisTimeTag.length(), data);
            startupTime.thisTime = Utils.safeParseLong(num);
        } else {
            LogUtil.logI(mTag, "no found thisTime");
        }
        //获取TotalTime
        index = data.indexOf(mTotalTimeTag);
        if (index > 0) {
            String num = getNumStr(index + mTotalTimeTag.length(), data);
            startupTime.totalTime = Utils.safeParseLong(num);
        } else {
            LogUtil.logI(mTag, "no found thisTime");
        }
        //获取WaitTime
        index = data.indexOf(mWaitTimeTag);
        if (index > 0) {
            String num = getNumStr(index + mWaitTimeTag.length(), data);
            startupTime.waitTime = Utils.safeParseLong(num);
        } else {
            LogUtil.logI(mTag, "no found thisTime");
        }
        return startupTime;
    }

    private String getNumStr(int index, String data) {
        int startIndex = index;
        for (int i = startIndex; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c >= '0' && c <= '9') {
                index++;
            } else {
                break;
            }
        }
        String num = data.substring(startIndex, index);
        return num;
    }
}
