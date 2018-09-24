package com.hago.startup.widget.chooseApk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hago.startup.R;
import com.hago.startup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/24.
 */

public class ChooseViewAdapter extends RecyclerView.Adapter<ChooseViewAdapter.ChooseHolder>{

    private List<String> data = new ArrayList<>();
    private boolean isResult;
    private boolean isVersion;
    private Context mContext;

    private LayoutInflater mInflater;

    private ChooseApkView.onItemClick mOnItemClick;

    public void setOnItemClick(ChooseApkView.onItemClick onItemClick) {
        mOnItemClick = onItemClick;
    }

    public ChooseViewAdapter(Context context, boolean isResult) {
        mContext = context;
        this.isResult = isResult;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<String> list, boolean version) {
        data.clear();
        if (!Utils.empty(list)) {
            data.addAll(list);
        }
        isVersion = version;
        notifyDataSetChanged();
    }

    public void addData(String branch) {
        if (!data.contains(branch)) {
            data.add(branch);
            notifyDataSetChanged();
        }
    }

    public List<String> getData() {
        return data;
    }

    @NonNull
    @Override
    public ChooseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_choose_view, parent, false);
        ChooseHolder holder = new ChooseHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClick != null && v.getTag() != null && v.getTag() instanceof String) {
                    String tag = (String) v.getTag();
                    mOnItemClick.onClick(tag, isVersion);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChooseHolder holder, final int position) {
        String text = data.get(position);
        holder.tvText.setText(text);
        holder.itemView.setTag(text);
        if (isResult) {
            holder.tvDelete.setVisibility(View.VISIBLE);
            holder.tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    data.remove(position);
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.tvDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ChooseHolder extends RecyclerView.ViewHolder {

        public TextView tvText;

        public TextView tvDelete;

        public ChooseHolder(View itemView) {
            super(itemView);
            tvDelete = itemView.findViewById(R.id.tv_delete);
            tvText = itemView.findViewById(R.id.tv_text);
        }
    }
}
