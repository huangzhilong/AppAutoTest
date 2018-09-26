package com.hago.startup.db;

import android.support.annotation.NonNull;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.db.bean.VersionInfo;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class InsertResultTask extends AbsDbTask<Integer> {

    private static final String TAG = "InsertResultTask";
    private ResultInfo mResultInfo;

    public InsertResultTask(@NonNull ApkInfo apkInfo, @NonNull List<StartupInfo> startupInfoList) {
        mResultInfo = new ResultInfo(apkInfo, startupInfoList);
    }

    @Override
    protected Integer operateDb() throws Exception {
        QueryBuilder<VersionInfo, Integer> queryBuilder = DBHelper.getDBInstance().getVersionInfoDao().queryBuilder();
        Where<VersionInfo, Integer> where = queryBuilder.where();
        where.eq("version", mResultInfo.mVersionInfo.version);
        List<VersionInfo> results = DBHelper.getDBInstance().getVersionInfoDao().query(queryBuilder.prepare());

        if (Utils.empty(results)) {
            int result = DBHelper.getDBInstance().getVersionInfoDao().create(mResultInfo.mVersionInfo);
            LogUtil.logI(TAG, "insert version result: %s  info: %s", result, mResultInfo.mVersionInfo);
        }
        final Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance().getMonitorInfoDao();
        int result = dao.callBatchTasks(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int count = 0;
                for (int i = 0; i < mResultInfo.mMonitorInfoList.size(); i++) {
                    MonitorInfo info = mResultInfo.mMonitorInfoList.get(i);
                    if (info == null) {
                        continue;
                    }
                    dao.create(info);
                    count++;
                }
                return count;
            }
        });
        LogUtil.logI(TAG, "insert MonitorInfo result: %s", result);
        return result;
    }
}
