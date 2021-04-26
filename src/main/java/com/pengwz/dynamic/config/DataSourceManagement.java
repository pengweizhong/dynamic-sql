package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.utils.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class DataSourceManagement {

    private static final Log log = LogFactory.getLog(DataSourceManagement.class);

    private DataSourceManagement() {
    }


    public static void close(String dataSourceName, ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        if (dataSourceInfo.getDataSourceBeanName() != null) {
            DataSourceUtils.releaseConnection(connection, dataSourceInfo.getDataSource());
        } else {
            if (Objects.nonNull(resultSet)) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (Objects.nonNull(preparedStatement)) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    public static Connection initConnection(String dataSourceName) {
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        if (dataSourceInfo.getDataSourceBeanName() != null) {
            return DataSourceUtils.getConnection(dataSourceInfo.getDataSource());
        }
        try {
            return dataSourceInfo.getDataSource().getConnection();
        } catch (SQLException e) {
            ExceptionUtils.boxingAndThrowBraveException(e);
        }
        return null;
    }

    public static String initDataSourceConfig(Class<?> dataSourceClass, String tableName) {
        String dataSourceName;
        if (Objects.isNull(dataSourceClass)) {
            dataSourceName = ContextApplication.getDefalutDataSource();
            if (Objects.isNull(dataSourceName)) {
                throw new BraveException("须指定数据源；表名：" + tableName);
            }
        } else {
            if (dataSourceClass.equals(DataSourceConfig.class)) {
                String tableNameDesc = tableName == null ? "" : "表名：" + tableName;
                throw new BraveException("没有指定默认数据源时，则须指定数据源；" + tableNameDesc);
            }
            dataSourceName = dataSourceClass.toString();
            if (!ContextApplication.existsDataSouce(dataSourceName)) {
                try {
                    DataSourceConfig dataSourceConfig = (DataSourceConfig) dataSourceClass.newInstance();
                    DataSourceInfo dataSourceInfo = new DataSourceInfo();
                    dataSourceInfo.setDefault(dataSourceConfig.defaultDataSource());
                    dataSourceInfo.setClassPath(dataSourceName);
                    dataSourceInfo.setDataSource(dataSourceConfig.getDataSource());
                    ContextApplication.putDataSource(dataSourceInfo);
                } catch (InstantiationException e) {
                    throw new BraveException(e.getMessage());
                } catch (IllegalAccessException e) {
                    throw new BraveException("必须提供无参构造器");
                }
            }
        }
        return dataSourceName;
    }
}
