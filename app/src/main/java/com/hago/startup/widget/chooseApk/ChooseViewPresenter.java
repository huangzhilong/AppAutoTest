package com.hago.startup.widget.chooseApk;

import android.content.Context;
import android.widget.Toast;

import com.hago.startup.net.RequestCenter;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by huangzhilong on 18/9/24.
 */

public class ChooseViewPresenter {

    private static final String TAG = "ChooseViewPresenter";
    private IChooseView mIChooseView;
    private Context mContext;

    private List<String> branch;
    private String mCurBranch;
    private HashMap<String, List<String>> mData = new HashMap<>();
    private Disposable mBranchDisposable, mVersionDisposable;

    public ChooseViewPresenter(IChooseView IChooseView, Context context) {
        mIChooseView = IChooseView;
        mContext = context;
        initData();
    }

    private void initData() {
        mBranchDisposable = RequestCenter.getInstance().getAllBranchByPage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(@NonNull List<String> list) throws Exception {
                        branch = list;
                        mIChooseView.updateBranchView(branch);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logE(TAG, "getAllBranch failed: %s", throwable);
                        Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void backBranchList() {
        mIChooseView.updateBranchView(branch);
    }

    public String getCurBranch() {
        return mCurBranch;
    }

    public void getVersionByBranch(String branch) {
        mCurBranch = branch;
        List<String> result = mData.get(branch);
        if (!Utils.empty(result)) {
            LogUtil.logD(TAG, "getVersionByBranch by cache branch: %s", mCurBranch);
            mIChooseView.updateApkVersionView(result);
            return;
        }
        mVersionDisposable = RequestCenter.getInstance().getAllPackageByBranch(branch)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(@NonNull List<String> list) throws Exception {
                        mData.put(mCurBranch, list);
                        mIChooseView.updateApkVersionView(list);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logE(TAG, "getVersionByBranch branch: %s failed: %s", mCurBranch, throwable);
                        Toast.makeText(mContext, "获取数据失败", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void release() {
        if (mBranchDisposable != null && !mBranchDisposable.isDisposed()) {
            mBranchDisposable.dispose();
        }
        if (mVersionDisposable != null && !mVersionDisposable.isDisposed()) {
            mVersionDisposable.dispose();
        }
    }
}
