package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.ICallback;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Created by huangzhilong on 18/9/21.
 * 查询当前数据库中最大的版本号
 */

public class MaxVersionTask extends AbsDbTask<String> {

    public MaxVersionTask(Context context, ICallback<String> callback) {
        super(context, callback);
    }

    @Override
    protected String operateDb() throws Exception {
        Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance(mContext).getMonitorInfoDao();
        QueryBuilder<MonitorInfo, Integer> queryBuilder = dao.queryBuilder();
        queryBuilder.selectRaw("MAX(version)");
        String [] result = dao.queryRaw(queryBuilder.prepareStatementString()).getFirstResult();
        return result[0];
    }
}
