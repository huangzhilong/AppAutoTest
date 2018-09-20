package com.hago.startup.db;

import android.content.Context;

import com.hago.startup.ICallback;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class SearchResultTask extends AbsDbTask<List<MonitorInfo>> {

    private HashMap<String, Object> searchMap;

    public SearchResultTask(HashMap<String, Object> searchMap, Context context, ICallback<List<MonitorInfo>> callback) {
        super(context, callback);
        this.searchMap = searchMap;
    }

    @Override
    public void run() {
        try {
            Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance(mContext).getMonitorInfoDao();
            QueryBuilder<MonitorInfo, Integer> queryBuilder = dao.queryBuilder();
            if (searchMap != null && searchMap.size() > 0) {
                Where<MonitorInfo, Integer> where = queryBuilder.where();
                Set<String> keySet = searchMap.keySet();
                Iterator<String> iterator = keySet.iterator();
                boolean first = true;
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (first) {
                        where.eq(key, searchMap.get(key));
                        first = false;
                    } else {
                        where.and().eq(key, searchMap.get(key));
                    }
                }
            }
            queryBuilder.orderBy("id", false);
            List<MonitorInfo> results = dao.query(queryBuilder.prepare());
            handleSuccess(results);
        } catch (Exception e) {
            handleFailed(e);
        }
    }
}
