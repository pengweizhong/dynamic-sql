package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class ContextApplication {
    private static final Log log = LogFactory.getLog(ContextApplication.class);
    private static final Map<String, Map<String, List<TableInfo>>> dataBaseMap = new ConcurrentHashMap<>();
    private static final Map<String, DataSourceConfig> dataSourcesMap = new ConcurrentHashMap<>();

    public static DataSourceConfig getDefalutDataSource() {
        Collection<DataSourceConfig> values = dataSourcesMap.values();
        for (DataSourceConfig dataSourceConfig : values) {
            if (dataSourceConfig.defaultDataSource()) {
                return dataSourceConfig;
            }
        }
        return null;
    }

    public static void putDataSource(Class<?> dataSourceClass)  {
        DataSourceConfig sourceConfig = null;
        try {
            sourceConfig = (DataSourceConfig) dataSourceClass.newInstance();
        } catch (Exception e) {
            throw new BraveException(dataSourceClass.toString() + " 必须实现或继承 " + DataSourceConfig.class + " 类");
        }
        if (sourceConfig.defaultDataSource()) {
            Collection<DataSourceConfig> dataSources = dataSourcesMap.values();
            List<DataSourceConfig> collect = dataSources.stream().filter(data -> data.defaultDataSource()).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                throw new BraveException("仅支持一个默认数据源，愈配置默认数据源：" + dataSourceClass + "，已存在的默认数据源：" + collect);
            }
        }
        String dataSourceName = dataSourceClass.toString();
        if (dataSourcesMap.containsKey(dataSourceName)) {
            throw new BraveException("数据源：" + dataSourceName + "已经存在");
        }
        dataSourcesMap.put(dataSourceName, sourceConfig);
    }

    public static DataSourceConfig getDataSource(Class<?> dataSourceClass) {
        DataSourceConfig dataSourceConfig = dataSourcesMap.get(dataSourceClass.toString());
        if(Objects.nonNull(dataSourceConfig)){
            return dataSourceConfig;
        }
        putDataSource(dataSourceClass);
        return dataSourcesMap.get(dataSourceClass.toString());
    }

    public static boolean existsTable(String tableName, Class<?> dataSourceClass) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (Objects.nonNull(tableMap)) {
            List<TableInfo> tableInfos = tableMap.get(tableName);
            if (Objects.nonNull(tableInfos)) {
                return true;
            }
        }
        return false;
    }

    public static String formatAllColumToStr(Class<?> dataSourceClass, String tableName) {
        List<String> columList = getAllColumnList(dataSourceClass, tableName);
        return String.join(",", columList);
    }

    public static List<String> getAllColumnList(Class<?> dataSourceClass, String tableName) {
        List<String> members = new ArrayList<>();
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (tableMap == null) {
            return members;
        }
        List<TableInfo> tableInfos = tableMap.get(tableName);
        if (tableInfos == null) {
            return members;
        }
        tableInfos.forEach(tableInfo -> {
            members.add(tableInfo.getColumn());
        });
        return members;
    }

    public static List<TableInfo> getTableInfos(Class<?> dataSourceClass, String tableName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (tableMap != null) {
            return tableMap.get(tableName);
        }
        throw new BraveException("无法匹配数据源：" + dataSourceClass + "或表：" + tableName);
    }

    public static String getColumnByField(Class<?> dataSourceClass, String tableName, String fieldName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getField().getName().equals(fieldName)) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableName + "中未识别的字段：" + fieldName);
    }

    public static String getPrimaryKey(Class<?> dataSourceClass, String tableName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.isPrimary()) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableName + "中未获取到主键字段");
    }

    public static TableInfo getTableInfoPrimaryKey(Class<?> dataSourceClass, String tableName) {
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.isPrimary()) {
                return tableInfo;
            }
        }
        throw new BraveException(tableName + "中未获取到主键信息");
    }

    public static Map<String, Map<String, List<TableInfo>>> getAll() {
        return dataBaseMap;
    }

    public static void saveTable(Class<?> dataSourceClass, String tableName, List<TableInfo> tableInfos) {
        if (StringUtils.isEmpty(tableName)) {
            throw new BraveException("待保存的表名不可为空");
        }
        if (CollectionUtils.isEmpty(tableInfos)) {
            throw new BraveException("待保存的表字段不可为空");
        }
        String dataSource = dataSourceClass.getName();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSource);
        if (tableMap == null) {
            tableMap = new ConcurrentHashMap<>();
            dataBaseMap.put(dataSource, tableMap);
        }
        tableMap.put(tableName, tableInfos);
    }

    public static void clear() {
        dataBaseMap.clear();
    }

}
