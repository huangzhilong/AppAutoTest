package com.hago.startup.util;

import com.hago.startup.db.MonitorInfo;

import java.lang.reflect.Field;
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
    private final static String TABLE_TH_START = "<th>";
    private final static String TABLE_TH_END = "</th>";
    private final static String TABLE_TD_START = "<td>";
    private final static String TABLE_TD_END = "</td>";

    private final static String[] mFieldsOrder = {
            "id", "branch", "version", "size", "thisTime", "totalTime", "waitTime", "startTime", "startupMemory",
            "timestamp"
    };

    String text = "<table border=\"1\"> <tr><th>Month</th><th>Savings</th></tr><tr><td>January</td><td>$100</td></tr></table>";

    public static String createMailTableText(List<MonitorInfo> result) {
        if (Utils.empty(result)) {
            LogUtil.logI(TAG, "createMailTableText result is empty");
            return "result is empty";
        }
        StringBuffer stringBuffer = new StringBuffer();
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
            stringBuffer.append(TABLE_TH_START);
            stringBuffer.append(mFieldsOrder[i]);
            stringBuffer.append(TABLE_TH_END);
        }
        stringBuffer.append(TABLE_TR_END);
        return stringBuffer.toString();
    }

    private static String createTableData(List<MonitorInfo> data) {
        Field[] fields = MonitorInfo.class.getDeclaredFields();
        if (fields == null || fields.length == 0) {
            LogUtil.logI(TAG, "createMailTableText getDeclaredFields is empty");
            return "getDeclaredFields is empty";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < data.size(); i++) {
            stringBuffer.append(TABLE_TR_START);
            MonitorInfo info = data.get(i);
            if (info == null) {
                continue;
            }
            for (int j = 0; j < mFieldsOrder.length; j++) {
                try {
                    Field field = info.getClass().getField(mFieldsOrder[j]);
                    stringBuffer.append(TABLE_TD_START);
                    if (mFieldsOrder[j].equals("timestamp") && field.get(info) instanceof Long) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        stringBuffer.append(dateFormat.format(new Date((Long) field.get(info))));
                    } else {
                        stringBuffer.append(field.get(info));
                    }
                    stringBuffer.append(TABLE_TD_END);
                } catch (NoSuchFieldException e) {
                    LogUtil.logI(TAG, "getField NoSuchField: %s", mFieldsOrder[j]);
                } catch (IllegalAccessException e) {
                    LogUtil.logI(TAG, "getField IllegalAccess: %s", mFieldsOrder[j]);
                } catch (Exception e) {
                    LogUtil.logI(TAG, "getField Exception: %s  ex: %s", mFieldsOrder[j], e);
                }
            }
            stringBuffer.append(TABLE_TR_END);
        }
        return stringBuffer.toString();
    }
}
