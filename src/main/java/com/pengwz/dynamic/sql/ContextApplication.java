package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.TableInfo;
import com.pengwz.dynamic.utils.CollectionUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("all")
public class ContextApplication {
    private static final Log log = LogFactory.getLog(ContextApplication.class);
    private static final Map<String, Map<String, List<TableInfo>>> dataBaseMap = new ConcurrentHashMap<>();
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
                log.info("Get the data source name [" + (dataSourcesMap.size() + 1) + "-" + (dataSource.getDataSourceBeanName()) + "]，belong to the category [" + dataSource.getClassPath() + "]");
            } else if (dataSource.getClassBeanName() != null) {
                log.info("Get the data source name [" + (dataSourcesMap.size() + 1) + "-" + (dataSource.getClassBeanName()) + "]，belong to the category [" + dataSource.getClassPath() + "]");
            } else {
                log.info("Get the data source name [" + (dataSourcesMap.size() + 1) + "]，belong to the category [" + dataSource.getClassPath() + "]");

            }
            dataSourcesMap.put(dataSource.getClassPath(), dataSource);
        }
    }


    public static synchronized boolean existsTable(String tableName, Class<?> dataSourceClass) {
        return existsTable(tableName, dataSourceClass.getName());
    }

    public static synchronized boolean existsTable(String tableName, String dataSourceName) {
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
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

    public static String formatAllColumToStr(String dataSourceName, String tableName) {
        List<String> columList = getAllColumnList(dataSourceName, tableName);
        return String.join(",", columList);
    }

    public static List<String> getAllColumnList(Class<?> dataSourceClass, String tableName) {
        return getAllColumnList(dataSourceClass.getCanonicalName(), tableName);
    }

    public static List<String> getAllColumnList(String dataSourceName, String tableName) {
        List<String> members = new ArrayList<>();
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
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

    public static TableInfo getTableInfo(String dataSourceName, String tableName, String property) {
        return getTableInfos(dataSourceName, tableName).stream().filter(tb -> tb.getField().getName().equals(property)).findFirst().get();
    }

    public static List<TableInfo> getTableInfos(Class<?> dataSourceClass, String tableName) {
        return getTableInfos(dataSourceClass.getCanonicalName(), tableName);
    }

    public static List<TableInfo> getTableInfosOrNull(String dataSourceName, String tableName) {
        try {
            return getTableInfos(dataSourceName, tableName);
        } catch (BraveException e) {
            return null;
        }
    }

    public static List<TableInfo> getTableInfos(String dataSourceName, String tableName) {
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
        if (tableMap != null) {
            List<TableInfo> tableInfosCache = tableMap.get(tableName);
            if (tableInfosCache != null) {
                return tableInfosCache;
            }
        }
//        return null;
        throw new BraveException("无法根据数据源" + dataSourceName + "获取表" + tableName + "信息");
//        synchronized (ContextApplication.class) {
//            List<TableInfo> tableInfos = tableMap.get(tableName);
//            if (CollectionUtils.isNotEmpty(tableInfos)) {
//                return tableInfos;
//            }
//            try {
//                Class<?> currentClass = Class.forName(dataSourceName);
//                Table table = currentClass.getAnnotation(Table.class);
//                if (table == null) {
//                    throw new BraveException("缺失注解 @Table For " + dataSourceName);
//                }
//                Check.checkAndSave(currentClass, table, dataSourceName);
//                return tableMap.get(tableName);
//            } catch (Exception e) {
//                throw new BraveException(e);
//            }
//        }
    }

    public static List<TableInfo> getTableInfos(Class<?> tableClass) {
        Table table = tableClass.getAnnotation(Table.class);
        if (table == null) {
            return null;
        }
        Class<? extends DataSourceConfig> aClass = table.dataSourceClass();
        String dataSourceName;
        if (aClass.equals(DataSourceConfig.class)) {
            dataSourceName = getDefalutDataSourceName();
        } else {
            dataSourceName = aClass.getCanonicalName();
        }
        String tableName = Check.getTableName(table.value(), dataSourceName);
        List<TableInfo> tableInfos = getTableInfosOrNull(dataSourceName, tableName);
        if (CollectionUtils.isNotEmpty(tableInfos)) {
            return tableInfos;
        }
        Check.checkAndSave(tableClass, dataSourceName, tableName);
        return getTableInfos(dataSourceName, tableName);
    }

    public static String getColumnByField(Class<?> dataSourceClass, String tableName, String fieldName) {
        return getColumnByField(dataSourceClass.getCanonicalName(), tableName, fieldName);
    }

    public static String getColumnByField(String dataSourceName, String tableName, String fieldName) {
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.getField().getName().equals(fieldName)) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableName + "中未识别的字段：" + fieldName);
    }

    public static String getPrimaryKey(Class<?> dataSourceClass, String tableName) {
        return getPrimaryKey(dataSourceClass.getCanonicalName(), tableName);
    }

    public static String getPrimaryKey(String dataSourceName, String tableName) {
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.isPrimary()) {
                return tableInfo.getColumn();
            }
        }
        throw new BraveException(tableName + "中未获取到主键字段");
    }

    public static TableInfo getTableInfoPrimaryKey(Class<?> dataSourceClass, String tableName) {
        return getTableInfoPrimaryKey(dataSourceClass.getCanonicalName(), tableName);
    }

    public static TableInfo getTableInfoPrimaryKey(String dataSourceName, String tableName) {
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
        List<TableInfo> tableInfos = tableMap.get(tableName);
        for (TableInfo tableInfo : tableInfos) {
            if (tableInfo.isPrimary()) {
                return tableInfo;
            }
        }
        return null;
    }

    public static Map<String, Map<String, List<TableInfo>>> getAll() {
        return dataBaseMap;
    }

    public static synchronized void saveTable(Class<?> dataSourceClass, String tableName, List<TableInfo> tableInfos) {
        saveTable(dataSourceClass.getCanonicalName(), tableName, tableInfos);
    }

    public static synchronized void saveTable(String dataSourceName, String tableName, List<TableInfo> tableInfos) {
        if (StringUtils.isEmpty(tableName)) {
            throw new BraveException("待保存的表名不可为空");
        }
        if (CollectionUtils.isEmpty(tableInfos)) {
            throw new BraveException("待保存的表字段不可为空");
        }
        Map<String, List<TableInfo>> tableMap = dataBaseMap.get(dataSourceName);
        if (tableMap == null) {
            tableMap = new ConcurrentHashMap<>();
            dataBaseMap.put(dataSourceName, tableMap);
        }
        tableMap.put(tableName, tableInfos);
    }

    public static void clear() {
        dataBaseMap.clear();
    }


}
