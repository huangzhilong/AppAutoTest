package com.hago.startup.net;

import android.text.TextUtils;

import com.hago.startup.Constant;
import com.hago.startup.bean.ApkInfo;
import com.hago.startup.util.ApkInfoUtil;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Maybe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

/**
 * Created by huangzhilong on 18/9/23.
 */

public class RequestCenter {

    private static final String TAG = "RequestCenter";
    private static final RequestCenter ourInstance = new RequestCenter();

    public static RequestCenter getInstance() {
        return ourInstance;
    }

    private RequestCenter() {
    }

    /**
     * 获取最新构建包的地址
     * @return
     */
    public Maybe<String> getNewestApkUrl() {
        return OkHttpUtil.getInstance().execRequest(Constant.GET_BUILD_URL)
                .map(new Function<Response, String>() {
                    @Override
                    public String apply(@NonNull Response response) throws Exception {
                        String url = getNewestUrlByResponse(response);
                        LogUtil.logI(TAG, "getNewestApkUrl : %s", url);
                        return url;
                    }
                });
    }

    private String getNewestUrlByResponse(Response response) {
        try {
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
                    //先指定测试包地址，到时去掉
                    result = "http://repo.yypm.com/dwbuild/mobile/android/hiyo/hiyo-android_1.3.0_startup_monitor_feature/20180922-3482-rd8b11182cfbebe783ec5aa2e05b7f12498276866/hiyo.apk";
                    return result;
                }
            }
            return Constant.EMPTYSTR;
        } catch (Exception e) {
            LogUtil.logI(TAG, "getNewestUrlByResponse ex: %s", e);
            return Constant.EMPTYSTR;
        }
    }

    /**
     * 下载apk
     * @param url
     * @return
     */
    public Maybe<ApkInfo> startDownloadApk(String url) {
        return OkHttpUtil.getInstance().execRequest(url)
                .subscribeOn(Schedulers.io())
                .map(new Function<Response, ApkInfo>() {
                    @Override
                    public ApkInfo apply(Response response) throws Exception {
                        String url = response.request().url().toString();
                        ApkInfo apkInfo = ApkInfoUtil.getApkInfo(url);
                        LogUtil.logI(TAG, "downloadApk apkInfo: %s", apkInfo);
                        saveApk(response, apkInfo);
                        return apkInfo;
                    }
                });
    }

    private void saveApk(Response response, ApkInfo apkInfo) {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len;
        long total = response.body().contentLength();
        apkInfo.size = total;
        LogUtil.logD(TAG, "download apk total: %s", total);
        FileOutputStream outputStream = null;
        int progress = 0;
        try {
            String savePath = Utils.getExternalDir();
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
        } catch (Exception e) {
            LogUtil.logE(TAG, "downloadApk exception", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
