package com.hago.startup.widget.chooseApk;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hago.startup.R;

import java.util.List;

/**
 * Created by huangzhilong on 18/9/24.
 */

public class ChooseApkView extends RelativeLayout implements View.OnClickListener, IChooseView {

    private RecyclerView mListRecyclerView;
    private RecyclerView mResultRecyclerView;
    private ChooseViewAdapter mListAdapter;
    private ChooseViewAdapter mResultAdapter;
    private TextView tvCancel, tvOK;
    private ChooseViewPresenter mChooseViewPresenter;

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
        tvOK.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        LinearLayoutManager mListLinearLayoutManager = new LinearLayoutManager(context);
        mListLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListRecyclerView.setLayoutManager(mListLinearLayoutManager);
        mListRecyclerView.setHasFixedSize(true);
        mListAdapter = new ChooseViewAdapter(context, false);
        mListRecyclerView.setAdapter(mListAdapter);
        mListAdapter.setOnItemClick(new onItemClick() {
            @Override
            public void onClick(String data, boolean isVersion) {
                if (!isVersion) {
                    mChooseViewPresenter.getVersionByBranch(data);
                } else {
                    //选择这个版本
                    mResultAdapter.addData(data);
                }
            }
        });

        LinearLayoutManager mResultLinearLayoutManager = new LinearLayoutManager(context);
        mResultLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mResultRecyclerView.setLayoutManager(mResultLinearLayoutManager);
        mResultRecyclerView.setHasFixedSize(true);
        mResultAdapter = new ChooseViewAdapter(context, true);
        mResultRecyclerView.setAdapter(mResultAdapter);
    }

    @Override
    public void updateBranchView(List<String> branch) {
        mListAdapter.setData(branch, false);
    }

    @Override
    public void updateApkVersionView(List<String> version) {
        mListAdapter.setData(version,  true);
    }

    @Override
    public void onClick(View v) {
        if (v == tvOK) {

        } else if (v == tvCancel) {

        }
    }

    public interface onItemClick {
        void onClick(String data, boolean version);
    }
}
