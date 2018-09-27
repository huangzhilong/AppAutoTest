package com.hago.startup.widget.result;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hago.startup.R;

/**
 * Created by huangzhilong on 18/9/27.
 */

public class ResultViewHolder extends RecyclerView.ViewHolder {

    public TextView tvBranch;

    public TextView tvVersion;

    public LinearLayout llContainer;

    public ResultViewHolder(View itemView) {
        super(itemView);
        tvBranch = itemView.findViewById(R.id.tv_branch);
        tvVersion = itemView.findViewById(R.id.tv_version);
        llContainer = itemView.findViewById(R.id.ll_container);
    }

}
