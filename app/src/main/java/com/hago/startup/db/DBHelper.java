package com.hago.startup.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hago.startup.ICallback;
import com.hago.startup.MonitorTaskInstance;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.VersionInfo;
import com.hago.startup.notify.RxJavaUtil;
import com.hago.startup.util.LogUtil;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;

/**
 * Created by huangzhilong on 18/9/13.
 */

class DBHelper extends OrmLiteSqliteOpenHelper  {

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
            TableUtils.createTableIfNotExists(connectionSource, VersionInfo.class);
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
    private Dao<VersionInfo, Integer> mVersionInfoDao;

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

    public Dao<VersionInfo, Integer> getVersionInfoDao() {
        if (mVersionInfoDao == null) {
            try {
                mVersionInfoDao = getDao(VersionInfo.class);
            } catch (SQLException e) {
                LogUtil.logE(TAG, "getVersionInfoDao ex: %s", e);
            }
        }
        return mVersionInfoDao;
    }

    /**
     * 执行数据操作
     * @param task
     * @param <T>
     * @return
     */
    public <T> Maybe<T> execDbTask(final AbsDbTask<T> task) {
        return Maybe.create(new MaybeOnSubscribe<T>() {
            @Override
            public void subscribe(final MaybeEmitter<T> e) throws Exception {
                task.setCallback(new ICallback<T>() {
                    @Override
                    public void onFailed(String msg) {
                        RxJavaUtil.safeEmitterError(e, new Exception(msg));
                    }

                    @Override
                    public void onSuccess(T data) {
                        RxJavaUtil.safeEmitterSuccess(e, data);
                    }
                });
                MonitorTaskInstance.getInstance().executeRunnable(task);
            }
        }).timeout(10, TimeUnit.SECONDS);
    }
}
