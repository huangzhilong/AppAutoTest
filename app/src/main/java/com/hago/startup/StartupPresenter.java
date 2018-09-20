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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.MaybeSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
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
    private Disposable mRealDisposable;

    //当前测试结果
    private ApkInfo mApkInfo;
    private List<StartupInfo> mResultList = new ArrayList<>(Constant.START_COUNT);

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
        mResultList.clear();
    }

    //卸载－>下载－>安装 －> 启动第一次
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
                .flatMap(new Function<Boolean, MaybeSource<StartupInfo>>() {
                    @Override
                    public MaybeSource<StartupInfo> apply(Boolean aBoolean) throws Exception {
                        //先启动安装的第一次, 第一次结果不计算
                        mView.updateStepView(String.format(stepTxt, "安装后第一次启动app"));
                        return NotificationCenter.INSTANCE.getAppInfo();
                    }
                }).delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<StartupInfo>() {
                    @Override
                    public void accept(StartupInfo startupInfo) throws Exception {
                        LogUtil.logI(TAG, "realMonitor get first startupInfo: %s", startupInfo);
                        realMonitor();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "startMonitor failed throwable: " + throwable);
                    }
                });
    }

    //获取真正的启动数据，启动3次求平均
    private void realMonitor() {
        mView.updateStepView(String.format(stepTxt, "第" + (mResultList.size() + 1) + "次获取app启动信息"));
        mRealDisposable = NotificationCenter.INSTANCE.getAppInfo()
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<StartupInfo>() {
                    @Override
                    public void accept(@NonNull StartupInfo startupInfo) throws Exception {
                        mResultList.add(startupInfo);
                        LogUtil.logI(TAG, "realMonitor size: %s  data: %s", mResultList.size(), startupInfo);
                        if (mResultList.size() < Constant.START_COUNT) {
                            realMonitor();
                        } else {
                            LogUtil.logI(TAG, "monitor success completed!");
                            mView.updateStepView(String.format(stepTxt, "完成自动化测试"));
                            handlerResult();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "realMonitor size: %s  throwable: %s", mResultList.size(), throwable);
                        if (mResultList.size() < Constant.START_COUNT) {
                            realMonitor();
                        }
                    }
                });
    }

    //取平均
    private void handlerResult() {
        mView.updateStepView(String.format(stepTxt, "结果处理...."));
        StartupInfo info = mResultList.get(0);
        for (int i = 1; i < mResultList.size(); i++) {
            info.mStartupTime.totalTime += mResultList.get(i).mStartupTime.totalTime;
            info.mStartupTime.waitTime += mResultList.get(i).mStartupTime.waitTime;
            info.mStartupTime.thisTime += mResultList.get(i).mStartupTime.thisTime;
            info.mStartupData.startupMemory += mResultList.get(i).mStartupData.startupMemory;
            info.mStartupData.startTime += mResultList.get(i).mStartupData.startTime;
        }
        info.mStartupTime.totalTime /= mResultList.size();
        info.mStartupTime.waitTime /= mResultList.size();
        info.mStartupTime.thisTime /= mResultList.size();
        info.mStartupData.startupMemory /= mResultList.size();
        info.mStartupData.startTime /= mResultList.size();
        ResultInfo resultInfo = new ResultInfo(mApkInfo, info);
        LogUtil.logI(TAG, "handlerResult: %s", resultInfo);
        //插入数据库
        InsertResultTask task = new InsertResultTask(resultInfo, mInsertCallback, mContext);
        MonitorTaskInstance.getInstance().executeRunnable(task);
    }

    public void release() {
        if (mStartDisposable != null && !mStartDisposable.isDisposed()) {
            mStartDisposable.isDisposed();
        }
        if (mRealDisposable != null && !mRealDisposable.isDisposed()) {
            mRealDisposable.dispose();
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
        }

        @Override
        public void onSuccess(Integer data) {
            LogUtil.logI(TAG, "mInsertCallback onSuccess: %s", data);
            mView.updateStepView(String.format(stepTxt, "存储数据库成功"));
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
