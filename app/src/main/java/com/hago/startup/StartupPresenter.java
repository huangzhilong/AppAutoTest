package com.hago.startup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Toast;

import com.hago.startup.bean.ApkInfo;
import com.hago.startup.bean.MonitorState;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.db.DBCenter;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.mail.MailInfo;
import com.hago.startup.mail.MailSender;
import com.hago.startup.net.RequestCenter;
import com.hago.startup.notify.NotificationCenter;
import com.hago.startup.notify.RxJavaUtil;
import com.hago.startup.util.CommonPref;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.TableUtil;
import com.hago.startup.util.Utils;
import com.hago.startup.widget.DialogManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.MaybeSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
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
    //最后一个跑过自动化成功的版本号
    private long mCurVersion;
    //当前测试包信息
    private ApkInfo mApkInfo = new ApkInfo();
    //----记录最后一次测试结果
    private List<ResultInfo> mTargetResult; //最后指定包
    private ResultInfo mLastResultInfo; // 最后自动化结果
    private boolean isTarget;
    //---------
    private int mMonitorState;

    public void setMonitorState(@MonitorState int monitorState) {
        mMonitorState = monitorState;
        if (mMonitorState == Constant.IDLE) {
            mView.changeStartAutoTestBtn(View.VISIBLE);
        } else {
            mView.changeStartAutoTestBtn(View.GONE);
        }
    }

    public StartupPresenter(IStartupView view) {
        mView = view;
        mContext = (Context) mView;
        CommonPref.INSTANCE.init(mContext);
        DBCenter.getInstance().initDB(mContext);
        setMonitorState(Constant.IDLE);
    }

    //指定包测试
    public void startTargetMonitor() {
        if (!accessibility) {
            mView.showOpenAccessibilityTipDialog();
            return;
        }
        if (mMonitorState == Constant.RUN_AUTO) {
            Toast.makeText(mContext, "正在自动化测试，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mMonitorState == Constant.RUN_TARGET) {
            Toast.makeText(mContext, "已经进行指定包测试了!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        MonitorTaskInstance.getInstance().clearMainThreadMsg(mRunnable);
        mView.showChooseApkVersionDialog(new DialogManager.ChooseDialogListener() {
            @Override
            public void onCancel() {
                setMonitorState(Constant.IDLE);
            }

            @Override
            public void ok(List<ApkInfo> results) {
                LogUtil.logD(TAG, "showChooseApkVersionDialog result: %s", results);
                mTargetResult = new ArrayList<>(results.size());
                startTargetTest(results);
            }
        });
    }

    //指定包测试
    private void startTargetTest(final List<ApkInfo> list) {
        setMonitorState(Constant.RUN_TARGET);
        final ApkInfo apkInfo = list.get(mTargetResult.size());
        LogUtil.logI(TAG, "startTargetTest apkInfo: %s", apkInfo);
        getStartMonitor(true, list.get(mTargetResult.size()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ResultInfo>() {
                    @Override
                    public void accept(ResultInfo resultInfo) throws Exception {
                        mTargetResult.add(resultInfo);
                        targetTest(list);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "startTargetTest throwable! %s", throwable);
                        mTargetResult.add(new ResultInfo(apkInfo));
                        targetTest(list);
                    }
                });
    }

    private void targetTest(List<ApkInfo> list) {
        if (mTargetResult.size() < list.size()) {
            startTargetTest(list);
        } else {
            mView.updateStepView("指定包测试完成");
            isTarget = true;
            LogUtil.logI(TAG, "startTargetTest completed! %s", mTargetResult);
            setMonitorState(Constant.IDLE);
        }
    }

    //自动拉取包测试
    public void timerStartMonitor() {
        if (!accessibility) {
            mView.showOpenAccessibilityTipDialog();
            return;
        }
        if (mMonitorState == Constant.RUN_TARGET) {
            Toast.makeText(mContext, "正在指定包测试，请稍后再试", Toast.LENGTH_SHORT).show();
            return;
        }
        mCurVersion = CommonPref.INSTANCE.getLong(Constant.MONITOR_VERSION);
        LogUtil.logI(TAG, "get mCurVersion: %s", mCurVersion);
        MonitorTaskInstance.getInstance().clearMainThreadMsg(mRunnable);
        MonitorTaskInstance.getInstance().postToMainThread(mRunnable);
    }

    //自动化测试
    private void startAutoTest() {
        mApkInfo.reset();
        setMonitorState(Constant.RUN_AUTO);
        getStartMonitor(false, mApkInfo)
                .flatMap(new Function<ResultInfo, MaybeSource<Integer>>() {
                    @Override
                    public MaybeSource<Integer> apply(ResultInfo resultInfo) throws Exception {
                        mView.updateStepView("数据存储.....");
                        isTarget = false;
                        mLastResultInfo = resultInfo;
                        return DBCenter.getInstance().insertResult(resultInfo);
                    }
                }).filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        //判断是否需要邮件
                        boolean mail = judgeNeedMail();
                        //更新已经测试过的版本号
                        CommonPref.INSTANCE.putLong(Constant.MONITOR_VERSION, Utils.safeParseLong(mApkInfo.version));
                        LogUtil.logI(TAG, "update MONITOR_VERSION version: %s", mApkInfo.version);
                        if (!mail) {
                            finishAutoTest("测试完成，无需发送邮件");
                        }
                        return mail;
                    }
                }).flatMap(new Function<Integer, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(Integer integer) throws Exception {
                            mView.updateStepView("发送邮件.....");
                            return sendToMail();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean results) throws Exception {
                        LogUtil.logI(TAG, "是否成功发送了邮件: %s", results);
                        finishAutoTest("测试完成");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "startAutoTest failed throwable: " + throwable);
                        finishAutoTest(throwable.getMessage());
                    }
                });
    }

    private void finishAutoTest(String msg) {
        LogUtil.logI(TAG, "finishAutoTest: %s", msg);
        mView.updateStepView(msg);
        //需要执行target
        setMonitorState(Constant.WAIT_AUTO);
        MonitorTaskInstance.getInstance().postToMainThreadDelay(mRunnable, Constant.START_MONITOR_INTERVAL);
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            startAutoTest();
        }
    };

    //查看结果
    public void checkResult() {
        List<ResultInfo> list;
        if (isTarget) {
            list = mTargetResult;
        } else {
            list = new ArrayList<>(1);
            if (mLastResultInfo != null) {
                list.add(mLastResultInfo);
            }
        }
        mView.showResultViewDialog(list, isTarget);
    }


    public void checkStartAccessibilityService() {
        accessibility = Utils.isStartAccessibilityService(mContext);
        String state = "已开启";
        int color = Color.GREEN;
        if (!accessibility) {
            state = "未开启";
            color = Color.RED;
        }
        state = "辅助功能状态: " + state;
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



    //去掉第一次启动数据
    private ResultInfo handlerResult(ApkInfo apkInfo, List<StartupInfo> mResultList, boolean isTarget) {
        mView.updateStepView("结果处理....");
        //去除第一次启动app数据
        mResultList.remove(0);
        ResultInfo resultInfo = new ResultInfo(apkInfo, mResultList);
        return resultInfo;
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
                                RxJavaUtil.safeEmitterSuccess(mMailEmitter, result);
                            }
                        });
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "queryMonitorByTimestamp failed : %s", throwable);
                        RxJavaUtil.safeEmitterError(mMailEmitter, throwable);
                    }
                });
    }

    /**
     *
     * @param target 是否是指定版本
     * @param mApkInfo 指定版本测试传入有的apkInfo，自动化传入reset的mApkInfo
     * @return
     */
    private Maybe<ResultInfo> getStartMonitor(final boolean target, final ApkInfo mApkInfo) {
        mView.updateStepView("获取最新构建包......");
        return RequestCenter.getInstance().getNewestApkUrl(mApkInfo)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        long version = Utils.safeParseLong(mApkInfo.version);
                        mView.updateApkView("测试包：" + mApkInfo.version);
                        if (!mApkInfo.checkAvailability()) {
                            LogUtil.logI(TAG, "mAkpInfo data is error: %s", mApkInfo);
                            return false;
                        }
                        //指定测试版本不要要判断
                        if (target) {
                            return true;
                        }
                        boolean result = version > mCurVersion;
                        if (!result) {
                            String msg = "最新构建版本不高于已测试版本";
                            finishAutoTest(msg);
                            LogUtil.logI(TAG, msg + " version: %s  curVersion: %s", version, mCurVersion);
                        }
                        return result;
                    }
                }).flatMap(new Function<String, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(String s) throws Exception {
                        mView.updateStepView("卸载已安装的hago");
                        return NotificationCenter.INSTANCE.unInstall(mContext);
                    }
                }).flatMap(new Function<Boolean, MaybeSource<ApkInfo>>() {
                    @Override
                    public MaybeSource<ApkInfo> apply(Boolean aBoolean) throws Exception {
                        //下载
                        mView.updateStepView("下载apk中.....");
                        return RequestCenter.getInstance().startDownloadApk(mApkInfo);
                    }
                }).flatMap(new Function<ApkInfo, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(ApkInfo s) throws Exception {
                        //安装
                        mView.updateStepView("安装apk中.....");
                        return NotificationCenter.INSTANCE.getInstall(mApkInfo.filePath, mContext);
                    }
                }).delay(2, TimeUnit.SECONDS)
                .flatMap(new Function<Boolean, MaybeSource<List<StartupInfo>>>() {
                    @Override
                    public MaybeSource<List<StartupInfo>> apply(Boolean aBoolean) throws Exception {
                        mView.updateStepView("启动获取app数据中.....");
                        return NotificationCenter.INSTANCE.getStartResult(Constant.START_COUNT);
                    }
                }).map(new Function<List<StartupInfo>, ResultInfo>() {
                    @Override
                    public ResultInfo apply(List<StartupInfo> result) throws Exception {
                        mView.updateStepView("结果处理中.....");
                        return handlerResult(mApkInfo, result, isTarget);
                    }
                });
    }
}
