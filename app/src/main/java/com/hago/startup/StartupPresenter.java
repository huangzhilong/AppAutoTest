package com.hago.startup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.ResultInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.InsertResultTask;
import com.hago.startup.db.MonitorInfo;
import com.hago.startup.db.SearchResultTask;
import com.hago.startup.mail.MailInfo;
import com.hago.startup.mail.MailSender;
import com.hago.startup.util.CommonPref;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.OkHttpUtil;
import com.hago.startup.util.TableUtil;
import com.hago.startup.util.Utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.MaybeSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
    //最后一个跑过自动化成功的版本号
    private long mCurVersion;
    //当前测试结果
    private ApkInfo mApkInfo;

    public StartupPresenter(IStartupView view) {
        mView = view;
        mContext = (Context) mView;
        CommonPref.INSTANCE.init(mContext);
        accessibility = Utils.isStartAccessibilityService(mContext);
        timerStartMonitor();
    }

    private void timerStartMonitor() {
        mCurVersion = CommonPref.INSTANCE.getLong(Constant.MONITOR_VERSION);
        LogUtil.logI(TAG, "get mCurVersion: %s", mCurVersion);
        MonitorTaskInstance.getInstance().postToMainThread(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (accessibility) {
                startMonitor();
            } else {
                Toast.makeText(mContext, "请开启辅助功能，不然无法自动化测试!!!", Toast.LENGTH_LONG);
            }
            MonitorTaskInstance.getInstance().postToMainThreadDelay(this, Constant.START_MONITOR_INTERVAL);
        }
    };

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
                        return OkHttpUtil.getInstance().getDownloadUrl(mCurVersion);
                    }
                }).flatMap(new Function<String, MaybeSource<ApkInfo>>() {
                    @Override
                    public MaybeSource<ApkInfo> apply(String s) throws Exception {
                        //下载
                        mView.updateStepView(String.format(stepTxt, "下载apk中....."));
                        return OkHttpUtil.getInstance().startDownloadApk(s);
                    }
                }).delay(1, TimeUnit.SECONDS)
                .flatMap(new Function<ApkInfo, MaybeSource<Boolean>>() {
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
                        mView.updateStepView(String.format(stepTxt, "数据存储....."));
                        return storeResult(resultInfo);
                    }
                }).flatMap(new Function<Boolean, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(Boolean aBoolean) throws Exception {
                        mView.updateStepView(String.format(stepTxt, "发送邮件....."));
                        return sendToMail();
                    }
                }).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean results) throws Exception {
                        LogUtil.logI(TAG, "startMonitor completed!");
                        mView.updateStepView("测试完成");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "startMonitor failed throwable: " + throwable);
                        mView.updateStepView("此次测试失败");
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
        return resultInfo;
    }

    private MaybeEmitter<Boolean> mDbEmitter;

    private Maybe<Boolean> storeResult(final ResultInfo resultInfo) {
        return Maybe.create(new MaybeOnSubscribe<Boolean>() {
            @Override
            public void subscribe(MaybeEmitter<Boolean> e) throws Exception {
                mDbEmitter = e;
                insertResult(resultInfo);
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mDbEmitter = null;
            }
        }).subscribeOn(Schedulers.io());
    }

    private void insertResult(final ResultInfo info) {
        InsertResultTask task = new InsertResultTask(info, mContext, new ICallback<Integer>() {
            @Override
            public void onFailed(String msg) {
                LogUtil.logI(TAG, "mInsertCallback onFailed: %s", msg);
                mView.updateStepView(String.format(stepTxt, "存储数据库失败"));
                Utils.safeEmitterError(mDbEmitter, new Exception("存储数据库失败"));
            }

            @Override
            public void onSuccess(Integer data) {
                LogUtil.logI(TAG, "mInsertCallback onSuccess: %s", data);
                mView.updateStepView(String.format(stepTxt, "存储数据库成功"));
                //更新版本号
                mCurVersion = Utils.safeParseLong(info.mApkInfo.version);
                CommonPref.INSTANCE.putLong(Constant.MONITOR_VERSION, mCurVersion);
                LogUtil.logI(TAG, "update MONITOR_VERSION : %s", mCurVersion);

                Utils.safeEmitterSuccess(mDbEmitter, true);
            }
        });
        MonitorTaskInstance.getInstance().executeRunnable(task);
    }


    private MaybeEmitter<Boolean> mMailEmitter;

    private Maybe<Boolean> sendToMail() {
        return Maybe.create(new MaybeOnSubscribe<Boolean>() {
            @Override
            public void subscribe(MaybeEmitter<Boolean> e) throws Exception {
                mMailEmitter = e;
                sendMail();
            }
        }).timeout(20, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mMailEmitter = null;
            }
        }).subscribeOn(Schedulers.io());
    }

    private void sendMail() {
        long timestamp = CommonPref.INSTANCE.getLong(Constant.MAIL_TIMESTAMP);
        String date = CommonPref.INSTANCE.getString(Constant.MAIL_DATE);
        //获取当前时间
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; //0开始算
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final String today = year + "-" + month + "-" + day;
        LogUtil.logI(TAG, "sendMail timestamp: %s date: %s today: %s  hour: %s", timestamp, date, today, hour);
        if (!today.equals(date) && hour > 5) {
            HashMap<String, SearchResultTask.SearchInfo> hashMap = new HashMap<>();
            SearchResultTask.SearchInfo searchInfo = new SearchResultTask.SearchInfo(SearchResultTask.GT, timestamp);
            hashMap.put("timestamp", searchInfo);
            SearchResultTask task = new SearchResultTask(hashMap, mContext, new ICallback<List<MonitorInfo>>() {
                @Override
                public void onFailed(String msg) {
                    LogUtil.logI(TAG, "sendMail search failed : %s", msg);
                }

                @Override
                public void onSuccess(List<MonitorInfo> data) {
                    LogUtil.logI(TAG, "sendMail search result size : %s", data.size());
                    //开始发送邮件
                    String mailContent = TableUtil.createMailTableText(data);
                    String title = "Hago " + today + " 自动化测试数据";
                    final MailInfo mailInfo = new MailInfo();
                    mailInfo.setUserName(Constant.USER); // 你的邮箱地址
                    mailInfo.setPassword(Constant.PWD);// 您的邮箱密码
                    mailInfo.setToAddress(Constant.TO_ADDRESS); // 发到哪个邮件去
                    mailInfo.setSubject(title); // 邮件主题
                    mailInfo.setContent(mailContent); // 邮件文本
                    final MailSender sms = new MailSender();
                    MonitorTaskInstance.getInstance().executeRunnable(new Runnable() {
                        @Override
                        public void run() {
                            boolean result = sms.sendTextMail(mailInfo);
                            if (result) {
                                CommonPref.INSTANCE.putLong(Constant.MAIL_TIMESTAMP, System.currentTimeMillis());
                                CommonPref.INSTANCE.putString(Constant.MAIL_DATE, today);
                            }
                            Utils.safeEmitterSuccess(mMailEmitter, result);
                        }
                    });
                }
            });
            MonitorTaskInstance.getInstance().executeRunnable(task);
        } else {
            Utils.safeEmitterSuccess(mMailEmitter, false);
            LogUtil.logI(TAG, "not need sendMail");
        }
    }



    public void release() {
        if (mStartDisposable != null && !mStartDisposable.isDisposed()) {
            mStartDisposable.isDisposed();
        }
    }
}
