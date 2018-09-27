package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;

import java.util.List;

import io.reactivex.Maybe;

/**
 * Created by huangzhilong on 18/9/25.
 */

public class DBCenter {

    private static final DBCenter ourInstance = new DBCenter();

    public static DBCenter getInstance() {
        return ourInstance;
    }

    private DBCenter() {
    }

    public void initDB(Context context) {
        DBHelper.initDB(context);
    }

    //插入测试结果
    public Maybe<Integer> insertResult(ResultInfo resultInfo) {
        InsertResultTask task = new InsertResultTask(resultInfo);
        return DBHelper.getDBInstance().execDbTask(task);
    }

    //根据版本号查询测试结果
    public Maybe<ResultInfo> queryMonitorByVersion(long version) {
        QueryMonitorByVersionTask task = new QueryMonitorByVersionTask(version);
        return DBHelper.getDBInstance().execDbTask(task);
    }

    //根据时间戳获取大于该时间戳的测试结果
    public Maybe<List<ResultInfo>> queryMonitorByTimestamp(long timestamp) {
        QueryMonitorTimestampTask timestampTask = new QueryMonitorTimestampTask(timestamp);
        return DBHelper.getDBInstance().execDbTask(timestampTask);
    }
}
