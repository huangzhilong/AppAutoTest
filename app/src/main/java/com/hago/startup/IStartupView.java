package com.hago.startup;

import android.text.SpannableString;

/**
 * Created by huangzhilong on 18/9/13.
 */

public interface IStartupView {

    void updateAccessibilityStateView(boolean open, SpannableString spannableString);

    void updateStepView(String text);
}
