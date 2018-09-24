package com.hago.startup.widget.chooseApk;

import java.util.List;

/**
 * Created by huangzhilong on 18/9/24.
 */

public interface IChooseView {

    void updateBranchView(List<String> branch);

    void updateApkVersionView(List<String> version);
}
