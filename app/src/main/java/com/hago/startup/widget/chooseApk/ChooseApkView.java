package com.hago.startup.widget.chooseApk;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hago.startup.Constant;
import com.hago.startup.R;
import com.hago.startup.bean.ApkInfo;
import com.hago.startup.util.ApkInfoUtil;
import com.hago.startup.util.Utils;
import com.hago.startup.widget.DialogManager;

import java.util.Iterator;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/24.
 */

public class ChooseApkView extends RelativeLayout implements View.OnClickListener, IChooseView {

    private RecyclerView mListRecyclerView;
    private RecyclerView mResultRecyclerView;
    private ChooseListAdapter mListAdapter;
    private ChooseResultAdapter mResultAdapter;
    private TextView tvCancel, tvOK;
    private TextView tvText;
    private ChooseViewPresenter mChooseViewPresenter;

    private DialogManager.ChooseDialogListener mListener;

    public void setListener(DialogManager.ChooseDialogListener listener) {
        mListener = listener;
    }

    public ChooseApkView(Context context) {
        this(context, null);
    }

    public ChooseApkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        mChooseViewPresenter = new ChooseViewPresenter(this, context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_choose_apk, this);
        mListRecyclerView = findViewById(R.id.rcy_list);
        mResultRecyclerView = findViewById(R.id.rcy_select);
        tvCancel = findViewById(R.id.btn_cancel);
        tvOK = findViewById(R.id.btn_ok);
        tvText= findViewById(R.id.tv_text);
        tvOK.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        LinearLayoutManager mListLinearLayoutManager = new LinearLayoutManager(context);
        mListLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListRecyclerView.setLayoutManager(mListLinearLayoutManager);
        mListRecyclerView.setHasFixedSize(true);
        mListAdapter = new ChooseListAdapter(context);
        mListRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setOnItemClick(new onItemClick() {
            @Override
            public void onClick(String data, boolean isVersion) {
                if (!isVersion) {
                    mChooseViewPresenter.getVersionByBranch(data);
                } else {
                    if (ChooseListAdapter.BACK.equals(data)) {
                        mChooseViewPresenter.backBranchList();
                        return;
                    }
                    //选择这个版本
                    String branch = mChooseViewPresenter.getCurBranch();
                    mResultAdapter.addData(branch, data);
                }
            }
        });

        LinearLayoutManager mResultLinearLayoutManager = new LinearLayoutManager(context);
        mResultLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mResultRecyclerView.setLayoutManager(mResultLinearLayoutManager);
        mResultRecyclerView.setHasFixedSize(true);
        mResultAdapter = new ChooseResultAdapter(context);
        mResultRecyclerView.setAdapter(mResultAdapter);
    }

    @Override
    public void updateBranchView(List<String> branch) {
        tvText.setText("请选择分支");
        mListAdapter.setData(branch, false);
    }

    @Override
    public void updateApkVersionView(List<String> version) {
        tvText.setText("请选择版本号");
        mListAdapter.setData(version,  true);
    }

    @Override
    public void onClick(View v) {
        if (mListener == null) {
            return;
        }
        if (v == tvOK) {
            List<ApkInfo> results = mResultAdapter.getData();
            if (Utils.empty(results)) {
                Toast.makeText(getContext(), "选择不能为空!", Toast.LENGTH_SHORT).show();
                return;
            }
            //处理选择的结果
            Iterator<ApkInfo> iterator = results.iterator();
            while (iterator.hasNext()) {
                ApkInfo apkInfo = iterator.next();
                //生成下载地址
                apkInfo.filePath = Constant.MATCHER_LAST_BUILD_TAG + apkInfo.branch + "/" + apkInfo.version + "/" + Constant.DOWNLOAD_SUFFIX;
                //提取真正的版本号
                apkInfo.version = ApkInfoUtil.getApkVersion(apkInfo.version);
                if (!apkInfo.checkAvailability()) {
                    iterator.remove();
                }
            }
            mListener.ok(results);
        } else if (v == tvCancel) {
            mListener.onCancel();
        }
    }

    public interface onItemClick {
        void onClick(String data, boolean version);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mChooseViewPresenter.release();
    }
}
