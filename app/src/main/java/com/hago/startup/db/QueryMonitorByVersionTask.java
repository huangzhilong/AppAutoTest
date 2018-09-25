package com.hago.startup.db;

import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.db.bean.VersionInfo;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/25.
 */

public class QueryMonitorByVersionTask extends AbsDbTask<ResultInfo>{

    private long version;

    public QueryMonitorByVersionTask(long version) {
        this.version = version;
    }

    @Override
    protected ResultInfo operateDb() throws Exception {
        Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance().getMonitorInfoDao();
        QueryBuilder<MonitorInfo, Integer> queryBuilder = dao.queryBuilder();
        queryBuilder.where().eq("version", version);
        List<MonitorInfo> results = dao.query(queryBuilder.prepare());
        if (Utils.empty(results)) {
            LogUtil.logI("QueryMonitorByVersionTask", "result size 0");
            return ResultInfo.RESULT_EMPTY;
        }
        LogUtil.logI("QueryMonitorByVersionTask", "result size: %s", results == null ? 0 : results.size());

        QueryBuilder<VersionInfo, Integer> builder = DBHelper.getDBInstance().getVersionInfoDao().queryBuilder();
        builder.where().eq("version", version);
        VersionInfo versionInfo = DBHelper.getDBInstance().getVersionInfoDao().queryForFirst(builder.prepare());
        if (versionInfo == null) {
            LogUtil.logI("QueryMonitorByVersionTask", "versionInfo is null");
            return ResultInfo.RESULT_EMPTY;
        }
        ResultInfo resultInfo = new ResultInfo(versionInfo, results);
        return resultInfo;
    }
}
