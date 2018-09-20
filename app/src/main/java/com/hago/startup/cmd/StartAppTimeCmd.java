package com.hago.startup.cmd;

import android.text.TextUtils;

import com.hago.startup.Constant;
import com.hago.startup.bean.StartCmdInfo;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

/**
 * Created by huangzhilong on 18/9/6.
 */

public class StartAppTimeCmd extends BaseCmd<StartCmdInfo> {

    private String mThisTimeTag = "ThisTime: ";
    private String mTotalTimeTag = "TotalTime: ";
    private String mWaitTimeTag = "WaitTime: ";

    public StartAppTimeCmd() {
        mCmd = Constant.CMD_START_APP;
    }

    @Override
    public StartCmdInfo parseResult(String data) {
        LogUtil.logI(mTag, "parseResult data: " + data);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        StartCmdInfo startCmdInfo = new StartCmdInfo();
        //获取thisTime
        int index = data.indexOf(mThisTimeTag);
        if (index > 0) {
            String num = getNumStr(index + mThisTimeTag.length(), data);
            startCmdInfo.thisTime = Utils.safeParseLong(num);
        } else {
            LogUtil.logI(mTag, "no found thisTime");
        }
        //获取TotalTime
        index = data.indexOf(mTotalTimeTag);
        if (index > 0) {
            String num = getNumStr(index + mTotalTimeTag.length(), data);
            startCmdInfo.totalTime = Utils.safeParseLong(num);
        } else {
            LogUtil.logI(mTag, "no found totalTime");
        }
        //获取WaitTime
        index = data.indexOf(mWaitTimeTag);
        if (index > 0) {
            String num = getNumStr(index + mWaitTimeTag.length(), data);
            startCmdInfo.waitTime = Utils.safeParseLong(num);
        } else {
            LogUtil.logI(mTag, "no found waitTime");
        }
        return startCmdInfo;
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
