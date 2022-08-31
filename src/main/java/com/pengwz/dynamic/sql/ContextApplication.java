package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.check.Check;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.TableColumnInfo;
import com.pengwz.dynamic.model.TableInfo;
import javafx.scene.control.Tab;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class ContextApplication {
    private static final Log log = LogFactory.getLog(ContextApplication.class);
    //key = tableClass   value = TableInfos
    private static final Map<Class<?>, TableInfo> tableInfoMap = new ConcurrentHashMap<>();
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
                log.info("get the data source name " + (dataSourcesMap.size() + 1) + "-" + (dataSource.getDataSourceBeanName()) + "，belong to the category " + dataSource.getClassPath());
            } else if (dataSource.getClassBeanName() != null) {
                log.info("get the data source name " + (dataSourcesMap.size() + 1) + "-" + (dataSource.getClassBeanName()) + "，belong to the category " + dataSource.getClassPath());
            } else {
                log.info("get the data source name " + (dataSourcesMap.size() + 1) + "，belong to the category " + dataSource.getClassPath());

            }
            dataSourcesMap.put(dataSource.getClassPath(), dataSource);
        }
    }

    /**
     * 从缓存中获取表信息
     *
     * @param tableClass 表实体
     * @return 如果缓存中不存在，将返回null
     */
    public static TableInfo getTableInfoCache(Class<?> tableClass) {
        return tableInfoMap.get(tableClass);
    }

    /**
     * 获取表信息
     *
     * @param tableClass 表实体
     * @return 优先从缓存中读取，如果缓存中不存在，那么将实时获取
     */
    public static TableInfo getTableInfo(Class<?> tableClass) {
        final TableInfo tableInfo = tableInfoMap.get(tableClass);
        if (tableInfo == null) {
            return Check.getBuilderTableInfo(tableClass);
        }
        return tableInfo;
    }

    public static TableColumnInfo getTableColumnInfo(Class<?> tableClass, String property) {
        return getTableInfo(tableClass).getTableColumnInfos().stream().filter(tb -> tb.getField().getName().equals(property)).findFirst().get();
    }

    public static String formatAllColumToStr(Class<?> tableClass) {
        List<String> columList = getAllColumnList(tableClass);
        return String.join(",", columList);
    }

    public static List<String> getAllColumnList(Class<?> tableClass) {
        return getTableInfo(tableClass).getTableColumnInfos().stream().map(TableColumnInfo::getColumn).collect(Collectors.toList());
    }


    public static List<TableColumnInfo> getTableColumnInfos(Class<?> tableClass) {
        return getTableInfo(tableClass).getTableColumnInfos();
    }

    public static String getPrimaryKey(Class<?> tableClass) {
        return getTableColumnInfoPrimaryKey(tableClass).getColumn();
    }

    public static TableColumnInfo getTableColumnInfoPrimaryKey(Class<?> tableClass) {
        final TableInfo tableInfo = getTableInfo(tableClass);
        return tableInfo.getTableColumnInfos().stream().filter(tableColumnInfo -> tableColumnInfo.isPrimary()).findFirst().orElse(null);
    }

    public static synchronized void saveTableInfo(Class<?> tableClass, TableInfo tableInfo) {
        final TableInfo tableInfo1 = tableInfoMap.get(tableClass);
        if (tableInfo1 == null) {
            tableInfoMap.put(tableClass, tableInfo);
        }
    }


    public static Map<String, DataSourceInfo> getAllDataSourcesMap() {
        return dataSourcesMap;
    }

    public static Map<Class<?>, TableInfo> getAllTableDataBaseMap() {
        return tableInfoMap;
    }

    public static void tableInfoClear() {
        tableInfoMap.clear();
    }

}
