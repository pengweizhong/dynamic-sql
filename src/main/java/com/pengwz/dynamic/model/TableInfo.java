package com.pengwz.dynamic.model;

import java.util.List;

public class TableInfo {
    //所属数据源名称
    private String dataSourceName;
    //表名
    private String tableName;
    //列集合
    private List<TableColumnInfo> tableColumnInfos;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TableColumnInfo> getTableColumnInfos() {
        return tableColumnInfos;
    }

    public void setTableColumnInfos(List<TableColumnInfo> tableColumnInfos) {
        this.tableColumnInfos = tableColumnInfos;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "dataSourceName='" + dataSourceName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", tableColumnInfos=" + tableColumnInfos +
                '}';
    }
}
