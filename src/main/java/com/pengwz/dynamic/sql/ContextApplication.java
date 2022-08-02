package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.utils.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class ContextApplication {
    private static final Log log = LogFactory.getLog(ContextApplication.class);
    //key = tableClass   value = TableInfos
    private static final Map<Class<?>, List<TableInfo>> tableDataBaseMap = new ConcurrentHashMap<>();
    //key = DataSource.class.name
    private static final Map<String, DataSourceInfo> dataSourcesMap = new ConcurrentHashMap<>();

    public static String getDefalutDataSourceName() {
        for (DataSourceInfo sourceInfo : dataSourcesMap.values()) {
            if (sourceInfo.isDefault()) {
                return sourceInfo.getClassPath();
            }
        }
        return null;
    }

    public static DataSource getDataSource(String dataSourceName) {
        DataSourceInfo dataSourceInfo = dataSourcesMap.get(dataSourceName);
        if (Objects.isNull(dataSourceInfo)) {
            return null;
        }
        return dataSourceInfo.getDataSource();
    }

    public static DataSourceInfo getDataSourceInfo(String dataSourceName) {
        if (null == dataSourceName) {
            throw new BraveException("未指定数据源");
        }
        return dataSourcesMap.get(dataSourceName);
    }

    public static List<DataSourceInfo> getAllDataSourceInfo() {
        return new ArrayList<>(dataSourcesMap.values());
    }

    /**
     * 检查该数据源是否存在，如果存在则返回true
     */
    public static synchronized boolean existsDataSouce(String dataSourceName) {
        return dataSourcesMap.get(dataSourceName) != null;
    }

    public static synchronized void putDataSource(DataSourceInfo dataSource) {
        if (Objects.isNull(dataSource)) {
            return;
        }
        if (Objects.isNull(dataSourcesMap.get(dataSource.getClassPath()))) {
            if (dataSource.getDataSourceBeanName() != null) {
                log.info("get the data source name [" + (dataSourcesMap.size() + 1) + "-" + (dataSource.getDataSourceBeanName()) + "]，belong to the category [" + dataSource.getClassPath() + "]");
            } else if (dataSource.getClassBeanName() != null) {
                log.info("get the data source name [" + (dataSourcesMap.size() + 1) + "-" + (dataSource.getClassBeanName()) + "]，belong to the category [" + dataSource.getClassPath() + "]");
            } else {
                log.info("get the data source name [" + (dataSourcesMap.size() + 1) + "]，belong to the category [" + dataSource.getClassPath() + "]");

            }
            dataSourcesMap.put(dataSource.getClassPath(), dataSource);
        }
    }

    public static String formatAllColumToStr(Class<?> tableClass) {
        List<String> columList = getAllColumnList(tableClass);
        return String.join(",", columList);
    }

    public static List<String> getAllColumnList(Class<?> tableClass) {
        final List<TableInfo> tableInfoList = tableDataBaseMap.get(tableClass);
        return tableInfoList.stream().map(TableInfo::getColumn).collect(Collectors.toList());
    }

    public static TableInfo getTableInfo(Class<?> tableClass, String property) {
        return getTableInfos(tableClass).stream().filter(tb -> tb.getField().getName().equals(property)).findFirst().get();
    }

    public static List<TableInfo> getTableInfos(Class<?> tableClass) {
        return Optional.ofNullable(tableDataBaseMap.get(tableClass)).orElseGet(Collections::emptyList);
    }

    public static String getColumnByField(Class<?> tableClass, String fieldName) {
        List<TableInfo> tableInfos = tableDataBaseMap.get(tableClass);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getField().getName().equals(fieldName)) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableInfos.get(0).getTableName() + "中未识别的字段：" + fieldName);
    }

    public static String getPrimaryKey(Class<?> tableClass) {
        return getTableInfoPrimaryKey(tableClass).getColumn();
    }

    public static TableInfo getTableInfoPrimaryKey(Class<?> tableClass) {
        final List<TableInfo> tableInfoList = tableDataBaseMap.get(tableClass);
        return tableInfoList.stream().filter(tableInfo -> tableInfo.isPrimary()).findFirst().orElse(null);
    }

    public static synchronized void saveTableInfos(Class<?> tableClass, List<TableInfo> tableInfos) {
        if (CollectionUtils.isEmpty(tableInfos)) {
            throw new BraveException("待保存的表字段不可为空");
        }
        final List<TableInfo> tableInfosCache = tableDataBaseMap.get(tableClass);
        if (CollectionUtils.isEmpty(tableInfosCache)) {
            tableDataBaseMap.put(tableClass, tableInfos);
        }
    }


    public static Map<String, DataSourceInfo> getAllDataSourcesMap() {
        return dataSourcesMap;
    }

    public static Map<Class<?>, List<TableInfo>> getAllTableDataBaseMap() {
        return tableDataBaseMap;
    }

    public static void clear() {
        tableDataBaseMap.clear();
    }


}
