package com.hago.startup.db;

import com.hago.startup.db.AbsDbTask;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.db.bean.VersionInfo;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangzhilong on 18/9/25.
 */

public class QueryMonitorTimestampTask extends AbsDbTask<List<ResultInfo>>{

    private long timestamp;

    public QueryMonitorTimestampTask(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    protected List<ResultInfo> operateDb() throws Exception {
        Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance().getMonitorInfoDao();
        QueryBuilder<MonitorInfo, Integer> queryBuilder = dao.queryBuilder();
        queryBuilder.where().gt("timestamp", timestamp);
        List<MonitorInfo> results = dao.query(queryBuilder.prepare());
        LogUtil.logI("QueryMonitorTimestampTask", "result size: %s", results == null ? 0 : results.size());
        if (Utils.empty(results)) {
            return Collections.EMPTY_LIST;
        }
        Set<Long> versionSet = new HashSet<>();
        for (int i = 0; i < results.size(); i++) {
            MonitorInfo info = results.get(i);
            if (info == null) {
                continue;
            }
            versionSet.add(info.version);
        }
        QueryBuilder<VersionInfo, Integer> builder = DBHelper.getDBInstance().getVersionInfoDao().queryBuilder();
        builder.where().in("version", versionSet);
        List<VersionInfo> versionInfoList = DBHelper.getDBInstance().getVersionInfoDao().query(builder.prepare());
        if (Utils.empty(results)) {
            LogUtil.logI("QueryMonitorTimestampTask", "versionInfoList is empty");
            return Collections.EMPTY_LIST;
        }
        List<ResultInfo> resultInfoList = new ArrayList<>(versionInfoList.size());
        for (int i = 0; i < versionInfoList.size(); i++) {
            VersionInfo versionInfo = versionInfoList.get(i);
            if (versionInfo == null) {
                continue;
            }
            List<MonitorInfo> monitorInfos = getMonitorByVersion(versionInfo, results);
            if (Utils.empty(monitorInfos)) {
                continue;
            } else {
                resultInfoList.add(new ResultInfo(versionInfo, monitorInfos));
            }
        }
        return resultInfoList;
    }

    private List<MonitorInfo> getMonitorByVersion(VersionInfo versionInfo, List<MonitorInfo> monitorInfos) {
        List<MonitorInfo> list = new ArrayList<>();
        for (int i = 0; i < monitorInfos.size(); i++) {
            MonitorInfo info= monitorInfos.get(i);
            if (info == null) {
                continue;
            }
            if (versionInfo != null && versionInfo.version == info.version) {
                list.add(info);
            }
        }
        return list;
    }
}
