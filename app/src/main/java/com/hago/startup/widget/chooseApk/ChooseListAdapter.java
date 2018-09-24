package com.hago.startup.widget.chooseApk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hago.startup.R;
import com.hago.startup.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/24.
 */

public class ChooseListAdapter extends RecyclerView.Adapter<ChooseHolder>{

    private List<String> data = new ArrayList<>();

    private boolean isVersion;
    private LayoutInflater mInflater;

    private ChooseApkView.onItemClick mOnItemClick;
    public final static String BACK = "返回上一级";

    public void setOnItemClick(ChooseApkView.onItemClick onItemClick) {
        mOnItemClick = onItemClick;
    }

    public ChooseListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<String> list, boolean version) {
        data.clear();
        if (!Utils.empty(list)) {
            if (version) {
                data.add(BACK);
            }
            data.addAll(list);
        }
        isVersion = version;
        notifyDataSetChanged();
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
        if (holder.tvDelete.getVisibility() != View.GONE) {
            holder.tvDelete.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

}
