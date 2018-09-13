package com.hago.startup;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.hago.startup.bean.StartupData;
import com.hago.startup.bean.StartupInfo;
import com.hago.startup.bean.StartupTime;
import com.hago.startup.cmd.ICmdCallback;
import com.hago.startup.cmd.list.StartAppTimeCmd;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;

/**
 * Created by huangzhilong on 18/9/11.
 */

public enum NotificationCenter {

    INSTANCE;

    private static final String TAG = "NotificationCenter";

    private MaybeEmitter<StartupData> mStartupDataMaybeEmitter;

    private MaybeEmitter<StartupTime> mStartupTimeMaybeEmitter;

    private MaybeEmitter<Boolean> mInstallMaybeEmitter;

    private MaybeEmitter<Boolean> mUnInstallMaybeEmitter;

    /**
     * 获取启动结果
     *
     * @return
     */
    public Maybe<StartupInfo> getAppInfo() {
        return Maybe.zip(
                getAppStartInfo(),
                getAppStartTime(),
                new BiFunction<StartupData, StartupTime, StartupInfo>() {
                    @Override
                    public StartupInfo apply(StartupData startupData, StartupTime startupTime) throws Exception {
                        StartupInfo startupInfo = new StartupInfo();
                        startupInfo.mStartupData = startupData;
                        startupInfo.mStartupTime = startupTime;
                        return startupInfo;
                    }
                }
        ).timeout(30, TimeUnit.SECONDS);
    }

    /**
     * 安装app
     *
     * @param path
     * @param context
     * @return
     */
    public Maybe<Boolean> getInstall(final String path, final Context context) {
        return Maybe.create(new MaybeOnSubscribe<Boolean>() {
            @Override
            public void subscribe(MaybeEmitter<Boolean> e) throws Exception {
                mInstallMaybeEmitter = e;
                smartInstall(path, context);
            }
        }).timeout(60, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mInstallMaybeEmitter = null;
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
                mUnInstallMaybeEmitter = e;
                startUnInstall(context);
            }
        }).timeout(30, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mUnInstallMaybeEmitter = null;
            }
        });
    }

    private Maybe<StartupData> getAppStartInfo() {
        return Maybe.create(new MaybeOnSubscribe<StartupData>() {
            @Override
            public void subscribe(MaybeEmitter<StartupData> e) throws Exception {
                mStartupDataMaybeEmitter = e;
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mStartupDataMaybeEmitter = null;
            }
        });
    }

    private Maybe<StartupTime> getAppStartTime() {
        return Maybe.create(new MaybeOnSubscribe<StartupTime>() {
            @Override
            public void subscribe(MaybeEmitter<StartupTime> e) throws Exception {
                mStartupTimeMaybeEmitter = e;
                startAppCmd();
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mStartupTimeMaybeEmitter = null;
            }
        });
    }

    public void emitterInstall() {
        Utils.safeEmitterSuccess(mInstallMaybeEmitter, true);
    }

    public void emitterStartData(StartupData startupData) {
        Utils.safeEmitterSuccess(mStartupDataMaybeEmitter, startupData);
    }

    public void emitterStartTime(StartupTime startupTime) {
        Utils.safeEmitterSuccess(mStartupTimeMaybeEmitter, startupTime);
    }

    public void emitterUnInstall() {
        Utils.safeEmitterSuccess(mUnInstallMaybeEmitter, true);
    }

    private void smartInstall(String path, Context context) {
        LogUtil.logI(TAG, "smartInstall path: " + path);
        // Uri uri = getUriForFile(new File(path), context);
        // Intent localIntent = new Intent(Intent.ACTION_VIEW);
        // localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
        // context.startActivity(localIntent);
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

    private void startAppCmd() {
        StartAppTimeCmd startAppCmd = new StartAppTimeCmd();
        startAppCmd.setCmdCallback(mStartupTimeCmdCallback);
        ExecutorsInstance.getInstance().executeRunnable(startAppCmd);
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


    private ICmdCallback<StartupTime> mStartupTimeCmdCallback = new ICmdCallback<StartupTime>() {
        @Override
        public void onFailed(String msg) {
            LogUtil.logI(TAG, "mStartupTimeCmdCallback onFailed " + msg);
        }

        @Override
        public void onSuccess(StartupTime data) {
            LogUtil.logI(TAG, "mStartupTimeCmdCallback success " + data);
            emitterStartTime(data);
        }
    };
}
