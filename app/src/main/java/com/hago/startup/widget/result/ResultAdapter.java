package com.hago.startup.widget.result;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hago.startup.R;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.db.bean.VersionInfo;
import com.hago.startup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/27.
 */

public class ResultAdapter extends RecyclerView.Adapter<ResultViewHolder> {

    private List<ResultInfo> mData;

    private LayoutInflater mInflater;

    public ResultAdapter(Context context) {
        mData = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<ResultInfo> list) {
        mData.clear();
        if (!Utils.empty(list)) {
            mData.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_result_view, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        ResultInfo resultInfo = mData.get(position);
        if (resultInfo != null) {
            VersionInfo versionInfo = resultInfo.mVersionInfo;
            if (versionInfo != null) {
                holder.tvVersion.setText("版本: " + versionInfo.version);
                holder.tvBranch.setText("分支: " +versionInfo.branch);
            }
            List<MonitorInfo> monitorInfoList = resultInfo.mMonitorInfoList;
            holder.llContainer.removeAllViews();
            if (!Utils.empty(monitorInfoList)) {
                for (int i = 0; i < monitorInfoList.size(); i++) {
                    MonitorInfo info = monitorInfoList.get(i);
                    if (info == null) {
                        continue;
                    }
                    View view = mInflater.inflate(R.layout.item_result_detail, holder.llContainer, false);
                    TextView tv1 = view.findViewById(R.id.tv_1);
                    TextView tv2 = view.findViewById(R.id.tv_2);
                    TextView tv3 = view.findViewById(R.id.tv_3);
                    tv1.setText(String.valueOf(info.totalTime));
                    tv2.setText(String.valueOf(info.startTime));
                    tv3.setText(String.valueOf(info.startupMemory));
                    holder.llContainer.addView(view);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        int size = Utils.getCollectionSize(mData);
        return size;
    }
}
