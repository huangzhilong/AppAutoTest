package com.hago.startup;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import com.hago.startup.bean.StartupInfo;
import com.hago.startup.net.OkHttpUtil;
import com.hago.startup.receiver.AppInstallReceiver;
import com.hago.startup.receiver.StartupTimeReceiver;

import java.util.concurrent.TimeUnit;

import io.reactivex.MaybeSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvStart;
    private TextView tvState;
    private boolean accessibility = false;
    private StartupTimeReceiver mStartupTimeReceiver;
    private AppInstallReceiver mAppInstallReceiver;
    private Disposable mDisposable;
    private String stateTxt = "辅助功能状态: %s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStart = findViewById(R.id.tv_start);
        tvState = findViewById(R.id.tv_state);
        tvStart.setOnClickListener(this);
        tvState.setOnClickListener(this);
        registerService();
        Utils.checkFilePermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        accessibility = Utils.isStartAccessibilityService(this);
        updateStateView();
    }

    private void updateStateView() {
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
        tvState.setText(spannableString);
    }

    private void startMonitor() {
        //若装了hago先卸载
        mDisposable = NotificationCenter.INSTANCE.unInstall(MainActivity.this)
                .flatMap(new Function<Boolean, MaybeSource<String>>() {
                    @Override
                    public MaybeSource<String> apply(Boolean aBoolean) throws Exception {
                        //获取最新构建包地址
                        return OkHttpUtil.getInstance().getDownloadUrl();
                    }
                }).flatMap(new Function<String, MaybeSource<String>>() {
                    @Override
                    public MaybeSource<String> apply(String s) throws Exception {
                        //下载
                        return OkHttpUtil.getInstance().startDownloadApk(s);
                    }
                }).flatMap(new Function<String, MaybeSource<Boolean>>() {
                    @Override
                    public MaybeSource<Boolean> apply(String s) throws Exception {
                        //安装
                        return NotificationCenter.INSTANCE.getInstall(s, MainActivity.this);
                    }
                }).delay(2, TimeUnit.SECONDS)
                .flatMap(new Function<Boolean, MaybeSource<StartupInfo>>() {
                    @Override
                    public MaybeSource<StartupInfo> apply(Boolean aBoolean) throws Exception {
                        //先启动安装的第一次
                        return NotificationCenter.INSTANCE.getAppInfo();
                    }
                }).delay(2, TimeUnit.SECONDS) //这个延时很有必要，不然getAppInfo的emitter大概率在doFinally会被置null
                .flatMap(new Function<StartupInfo, MaybeSource<StartupInfo>>() {
                    @Override
                    public MaybeSource<StartupInfo> apply(StartupInfo startupInfo) throws Exception {
                        //不计算第一次启动结果
                        LogUtil.logI("MainActivity", "first time startMonitor StartupInfo: " + startupInfo);
                        //启动获取启动结果
                        return NotificationCenter.INSTANCE.getAppInfo();
                    }
                }).subscribe(new Consumer<StartupInfo>() {
                    @Override
                    public void accept(StartupInfo startupInfo) throws Exception {
                        LogUtil.logI("MainActivity", "startMonitor StartupInfo: " + startupInfo);
                        //postNextMonitorRunnable();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI("MainActivity", "startMonitor throwable: " + throwable);
                        //postNextMonitorRunnable();
                    }
                });
    }

    private void postNextMonitorRunnable() {
        LogUtil.logI("MainActivity", "postNextMonitorRunnable");
        ExecutorsInstance.getInstance().postToMainThreadDelay(new Runnable() {
            @Override
            public void run() {
                startMonitor();
            }
        }, Constant.START_MONITOR_INTERVAL);
    }

    private void registerService() {
        mAppInstallReceiver = new AppInstallReceiver();
        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addDataScheme("package");
        registerReceiver(mAppInstallReceiver, installFilter);

        mStartupTimeReceiver = new StartupTimeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(StartupTimeReceiver.ACTION);
        registerReceiver(mStartupTimeReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAppInstallReceiver);
        unregisterReceiver(mStartupTimeReceiver);
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.isDisposed();
        }
    }


    @Override
    public void onClick(View v) {
        if (v == tvStart) {
            startMonitor();
        } else if (v == tvState) {
            if (accessibility) {
                return;
            }
            Utils.openAccessibilityService(this);
        }
    }
}

