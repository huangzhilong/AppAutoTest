package com.hago.startup.db;

import android.support.annotation.NonNull;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.VersionInfo;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class InsertResultTask extends AbsDbTask<Integer> {

    private static final String TAG = "InsertResultTask";
    private List<MonitorInfo> mMonitorInfo;
    private VersionInfo mVersionInfo;

    public InsertResultTask(@NonNull ApkInfo apkInfo, @NonNull List<StartupInfo> startupInfoList) {
        mVersionInfo = getVersionInfoByResult(apkInfo, startupInfoList);
        mMonitorInfo = getMonitorInfoByResult(startupInfoList);
    }

    @Override
    protected Integer operateDb() throws Exception {
        QueryBuilder<VersionInfo, Integer> queryBuilder = DBHelper.getDBInstance().getVersionInfoDao().queryBuilder();
        Where<VersionInfo, Integer> where = queryBuilder.where();
        where.eq("version", mVersionInfo.version);
        List<VersionInfo> results = DBHelper.getDBInstance().getVersionInfoDao().query(queryBuilder.prepare());

        if (Utils.empty(results)) {
            int result = DBHelper.getDBInstance().getVersionInfoDao().create(mVersionInfo);
            LogUtil.logI(TAG, "insert version result: %s  info: %s", result, mVersionInfo);
        }
        final Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance().getMonitorInfoDao();
        int result = dao.callBatchTasks(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int count = 0;
                for (int i = 0; i < mMonitorInfo.size(); i++) {
                    MonitorInfo info = mMonitorInfo.get(i);
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

    private VersionInfo getVersionInfoByResult(ApkInfo apkInfo, List<StartupInfo> list) {
        VersionInfo info = new VersionInfo();
        info.branch = apkInfo.branch;
        info.size = apkInfo.size;
        info.version = Utils.safeParseLong(apkInfo.version);
        info.isDebug = list.get(0).mStartAppInfo.isDebug;
        return info;
    }

    private List<MonitorInfo> getMonitorInfoByResult(List<StartupInfo> startupInfoList) {
        List<MonitorInfo> results = new ArrayList<>(startupInfoList.size());
        for (int i = 0; i < startupInfoList.size(); i++) {
            StartupInfo startupInfo = startupInfoList.get(i);
            MonitorInfo monitorInfo = new MonitorInfo();
            monitorInfo.thisTime = startupInfo.mStartCmdInfo.thisTime;
            monitorInfo.totalTime = startupInfo.mStartCmdInfo.totalTime;
            monitorInfo.waitTime = startupInfo.mStartCmdInfo.waitTime;
            monitorInfo.startTime = startupInfo.mStartAppInfo.startTime;
            monitorInfo.startupMemory = startupInfo.mStartAppInfo.startupMemory;
            monitorInfo.timestamp = System.currentTimeMillis();
            monitorInfo.version = mVersionInfo.version;
            results.add(monitorInfo);
        }
        return results;
    }
}
