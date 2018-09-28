package com.hago.startup.net;

import android.text.TextUtils;

import com.hago.startup.notify.RxJavaUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    private static final int IO_TIME_OUT = 30;
    private static final int CONNECT_TIME_OUT = 10;
    private OkHttpClient mOkHttpClient;

    private static final OkHttpUtil mOkHttpUtil = new OkHttpUtil();

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

    private Map<String, MaybeEmitter> mEmitterMap = new ConcurrentHashMap<>();

    /**
     * 执行请求
     * @param url
     * @return
     */
    public Maybe<Response> execRequest(final String url) {
        return Maybe.create(new MaybeOnSubscribe<Response>() {
            @Override
            public void subscribe(MaybeEmitter<Response> e) throws Exception {
                if (TextUtils.isEmpty(url)) {
                    RxJavaUtil.safeEmitterError(e, new Exception("url is empty"));
                    return;
                }
                mEmitterMap.put(url, e);
                Request request = new Request.Builder().url(url).build();
                Call call = mOkHttpClient.newCall(request);
                call.enqueue(mResponseCallback);
            }
        }).doFinally(new Action() {
            @Override
            public void run() throws Exception {
                releaseEmitters();
            }
        }).timeout(60, TimeUnit.SECONDS);
    }

    private void releaseEmitters() {
        if (mEmitterMap.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, MaybeEmitter>> iterator = mEmitterMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MaybeEmitter> entry = iterator.next();
            if (entry.getValue() == null || entry.getValue().isDisposed()){
                iterator.remove();
            }
        }
    }

    private Callback mResponseCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String url = call.request().url().toString();
            RxJavaUtil.safeEmitterError(mEmitterMap.get(url), e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String url = call.request().url().toString();
            RxJavaUtil.safeEmitterSuccess(mEmitterMap.get(url), response);
        }
    };
}
