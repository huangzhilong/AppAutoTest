package com.hago.startup;

import android.text.SpannableString;

import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.widget.DialogManager;

import java.util.List;

/**
 * Created by huangzhilong on 18/9/13.
 */

public interface IStartupView {

    void updateAccessibilityStateView(boolean open, SpannableString spannableString);

    void updateStepView(String text);

    void updateApkView(String text);

    void showOpenAccessibilityTipDialog();

    void showChooseApkVersionDialog(DialogManager.ChooseDialogListener listener);

    void showResultViewDialog(List<ResultInfo> resultInfoList, boolean target);
}
