package com.pengwz.dynamic.model;

import java.util.List;

public class TableInfo {
    //所属数据源名称
    private String dataSourceName;
    //表名
    private String tableName;
    //表类型
    private String viewType;
    //是否允许缓存
    private boolean isCache;
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

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public boolean isCache() {
        return isCache;
    }

    public void setCache(boolean cache) {
        isCache = cache;
    }

    @Override
    public String toString() {
        return "TableInfo{" +
                "dataSourceName='" + dataSourceName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", viewType='" + viewType + '\'' +
                ", isCache=" + isCache +
                ", tableColumnInfos=" + tableColumnInfos +
                '}';
    }
}
