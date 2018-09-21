package com.hago.startup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.ResultInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.InsertResultTask;
import com.hago.startup.db.MonitorInfo;
import com.hago.startup.db.SearchResultTask;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.OkHttpUtil;
import com.hago.startup.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.MaybeSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by huangzhilong on 18/9/7.
 */

public class StartupPresenter {

    private static final String TAG = "StartupPresenter";
    private IStartupView mView;
    private Context mContext;
    //是否开启了辅助功能
    private boolean accessibility = false;
    private String stateTxt = "辅助功能状态: %s";
    private String stepTxt = "当前步骤: %s";

    private Disposable mStartDisposable;
    //当前测试结果
    private ApkInfo mApkInfo;

    public StartupPresenter(IStartupView view) {
        mView = view;
        mContext = (Context) mView;
    }

    public void checkStartAccessibilityService() {
        accessibility = Utils.isStartAccessibilityService(mContext);
        String state = "已开启";
        int color = Color.GREEN;
        if (!accessibility) {
            state = "未开启";
            color = Color.RED;
        }
        state = String.format(stateTxt, state);
        SpannableString spannableString = new SpannableString(state);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
        spannableString.setSpan(colorSpan, spannableString.length() - 3, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mView.updateAccessibilityStateView(accessibility, spannableString);
    }

    public void openAccessibilityService() {
        if (accessibility) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        mContext.startActivity(intent);
    }

    private void clearData() {
        mApkInfo = null;
    }

    //卸载－>下载－>安装 －> 启动app -> 结果插入数据库 —> 邮件
    public void startMonitor() {
        clearData();
        mView.updateStepView(String.format(stepTxt, "检测卸载已安装的hago"));
        mStartDisposable = NotificationCenter.INSTANCE.unInstall(mContext)
                .flatMap(new Function<Boolean, MaybeSource<String>>() {
                    @Override
                    public MaybeSource<String> apply(Boolean aBoolean) throws Exception {
                        //获取最新构建包地址
                        mView.updateStepView(String.format(stepTxt, "获取最新包地址"));
                        return OkHttpUtil.getInstance().getDownloadUrl();
                    }
                }).flatMap(new Function<String, MaybeSource<ApkInfo>>() {
                    @Override
                    public MaybeSource<ApkInfo> apply(String s) throws Exception {
                        //下载
                        mView.updateStepView(String.format(stepTxt, "下载apk中....."));
                        return OkHttpUtil.getInstance().startDownloadApk(s);
                    }
                }).flatMap(new Function<ApkInfo, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(ApkInfo s) throws Exception {
                        mApkInfo = s;
                        //安装
                        mView.updateStepView(String.format(stepTxt, "安装apk中....."));
                        return NotificationCenter.INSTANCE.getInstall(s.filePath, mContext);
                    }
                }).delay(2, TimeUnit.SECONDS)
                .flatMap(new Function<Boolean, MaybeSource<List<StartupInfo>>>() {
                    @Override
                    public MaybeSource<List<StartupInfo>> apply(Boolean aBoolean) throws Exception {
                        mView.updateStepView(String.format(stepTxt, "启动获取app数据中....."));
                        return NotificationCenter.INSTANCE.getStartResult(Constant.START_COUNT);
                    }
                }).map(new Function<List<StartupInfo>, ResultInfo>() {
                    @Override
                    public ResultInfo apply(List<StartupInfo> result) throws Exception {
                        mView.updateStepView(String.format(stepTxt, "结果处理中....."));
                        return handlerResult(result);
                    }
                }).flatMap(new Function<ResultInfo, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(ResultInfo resultInfo) throws Exception {
                        mView.updateStepView(String.format(stepTxt, "数据库存储....."));
                        return insertResultDb(resultInfo);
                    }
                }).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean results) throws Exception {
                        LogUtil.logI(TAG, "get startup result: %s", results);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "startMonitor failed throwable: " + throwable);
                    }
                });
    }

    //去掉第一次启动数据，在取平均
    private ResultInfo handlerResult(List<StartupInfo> mResultList) {
        mView.updateStepView(String.format(stepTxt, "结果处理...."));
        //去除第一次启动app数据
        mResultList.remove(0);
        //求平均
        StartupInfo info = mResultList.get(0);
        for (int i = 1; i < mResultList.size(); i++) {
            info.mStartCmdInfo.totalTime += mResultList.get(i).mStartCmdInfo.totalTime;
            info.mStartCmdInfo.waitTime += mResultList.get(i).mStartCmdInfo.waitTime;
            info.mStartCmdInfo.thisTime += mResultList.get(i).mStartCmdInfo.thisTime;
            info.mStartAppInfo.startupMemory += mResultList.get(i).mStartAppInfo.startupMemory;
            info.mStartAppInfo.startTime += mResultList.get(i).mStartAppInfo.startTime;
        }
        info.mStartCmdInfo.totalTime /= mResultList.size();
        info.mStartCmdInfo.waitTime /= mResultList.size();
        info.mStartCmdInfo.thisTime /= mResultList.size();
        info.mStartAppInfo.startupMemory /= mResultList.size();
        info.mStartAppInfo.startTime /= mResultList.size();
        ResultInfo resultInfo = new ResultInfo(mApkInfo, info);
        LogUtil.logI(TAG, "handlerResult: %s", resultInfo);
        //插入数据库
        InsertResultTask task = new InsertResultTask(resultInfo, mInsertCallback, mContext);
        MonitorTaskInstance.getInstance().executeRunnable(task);
        return resultInfo;
    }

    private MaybeEmitter<Boolean> mDbEmitter;

    private Maybe<Boolean> insertResultDb(ResultInfo resultInfo) {
        return Maybe.create(new MaybeOnSubscribe<Boolean>() {
            @Override
            public void subscribe(MaybeEmitter<Boolean> e) throws Exception {
                mDbEmitter = e;
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mDbEmitter = null;
            }
        });
    }

    public void release() {
        if (mStartDisposable != null && !mStartDisposable.isDisposed()) {
            mStartDisposable.isDisposed();
        }
    }

    public void queryMoitorResult(HashMap<String, Object> searchMap) {
        SearchResultTask task = new SearchResultTask(searchMap, mContext, mSearchCallback);
        MonitorTaskInstance.getInstance().executeRunnable(task);
    }

    //插入数据库回调
    private ICallback<Integer> mInsertCallback = new ICallback<Integer>() {

        @Override
        public void onFailed(String msg) {
            LogUtil.logI(TAG, "mInsertCallback onFailed: %s", msg);
            mView.updateStepView(String.format(stepTxt, "存储数据库失败"));
            Utils.safeEmitterSuccess(mDbEmitter, false);
        }

        @Override
        public void onSuccess(Integer data) {
            LogUtil.logI(TAG, "mInsertCallback onSuccess: %s", data);
            mView.updateStepView(String.format(stepTxt, "存储数据库成功"));
            Utils.safeEmitterSuccess(mDbEmitter, true);
        }
    };

    private ICallback<List<MonitorInfo>> mSearchCallback = new ICallback<List<MonitorInfo>>() {
        @Override
        public void onFailed(String msg) {
            LogUtil.logI(TAG, "mSearchCallback onFailed: %s", msg);
        }

        @Override
        public void onSuccess(List<MonitorInfo> data) {
            LogUtil.logI(TAG, "mInsertCallback onSuccess: %s", data);
        }
    };
}
