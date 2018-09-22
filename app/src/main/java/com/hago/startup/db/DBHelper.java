package com.hago.startup.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hago.startup.util.LogUtil;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by huangzhilong on 18/9/13.
 */

public class DBHelper extends OrmLiteSqliteOpenHelper  {

    private static final String TAG = "DBHelper";
    private static final int VERSION = 1;

    private static final String DB_NAME = "monitor.db";

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static void initDB(Context context) {
        if (mDBInstance == null) {
            synchronized (DBHelper.class) {
                if (mDBInstance == null) {
                    mDBInstance = new DBHelper(context);
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, MonitorInfo.class);
        } catch (SQLException e) {
            LogUtil.logE(TAG, "onCreate ex: %s", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

    }

    private static DBHelper mDBInstance;

    private Dao<MonitorInfo, Integer> mMonitorInfoDao;

    public static DBHelper getDBInstance() {
        return mDBInstance;
    }

    public Dao<MonitorInfo, Integer> getMonitorInfoDao() {
        if (mMonitorInfoDao == null) {
            try {
                mMonitorInfoDao = getDao(MonitorInfo.class);
            } catch (SQLException e) {
                LogUtil.logE(TAG, "getMonitorInfoDao ex: %s", e);
            }
        }
        return mMonitorInfoDao;
    }
}
