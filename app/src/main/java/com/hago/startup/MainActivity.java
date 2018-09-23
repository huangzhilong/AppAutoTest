package com.hago.startup;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.hago.startup.db.DBHelper;
import com.hago.startup.receiver.AppInstallReceiver;
import com.hago.startup.receiver.StartAppReceiver;
import com.hago.startup.util.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IStartupView {

    private static final String TAG = "MainActivity";
    private TextView tvStop;
    private TextView tvStart;
    private TextView tvState;
    private TextView tvStep;
    private StartAppReceiver mStartupTimeReceiver;
    private AppInstallReceiver mAppInstallReceiver;
    private StartupPresenter mStartupPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStop = findViewById(R.id.tv_stop);
        tvStart = findViewById(R.id.tv_start);
        tvState = findViewById(R.id.tv_state);
        tvStep = findViewById(R.id.tv_step);
        tvStop.setOnClickListener(this);
        tvStart.setOnClickListener(this);
        tvState.setOnClickListener(this);
        registerService();
        DBHelper.initDB(this);
        Utils.checkFilePermission(this);
        mStartupPresenter = new StartupPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStartupPresenter.checkStartAccessibilityService();
    }

    @Override
    public void updateAccessibilityStateView(boolean open, SpannableString spannableString) {
        tvState.setText(spannableString);
    }

    @Override
    public void updateStepView(final String text) {
        MonitorTaskInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                tvStep.setText(text);
            }
        });
    }

    private void registerService() {
        mAppInstallReceiver = new AppInstallReceiver();
        IntentFilter installFilter = new IntentFilter();
        installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        installFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        installFilter.addDataScheme("package");
        registerReceiver(mAppInstallReceiver, installFilter);

        mStartupTimeReceiver = new StartAppReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(StartAppReceiver.ACTION);
        registerReceiver(mStartupTimeReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAppInstallReceiver);
        unregisterReceiver(mStartupTimeReceiver);
        mStartupPresenter.release();
    }


    @Override
    public void onClick(View v) {
        if (v == tvStop) {
            //关闭app
            MonitorTaskInstance.getInstance().clearMsgMainThread();
            int pid = Process.myPid();
            System.exit(0);
            android.os.Process.killProcess(pid);
        } else if (v == tvState) {
            mStartupPresenter.openAccessibilityService();
        } else if (v == tvStart) {
            tvStart.setVisibility(View.GONE);
            mStartupPresenter.timerStartMonitor();
        }
    }
}

