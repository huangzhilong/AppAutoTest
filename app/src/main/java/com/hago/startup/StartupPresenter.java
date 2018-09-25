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
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.DBCenter;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.mail.MailInfo;
import com.hago.startup.mail.MailSender;
import com.hago.startup.net.RequestCenter;
import com.hago.startup.util.ApkInfoUtil;
import com.hago.startup.util.CommonPref;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.TableUtil;
import com.hago.startup.util.Utils;
import com.hago.startup.widget.DialogManager;

import java.util.Calendar;
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
import io.reactivex.functions.Predicate;
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
    }

    public void startTargetMonitor() {
        if (!accessibility) {
            mView.showOpenAccessibilityTipDialog();
            return;
        }
        //停止在进行的自动化
        release();
        MonitorTaskInstance.getInstance().clearMainThreadMsg();
        mView.showChooseApkVersionDialog(new DialogManager.ChooseDialogListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void ok(List<ApkInfo> results) {
                LogUtil.logD(TAG, "showChooseApkVersionDialog result: %s", results);
            }
        });
    }

    public void timerStartMonitor() {
        if (!accessibility) {
            mView.showOpenAccessibilityTipDialog();
            return;
        }
        mCurVersion = CommonPref.INSTANCE.getLong(Constant.MONITOR_VERSION);
        LogUtil.logI(TAG, "get mCurVersion: %s", mCurVersion);
        MonitorTaskInstance.getInstance().postToMainThread(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (accessibility) {
                startMonitor(false);
            } else {
                Toast.makeText(mContext, "请开启辅助功能，不然无法自动化测试!!!", Toast.LENGTH_LONG).show();
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
    /**
     * 开始自动化测试
     *
     * @param target 是否指定了测试版本
     */
    public void startMonitor(final boolean target) {
        clearData();
        mView.updateStepView("卸载已安装的hago");
        NotificationCenter.INSTANCE.unInstall(mContext)
                .flatMap(new Function<Boolean, MaybeSource<String>>() {
                    @Override
                    public MaybeSource<String> apply(Boolean aBoolean) throws Exception {
                        //获取最新构建包地址
                        mView.updateStepView("获取apk下载地址");
                        return RequestCenter.getInstance().getNewestApkUrl();
                    }
                }).filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        //指定测试版本不要要判断
                        if (target) {
                            return true;
                        }
                        ApkInfo apkInfo = ApkInfoUtil.getApkInfo(s);
                        long version = Utils.safeParseLong(apkInfo.version);
                        boolean result = version > mCurVersion;
                        if (!result) {
                            mView.updateStepView("最新构建版本不高于已测试版本");
                            LogUtil.logI(TAG, "最新构建版本不高于已测试版本 version: %s  curVersion: %s", version, mCurVersion);
                        }
                        return true;
                    }
                }).flatMap(new Function<String, MaybeSource<ApkInfo>>() {
                    @Override
                    public MaybeSource<ApkInfo> apply(String s) throws Exception {
                        //下载
                        mView.updateStepView("下载apk中.....");
                        return RequestCenter.getInstance().startDownloadApk(s);
                    }
                }).delay(1, TimeUnit.SECONDS)
                .flatMap(new Function<ApkInfo, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(ApkInfo s) throws Exception {
                        mApkInfo = s;
                        //安装
                        mView.updateApkView("测试包：" + mApkInfo.version);
                        mView.updateStepView("安装apk中.....");
                        return NotificationCenter.INSTANCE.getInstall(s.filePath, mContext);
                    }
                }).delay(2, TimeUnit.SECONDS)
                .flatMap(new Function<Boolean, MaybeSource<List<StartupInfo>>>() {
                    @Override
                    public MaybeSource<List<StartupInfo>> apply(Boolean aBoolean) throws Exception {
                        mView.updateStepView("启动获取app数据中.....");
                        return NotificationCenter.INSTANCE.getStartResult(Constant.START_COUNT);
                    }
                }).map(new Function<List<StartupInfo>, List<StartupInfo>>() {
                    @Override
                    public List<StartupInfo> apply(List<StartupInfo> result) throws Exception {
                        mView.updateStepView("结果处理中.....");
                        return handlerResult(result);
                    }
                }).flatMap(new Function<List<StartupInfo>, MaybeSource<Integer>>() {
                    @Override
                    public MaybeSource<Integer> apply(List<StartupInfo> resultInfo) throws Exception {
                        mView.updateStepView("数据存储.....");
                        return DBCenter.getInstance().insertResult(mApkInfo, resultInfo);
                    }
                }).filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        //判断是否需要邮件
                        return judgeNeedMail();
                    }
                }).flatMap(new Function<Integer, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(Integer integer) throws Exception {
                        mView.updateStepView("发送邮件.....");
                        return sendToMail();
                    }
                }).subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean results) throws Exception {
                        LogUtil.logI(TAG, "startMonitor completed! send mail result: %s", results);
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
    private List<StartupInfo> handlerResult(List<StartupInfo> mResultList) {
        mView.updateStepView(String.format(stepTxt, "结果处理...."));
        //去除第一次启动app数据
        mResultList.remove(0);
        LogUtil.logI(TAG, "handlerResult: %s", mResultList);
        return mResultList;
    }

    private boolean judgeNeedMail() {
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
        if (!today.equals(date) && hour > Constant.SEND_MAIL_TIME) {
            return true;
        } else {
            return false;
        }
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
        DBCenter.getInstance().queryMonitorByTimestamp(timestamp)
                .subscribe(new Consumer<List<ResultInfo>>() {
                    @Override
                    public void accept(List<ResultInfo> data) throws Exception {
                        LogUtil.logI(TAG, "sendMail search result size : %s", data.size());
                        //开始发送邮件
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH) + 1; //0开始算
                        int day = calendar.get(Calendar.DATE);
                        final String today = year + "-" + month + "-" + day;
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
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "queryMonitorByTimestamp failed : %s", throwable);
                        Utils.safeEmitterError(mMailEmitter, throwable);
                    }
                });
    }



    public void release() {
        if (mStartDisposable != null && !mStartDisposable.isDisposed()) {
            mStartDisposable.isDisposed();
        }
    }
}
