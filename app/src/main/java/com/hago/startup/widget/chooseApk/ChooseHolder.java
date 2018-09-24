package com.hago.startup.widget.chooseApk;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.hago.startup.R;

/**
 * Created by huangzhilong on 18/9/24.
 */
public class ChooseHolder extends RecyclerView.ViewHolder {

    public TextView tvText;

    public TextView tvDelete;

    public ChooseHolder(View itemView) {
        super(itemView);
        tvDelete = itemView.findViewById(R.id.tv_delete);
        tvText = itemView.findViewById(R.id.tv_text);
    }
}
