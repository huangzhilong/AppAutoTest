package com.hago.startup.widget.chooseApk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hago.startup.R;
import com.hago.startup.bean.ApkInfo;
import com.hago.startup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/24.
 */

public class ChooseResultAdapter extends RecyclerView.Adapter<ChooseHolder> {

    private LayoutInflater mInflater;
    private List<ApkInfo> mData = new ArrayList<>();

    public ChooseResultAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<ApkInfo> apkInfoList) {
        mData.clear();
        if (!Utils.empty(apkInfoList)) {
            mData.addAll(apkInfoList);
        }
        notifyDataSetChanged();
    }

    public void addData(String branch, String version) {
        for (int i = 0; i < mData.size(); i++) {
            ApkInfo info = mData.get(i);
            if (info != null && branch.equals(info.branch) && version.equals(info.version)) {
                return;
            }
        }
        ApkInfo info = new ApkInfo();
        info.version = version;
        info.branch = branch;
        mData.add(info);
        notifyDataSetChanged();
    }

    public List<ApkInfo> getData() {
        return mData;
    }

    @NonNull
    @Override
    public ChooseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_choose_view, parent, false);
        ChooseHolder holder = new ChooseHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseHolder holder, final int position) {
        ApkInfo info = mData.get(position);
        if (info != null) {
            holder.tvText.setText(info.version);
            holder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mData.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }
}
