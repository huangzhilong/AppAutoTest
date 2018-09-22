package com.hago.startup.db;

import com.hago.startup.ICallback;
import com.hago.startup.bean.ResultInfo;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class InsertResultTask extends AbsDbTask<Integer> {

    private MonitorInfo mMonitorInfo;

    public InsertResultTask(ResultInfo info, ICallback<Integer> callback) {
        super(callback);
        mMonitorInfo = new MonitorInfo(info);
    }

    @Override
    protected Integer operateDb() throws Exception {
        return DBHelper.getDBInstance().getMonitorInfoDao().create(mMonitorInfo);
    }
}
