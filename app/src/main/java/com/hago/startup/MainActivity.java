package com.hago.startup;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.receiver.AppInstallReceiver;
import com.hago.startup.receiver.StartAppReceiver;
import com.hago.startup.util.Utils;
import com.hago.startup.widget.DialogManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, IStartupView {

    private static final String TAG = "MainActivity";
    private Button tvTarget;
    private Button tvStart;
    private Button btnResult;
    private TextView tvState;
    private TextView tvStep;
    private TextView tvApk;
    private StartAppReceiver mStartupTimeReceiver;
    private AppInstallReceiver mAppInstallReceiver;
    private StartupPresenter mStartupPresenter;
    private DialogManager mDialogManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTarget = findViewById(R.id.tv_target);
        tvStart = findViewById(R.id.tv_start);
        btnResult = findViewById(R.id.tv_result);
        tvState = findViewById(R.id.tv_state);
        tvStep = findViewById(R.id.tv_step);
        tvApk = findViewById(R.id.tv_apk);
        btnResult.setOnClickListener(this);
        tvTarget.setOnClickListener(this);
        tvStart.setOnClickListener(this);
        tvState.setOnClickListener(this);
        registerService();
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

    @Override
    public void updateApkView(final String text) {
        MonitorTaskInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                tvApk.setText(text);
            }
        });
    }

    @Override
    public void showOpenAccessibilityTipDialog() {
        getDialogManager().showOkCancleCancelBigTips("提示", "必须开启辅助功能才能进行自动化测试!!!", new DialogManager.OkCancelDialogListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onOk() {
                mStartupPresenter.openAccessibilityService();
            }
        });
    }

    @Override
    public void showChooseApkVersionDialog(DialogManager.ChooseDialogListener listener) {
        getDialogManager().showChooseApkVersionDialog(listener);
    }

    @Override
    public void showResultViewDialog(List<ResultInfo> resultInfoList, boolean target) {
        getDialogManager().showResultDialog(resultInfoList, target);
    }

    @Override
    public void changeStartAutoTestBtn(final int visibility) {
        MonitorTaskInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                if (tvStart.getVisibility() != visibility) {
                    tvStart.setVisibility(visibility);
                }
            }
        });
    }

    private DialogManager getDialogManager() {
        if (mDialogManager == null) {
            mDialogManager = new DialogManager(this);
        }
        return mDialogManager;
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
    }


    @Override
    public void onClick(View v) {
        if (v == tvTarget) {
            mStartupPresenter.startTargetMonitor();
        } else if (v == tvState) {
            mStartupPresenter.openAccessibilityService();
        } else if (v == tvStart) {
            //开启自动化测试
            mStartupPresenter.timerStartMonitor();
        } else if (v == btnResult) {
            mStartupPresenter.checkResult();
        }
    }
}

