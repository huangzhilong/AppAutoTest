package com.hago.startup.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import com.hago.startup.util.LogUtil;
import com.hago.startup.util.Utils;

import java.util.List;

/**
 * Created by huangzhilong on 18/9/10.
 */

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = "MyAccessibilityService";
    private static final String NEXT = "下一步";
    private static final String OK = "确认";
    private static final String OK1 = "确定";
    private static final String DONE = "完成";
    private static final String INSTALL = "安装";
    private static final String AGREE = "同意";

    //处理删除安装包弹窗
    private static final String DELETE = "立即删除";
    private static final String UNINSTALL = "卸载";

    private static final String ALLOW = "允许";
    private static final String ALWAYS_ALLOW = "始终允许";
    private static final String ALWAYS_ALLOW1 = "总是允许";

    //处理系统的安装卸载
    private String[] operation = {NEXT, OK, OK1, DONE, INSTALL, AGREE, DELETE, UNINSTALL, ALLOW, ALWAYS_ALLOW, ALWAYS_ALLOW1};

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        LogUtil.logD(TAG, "onAccessibilityEvent eventType: " + eventType);
        if ((eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
                && (event.getPackageName().equals("com.android.packageinstaller") || event.getPackageName().equals("com.yy.hiyo"))) {
            handlerInstant();
        }
    }

    @Override
    public void onInterrupt() {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handlerInstant() {
        LogUtil.logD(TAG, "handlerInstant");
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> apkName = nodeInfo.findAccessibilityNodeInfosByText("Hago");
        List<AccessibilityNodeInfo> apkName1 = nodeInfo.findAccessibilityNodeInfosByText("hago");
        if (Utils.empty(apkName) && Utils.empty(apkName1)) {
            //避免所有app安装都自动
            return;
        }

        for (int i = 0; i < operation.length; i++) {
            List<AccessibilityNodeInfo> nodeList = nodeInfo.findAccessibilityNodeInfosByText(operation[i]);
            if (nodeList != null && nodeList.size() > 0) {
                for (int j = 0; j < nodeList.size(); j++) {
                    AccessibilityNodeInfo node = nodeList.get(j);
                    LogUtil.logD(TAG, "handlerInstant className " + node.getClassName() + "  " + operation[i]);
                    if (node != null && node.isEnabled() && node.isClickable()) {
                        LogUtil.logI(TAG, "handlerInstant click " + operation[i]);
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }
    }
}
