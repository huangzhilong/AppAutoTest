package com.hago.startup.util;

import android.text.TextUtils;

import com.hago.startup.Constant;
import com.hago.startup.bean.ApkInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.functions.Action;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class OkHttpUtil {

    private static final String TAG = "OkHttpUtil";
    private static final int IO_TIME_OUT = 30;
    private static final int CONNECT_TIME_OUT = 10;
    private OkHttpClient mOkHttpClient;

    private static final OkHttpUtil mOkHttpUtil = new OkHttpUtil();
    private MaybeEmitter<String> mUrlEmitter;
    private MaybeEmitter<ApkInfo> mApkEmitter;

    public static OkHttpUtil getInstance() {
        return mOkHttpUtil;
    }

    private OkHttpUtil() {
        initClient();
    }

    private void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(IO_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(IO_TIME_OUT, TimeUnit.SECONDS)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
        mOkHttpClient = builder.build();
    }

    private void getLastBuildUrl() {
        Request request = new Request.Builder().url(Constant.GET_BUILD_URL).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.logI(TAG, "getLastBuildUrl onFailure: " + e.getMessage());
                Utils.safeEmitterError(mUrlEmitter, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                String result;
                if (!TextUtils.isEmpty(body)) {
                    int index = body.lastIndexOf(Constant.MATCHER_LAST_BUILD_TAG);
                    int endIndex = 0;
                    if (index > 0) {
                        for (int i = index; i < body.length(); i++) {
                            char c = body.charAt(i);
                            if (c == '<') {
                                endIndex = i;
                                break;
                            }
                        }
                    }
                    if (endIndex > index) {
                        result = body.substring(index, endIndex) + Constant.DOWNLOAD_SUFFIX;
                        LogUtil.logI(TAG, "getLastBuildUrl result: " + result);

                        //先指定测试包地址，到时去掉
                        result = "http://repo.yypm.com/dwbuild/mobile/android/hiyo/hiyo-android_1.3.0_startup_monitor_feature/20180911-3242-r5fb1c1c9a18474fc3fcd8ad1d2fe8f7f919a389b/hiyo.apk";
                        Utils.safeEmitterSuccess(mUrlEmitter, result);
                        return;
                    }
                }
                String msg = "getLastBuildUrl result parse failed";
                LogUtil.logI(TAG, msg);
                Exception e = new Exception(msg);
                Utils.safeEmitterError(mUrlEmitter, e);
            }
        });
    }

    private void downloadApk(final String url) {
        Request request = new Request.Builder().url(url).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.logI(TAG, "downloadApk onFailure: " + e.getMessage());
                Utils.safeEmitterError(mApkEmitter, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ApkInfo apkInfo = ApkInfoUtil.getApkInfo(url);
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len;
                long total = response.body().contentLength();
                apkInfo.size = total;
                LogUtil.logD(TAG, "download apk total: %s", total);
                FileOutputStream outputStream = null;
                String savePath = Utils.getExternalDir();
                int progress = 0;
                try {
                    is = response.body().byteStream();
                    File file = new File(savePath, apkInfo.getApkName());
                    outputStream = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        outputStream.write(buf, 0, len);
                        sum += len;
                        int p = (int) ((sum * 1.0f / total) * 100);
                        if (p % 10 == 0 && p != progress) {
                            progress = p;
                            LogUtil.logD(TAG, "download apk progress: %s", progress);
                        }
                    }
                    outputStream.flush();
                    apkInfo.filePath = file.getPath();
                    Utils.safeEmitterSuccess(mApkEmitter, apkInfo);
                } catch (Exception e) {
                    LogUtil.logE(TAG, "downloadApk exception", e);
                    Utils.safeEmitterError(mApkEmitter, e);
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        });
    }

    /**
     * 获取最新打包的下载地址
     */
    public Maybe<String> getDownloadUrl() {
        LogUtil.logI(TAG, "start getDownloadUrl");
        return Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> e) throws Exception {
                mUrlEmitter = e;
                getLastBuildUrl();
            }
        }).timeout(60, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mUrlEmitter = null;
            }
        });
    }

    public Maybe<ApkInfo> startDownloadApk(final String path) {
        LogUtil.logI(TAG, "start getDownloadApk");
        return Maybe.create(new MaybeOnSubscribe<ApkInfo>() {
            @Override
            public void subscribe(MaybeEmitter<ApkInfo> e) throws Exception {
                mApkEmitter = e;
                downloadApk(path);
            }
        }).timeout(60, TimeUnit.SECONDS).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                mApkEmitter = null;
            }
        });
    }
}
