package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.ICallback;
import com.hago.startup.util.Utils;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Created by huangzhilong on 18/9/21.
 * 查询当前数据库中最大的版本号
 */

public class MaxVersionTask extends AbsDbTask<Long> {

    public MaxVersionTask(Context context, ICallback<Long> callback) {
        super(context, callback);
    }

    @Override
    protected Long operateDb() throws Exception {
        Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance(mContext).getMonitorInfoDao();
        QueryBuilder<MonitorInfo, Integer> queryBuilder = dao.queryBuilder();
        queryBuilder.selectRaw("MAX(version)");
        String [] result = dao.queryRaw(queryBuilder.prepareStatementString()).getFirstResult();
        if (result == null || result.length == 0) {
            return 0L;
        }
        return Utils.safeParseLong(result[0]);
    }
}
