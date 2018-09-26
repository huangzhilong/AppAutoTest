package com.hago.startup.util;

import com.hago.startup.Constant;
import com.hago.startup.db.bean.MonitorInfo;
import com.hago.startup.db.bean.ResultInfo;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by huangzhilong on 18/9/20.
 * 用于生成mail表格的html,注意：测试发现生成的str含null时发送失败
 */

public class TableUtil {

    private static final String TAG = "TableUtil";
    private final static String TABLE_START = "<table border=\"1\">";
    private final static String TABLE_END = "</table>";
    private final static String TABLE_TR_START = "<tr>";
    private final static String TABLE_TR_END = "</tr>";
    private final static String TABLE_TH_START = "<th align=\"left\">";
    private final static String TABLE_TH_END = "</th>";
    private final static String TABLE_TD_START = "<td>";
    private final static String TABLE_TD_END = "</td>";
    private final static String TABLE_NEW_LINE = "<br>";
    private final static String DATA = "MonitorData";

    private final static String[] mFieldsOrder = {"version", "isDebug", DATA, "branch", "size"};

    public static String createMailTableText(List<ResultInfo> result) {
        if (Utils.empty(result)) {
            LogUtil.logI(TAG, "createMailTableText result is empty");
            return "result is empty";
        }
        StringBuffer stringBuffer = new StringBuffer();
        //说明
        stringBuffer.append(addInstruction());
        stringBuffer.append(TABLE_START);
        //表头
        stringBuffer.append(createTableHead());
        //数据
        stringBuffer.append(createTableData(result));
        stringBuffer.append(TABLE_END);
        return stringBuffer.toString();
    }

    private static String createTableHead() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(TABLE_TR_START);
        for (int i = 0; i < mFieldsOrder.length; i++) {
            if (DATA.equals(mFieldsOrder[i])) {
                for (int j = 1; j < Constant.START_COUNT; j++) {
                    stringBuffer.append(TABLE_TH_START);
                    stringBuffer.append(getTitle(mFieldsOrder[i]) + j);
                    stringBuffer.append(TABLE_TH_END);
                }
            } else {
                stringBuffer.append(TABLE_TH_START);
                stringBuffer.append(getTitle(mFieldsOrder[i]));
                stringBuffer.append(TABLE_TH_END);
            }
        }
        stringBuffer.append(TABLE_TR_END);
        return stringBuffer.toString();
    }

    private static String getTitle(String column) {
        if (column.equals("version")) {
            return "版本号";
        }
        if (column.equals(DATA)) {
            return "测试结果";
        }
        if (column.equals("branch")) {
            return "分支";
        }
        if (column.equals("size")) {
            return "安装包大小";
        }
        if (column.equals("isDebug")) {
            return "测试包";
        }
        return column;
    }

    private static String createTableData(List<ResultInfo> data) {
        Field[] fields = MonitorInfo.class.getDeclaredFields();
        if (fields == null || fields.length == 0) {
            LogUtil.logI(TAG, "createMailTableText getDeclaredFields is empty");
            return "getDeclaredFields is empty";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < data.size(); i++) {
            stringBuffer.append(TABLE_TR_START);
            ResultInfo info = data.get(i);
            if (info == null || info == ResultInfo.RESULT_EMPTY || info.mVersionInfo == null || Utils.empty(info.mMonitorInfoList)) {
                continue;
            }
            //版本
            stringBuffer.append(TABLE_TD_START);
            stringBuffer.append(info.mVersionInfo.version);
            stringBuffer.append(TABLE_TD_END);
            //是否是测试包
            stringBuffer.append(TABLE_TD_START);
            stringBuffer.append(info.mVersionInfo.isDebug ? "是" : "否");
            stringBuffer.append(TABLE_TD_END);

            //测试结果
            for (int j = 0; j < Constant.START_COUNT - 1; j++) {
                stringBuffer.append(TABLE_TD_START);
                MonitorInfo monitorInfo = safeGetMonitor(info.mMonitorInfoList, j);
                StringBuilder monitorBuilder = new StringBuilder();
                monitorBuilder.append("totalTime: ");
                monitorBuilder.append(monitorInfo == null ? 0 : monitorInfo.totalTime);
                monitorBuilder.append(TABLE_NEW_LINE);
                monitorBuilder.append("startTime: ");
                monitorBuilder.append(monitorInfo == null ? 0 : monitorInfo.startTime);
                monitorBuilder.append(TABLE_NEW_LINE);
                monitorBuilder.append("startMemory: ");
                monitorBuilder.append(monitorInfo == null ? 0 : monitorInfo.startupMemory);
                monitorBuilder.append(TABLE_NEW_LINE);
                monitorBuilder.append("time: ");
                long timestamp = monitorInfo == null ? 0 : monitorInfo.timestamp;
                if(timestamp == 0) {
                    monitorBuilder.append(timestamp);
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    monitorBuilder.append(dateFormat.format(new Date(timestamp)));
                }
                String str = monitorBuilder.toString();
                stringBuffer.append(str);
                stringBuffer.append(TABLE_TD_END);
            }

            //分支
            stringBuffer.append(TABLE_TD_START);
            stringBuffer.append(info.mVersionInfo.branch);
            stringBuffer.append(TABLE_TD_END);

            //包大小
            stringBuffer.append(TABLE_TD_START);
            float size= info.mVersionInfo.size / (1024 * 1024.0f);
            DecimalFormat format = new DecimalFormat("##0.00");
            stringBuffer.append(format.format(size) + "M");
            stringBuffer.append(TABLE_TD_END);

            stringBuffer.append(TABLE_TR_END);
        }
        return stringBuffer.toString();
    }

    private static MonitorInfo safeGetMonitor(List<MonitorInfo> list, int index) {
        if (Utils.empty(list) || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    private static String addInstruction() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h2>统计方式</h2>");
        stringBuilder.append("用adb启动hago获取启动时间。自动登入进入首页，代码计算获取启动时间，等待10秒获取当前app内存，结束app并返回app启动时间和内存。并去除刚安装第一次app启动结果！");
        stringBuilder.append("<h2>参数</h2>");
        stringBuilder.append("totalTime: ");
        stringBuilder.append("adb启动时间");
        stringBuilder.append(TABLE_NEW_LINE);
        stringBuilder.append("startTime: ");
        stringBuilder.append("app计算的启动时间");
        stringBuilder.append(TABLE_NEW_LINE);
        stringBuilder.append("startMemory: ");
        stringBuilder.append("app启动内存");
        stringBuilder.append(TABLE_NEW_LINE);
        stringBuilder.append("time: ");
        stringBuilder.append("测试时间");
        stringBuilder.append(TABLE_NEW_LINE);
        stringBuilder.append(TABLE_NEW_LINE);
        stringBuilder.append(TABLE_NEW_LINE);
        return stringBuilder.toString();
    }
}
