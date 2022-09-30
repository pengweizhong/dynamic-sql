package com.pengwz.dynamic.model;

import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.StringUtils;

import java.util.List;

public class TableInfo {
    //所属数据源名称
    private String dataSourceName;
    //表名
    private String tableName;
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

    public TableColumnInfo getTableColumnInfoByFieldName(String fieldName) {
        if (CollectionUtils.isEmpty(tableColumnInfos) || StringUtils.isEmpty(fieldName)) {
            return null;
        }
        return tableColumnInfos.stream().filter(ci -> ci.getField().getName().equalsIgnoreCase(fieldName)).findFirst().orElse(null);
    }

    public void setTableColumnInfos(List<TableColumnInfo> tableColumnInfos) {
        this.tableColumnInfos = tableColumnInfos;
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
                ", isCache=" + isCache +
                ", tableColumnInfos=" + tableColumnInfos +
                '}';
    }


}
