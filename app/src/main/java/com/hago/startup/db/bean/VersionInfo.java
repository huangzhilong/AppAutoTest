package com.hago.startup.db.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by huangzhilong on 18/9/25.
 */


@DatabaseTable(tableName = "tb_version")
public class VersionInfo {

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(columnName = "branch")
    public String branch; //分支

    @DatabaseField(columnName = "version")
    public long version; //版本号

    @DatabaseField(columnName = "size")
    public long size; //包大小

    @DatabaseField(columnName = "isDebug")
    public boolean isDebug; //当前包是否时debug包,因为release包性能会优于debug，需要区分

    @DatabaseField(columnName = "reserve1")
    public String reserve1; //保留字段未使用

    @DatabaseField(columnName = "reserve2")
    public String reserve2; //保留字段未使用

    @DatabaseField(columnName = "reserve3")
    public String reserve3; //保留字段未使用

    @Override
    public String toString() {
        return "VersionInfo{" +
                "id=" + id +
                ", branch='" + branch + '\'' +
                ", version=" + version +
                ", size=" + size +
                ", isDebug=" + isDebug +
                ", reserve1='" + reserve1 + '\'' +
                ", reserve2='" + reserve2 + '\'' +
                ", reserve3='" + reserve3 + '\'' +
                '}';
    }
}
