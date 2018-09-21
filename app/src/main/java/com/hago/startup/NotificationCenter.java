package com.hago.startup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.hago.startup.bean.StartAppInfo;
import com.hago.startup.bean.StartCmdInfo;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.cmd.StartAppTimeCmd;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

/**
 * Created by huangzhilong on 18/9/11.
 */

public enum NotificationCenter {

    INSTANCE;

    private static final String TAG = "NotificationCenter";

    //app自己计算的时间和内存
    private MaybeEmitter<StartAppInfo> mStartAppEmitter;

    //cmd启动时间
    private MaybeEmitter<StartCmdInfo> mStartCmdEmitter;

    //app安装
    private MaybeEmitter<Boolean> mInstallEmitter;

    //app卸载
    private MaybeEmitter<Boolean> mUnInstallEmitter;

    //结果获取
    private MaybeEmitter<List<StartupInfo>> mResultEmitter;

    /**
     * 启动app
     * @return
     */
    private Maybe<StartupInfo> getAppInfo() {
        return Maybe.zip(
                getAppStartInfo(),
                getAppStartTime(),
                new BiFunction<StartAppInfo, StartCmdInfo, StartupInfo>() {
                    @Override
                    public StartupInfo apply(StartAppInfo startupData, StartCmdInfo startupTime) throws Exception {
                        StartupInfo startupInfo = new StartupInfo();
                        startupInfo.mStartAppInfo = startupData;
                        startupInfo.mStartCmdInfo = startupTime;
                        return startupInfo;
                    }
                }
        ).timeout(30, TimeUnit.SECONDS);
    }

    private Maybe<StartAppInfo> getAppStartInfo() {
        return Maybe.create(new MaybeOnSubscribe<StartAppInfo>() {
            @Override
            public void subscribe(MaybeEmitter<StartAppInfo> e) throws Exception {
                mStartAppEmitter = e;
            }
        }).timeout(30, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mStartAppEmitter = null;
            }
        });
    }

    private Maybe<StartCmdInfo> getAppStartTime() {
        return Maybe.create(new MaybeOnSubscribe<StartCmdInfo>() {
            @Override
            public void subscribe(MaybeEmitter<StartCmdInfo> e) throws Exception {
                mStartCmdEmitter = e;
                StartAppTimeCmd startAppCmd = new StartAppTimeCmd();
                startAppCmd.setCmdCallback(mStartupTimeCmdCallback);
                MonitorTaskInstance.getInstance().executeRunnable(startAppCmd);
            }
        }).timeout(30, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mStartCmdEmitter = null;
            }
        });
    }

    private ICallback<StartCmdInfo> mStartupTimeCmdCallback = new ICallback<StartCmdInfo>() {
        @Override
        public void onFailed(String msg) {
            LogUtil.logI(TAG, "mStartupTimeCmdCallback onFailed " + msg);
        }

        @Override
        public void onSuccess(StartCmdInfo data) {
            LogUtil.logI(TAG, "mStartupTimeCmdCallback success " + data);
            emitterStartCmd(data);
        }
    };

    /**
     *
     * @param count app 启动次数
     * @return
     */
    public Maybe<List<StartupInfo>> getStartResult(final int count) {
        return Maybe.create(new MaybeOnSubscribe<List<StartupInfo>>() {
            @Override
            public void subscribe(MaybeEmitter<List<StartupInfo>> e) throws Exception {
                mResultEmitter = e;
                mResult = new ArrayList<>(count);
                startApp(count);
            }
        }).timeout(30 * count, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mResultEmitter = null;
            }
        });
    }

    private List<StartupInfo> mResult;

    private void startApp(final int count) {
        getAppInfo().delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<StartupInfo>() {
                    @Override
                    public void accept(@NonNull StartupInfo startupInfo) throws Exception {
                        mResult.add(startupInfo);
                        LogUtil.logI(TAG, "startApp result size: %s  data: %s", mResult.size(), startupInfo);
                        if (mResult.size() < count) {
                            startApp(count);
                        } else {
                            LogUtil.logI(TAG, "startApp completed!");
                            Utils.safeEmitterSuccess(mResultEmitter, mResult);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logI(TAG, "startApp size: %s  throwable: %s", mResult.size(), throwable);
                        if (mResult.size() < count) {
                            startApp(count);
                        }
                    }
                });
    }

    /**
     * 安装app
     * @param path
     * @param context
     * @return
     */
    public Maybe<Boolean> getInstall(final String path, final Context context) {
        return Maybe.create(new MaybeOnSubscribe<Boolean>() {
            @Override
            public void subscribe(MaybeEmitter<Boolean> e) throws Exception {
                mInstallEmitter = e;
                smartInstall(path, context);
            }
        }).timeout(60, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mInstallEmitter = null;
            }
        });
    }

    /**
     * 检测是否已经安装hago，安装了旧卸载
     *
     * @param context
     * @return
     */
    public Maybe<Boolean> unInstall(final Context context) {
        return Maybe.create(new MaybeOnSubscribe<Boolean>() {
            @Override
            public void subscribe(MaybeEmitter<Boolean> e) throws Exception {
                mUnInstallEmitter = e;
                startUnInstall(context);
            }
        }).timeout(30, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mUnInstallEmitter = null;
            }
        });
    }

    public void emitterInstall() {
        Utils.safeEmitterSuccess(mInstallEmitter, true);
    }

    public void emitterStartInfo(StartAppInfo startupData) {
        Utils.safeEmitterSuccess(mStartAppEmitter, startupData);
    }

    public void emitterStartCmd(StartCmdInfo startupTime) {
        Utils.safeEmitterSuccess(mStartCmdEmitter, startupTime);
    }

    public void emitterUnInstall() {
        Utils.safeEmitterSuccess(mUnInstallEmitter, true);
    }

    private void smartInstall(String path, Context context) {
        LogUtil.logI(TAG, "smartInstall path: " + path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File apkFile = new File(path);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void startUnInstall(Context context) {
        LogUtil.logI(TAG, "startUnInstall");
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        boolean installed = false;
        if (packageInfoList != null) {
            for (int i = 0; i < packageInfoList.size(); i++) {
                PackageInfo packageInfo = packageInfoList.get(i);
                if (packageInfo == null) {
                    continue;
                }
                String packName = packageInfo.packageName;
                if (Constant.HIYO_PACKAGENAME.equals(packName)) {
                    installed = true;
                    break;
                }
            }
        }
        LogUtil.logI(TAG, "installed: " + String.valueOf(installed));
        if (!installed) {
            emitterUnInstall();
        } else {
            LogUtil.logI(TAG, "unInstall com.yy.hiyo");
            Uri uri = Uri.fromParts("package", "com.yy.hiyo", null);
            Intent intent = new Intent(Intent.ACTION_DELETE, uri);
            context.startActivity(intent);
        }
    }
}
