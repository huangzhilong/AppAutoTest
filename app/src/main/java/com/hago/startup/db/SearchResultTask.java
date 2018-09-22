package com.hago.startup.db;

import android.support.annotation.IntDef;

import com.hago.startup.ICallback;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by huangzhilong on 18/9/13.
 * 数据查询
 */

public class SearchResultTask extends AbsDbTask<List<MonitorInfo>> {

    private HashMap<String, SearchInfo> searchMap;

    public SearchResultTask(HashMap<String, SearchInfo> searchMap, ICallback<List<MonitorInfo>> callback) {
        super(callback);
        this.searchMap = searchMap;
    }

    @Override
    protected List<MonitorInfo> operateDb() throws Exception {
        Dao<MonitorInfo, Integer> dao = DBHelper.getDBInstance().getMonitorInfoDao();
        QueryBuilder<MonitorInfo, Integer> queryBuilder = dao.queryBuilder();
        if (searchMap != null && searchMap.size() > 0) {
            Where<MonitorInfo, Integer> where = queryBuilder.where();
            Set<String> keySet = searchMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            boolean first = true;
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (first) {
                    handlerOp(where, key, searchMap.get(key));
                    first = false;
                } else {
                    handlerOp(where.and(), key, searchMap.get(key));
                }
            }
        }
        queryBuilder.orderBy("id", false);
        List<MonitorInfo> results = dao.query(queryBuilder.prepare());
        return results;
    }

    private void handlerOp(Where where, String key, SearchInfo info) throws Exception {
        if (info.type == EQ) {
            where.eq(key, info.data);
        } else if (info.type == LT) {
            where.lt(key, info.data);
        } else if (info.type == GT) {
            where.gt(key, info.data);
        } else if (info.type == GE) {
            where.ge(key, info.data);
        } else {
            where.le(key, info.data);
        }
    }

    public static class SearchInfo {
        //查询条件，大于等于还是小于等
        @OP
        public int type;

        public Object data;

        public SearchInfo(@OP int type, Object data) {
            this.type = type;
            this.data = data;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EQ, LT, GT, GE, LE})
    public @interface OP {

    }

    public final static int EQ = 1; // ==
    public final static int LT = 2; // <
    public final static int GT = 3; // >
    public final static int GE = 4; // >=
    public final static int LE = 5; // <=
}
