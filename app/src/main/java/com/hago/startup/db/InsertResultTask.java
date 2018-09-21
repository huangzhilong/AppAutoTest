package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.ICallback;
import com.hago.startup.bean.ResultInfo;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class InsertResultTask extends AbsDbTask<Integer> {

    private MonitorInfo mMonitorInfo;

    public InsertResultTask(ResultInfo info, Context context, ICallback<Integer> callback) {
        super(context, callback);
        mMonitorInfo = new MonitorInfo(info);
    }

    @Override
    protected Integer operateDb() throws Exception {
        return DBHelper.getDBInstance(mContext).getMonitorInfoDao().create(mMonitorInfo);
    }
}
