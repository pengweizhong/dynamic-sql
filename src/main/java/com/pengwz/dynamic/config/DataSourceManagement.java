package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.model.DataSourceInfo;
import com.pengwz.dynamic.model.DbType;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.utils.ExceptionUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public final class DataSourceManagement {

    private DataSourceManagement() {
    }

    public static void close(String dataSourceName, ResultSet resultSet, Statement statement, Connection connection) {
        DataSourceInfo dataSourceInfo = ContextApplication.getDataSourceInfo(dataSourceName);
        JdbcUtils.closeResultSet(resultSet);
        JdbcUtils.closeStatement(statement);
        if (dataSourceInfo.getDataSourceBeanName() != null && connection != null) {
            DataSourceUtils.releaseConnection(connection, dataSourceInfo.getDataSource());
        } else {
            JdbcUtils.closeConnection(connection);
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

    @SuppressWarnings("all")
    public static String initDataSourceConfig(Class<?> dataSourceClass, String tableName) {
        String dataSourceName;
        int checkWaitTime = 30 * 1000;
        long limitTime = System.currentTimeMillis() + checkWaitTime;
        while (true) {
            dataSourceName = getDataSourceName(dataSourceClass);
            if (dataSourceName != null) {
                break;
            }
            //x秒内循环获取
            if (limitTime <= System.currentTimeMillis()) {
                break;
            }
            try {
                //半秒检查一下
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (Objects.isNull(dataSourceName)) {
            throw new BraveException("在不存在默认数据源的情况下，须显式指定数据源；非spring环境必须明确指定数据源；表名：" + tableName);
        }
        return dataSourceName;
    }

    private static String getDataSourceName(Class<?> dataSourceClass) {
        String dataSourceName;
        if (dataSourceClass.equals(DataSourceConfig.class)) {
            dataSourceName = ContextApplication.getDefalutDataSourceName();
        } else {
            dataSourceName = dataSourceClass.getCanonicalName();
            if (!ContextApplication.existsDataSouce(dataSourceName)) {
                try {
                    DataSourceConfig dataSourceConfig = (DataSourceConfig) dataSourceClass.newInstance();
                    DataSourceInfo dataSourceInfo = new DataSourceInfo();
                    dataSourceInfo.setDefault(dataSourceConfig.defaultDataSource());
                    dataSourceInfo.setClassPath(dataSourceName);
                    DataSource dataSource = dataSourceConfig.getDataSource();
                    dataSourceInfo.setDataSource(dataSource);
                    dataSourceInfo.setDbType(getDbType(dataSource));
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

    public static DbType getDbType(DataSource dataSource) {
        String url = getDriverUrl(dataSource);
        if (url.startsWith("jdbc:mysql")) {
            return DbType.MYSQL;
        }
        if (url.startsWith("jdbc:mariadb:")) {
            return DbType.MARIADB;
        }
        if (url.startsWith("jdbc:oracle:") || url.startsWith("JDBC:oracle:")) {
            return DbType.ORACLE;
        }
        return DbType.OTHER;
    }

    public static String getDriverUrl(DataSource dataSource) {
        List<Method> methods = new ArrayList<>();
        getAllMethod(dataSource.getClass(), methods);
        Map<String, Method> methodMap = methods.stream().collect(Collectors.toMap(Method::getName, v -> v, (k1, k2) -> k1));
        try {
            //DruidDataSource  BasicDataSource
            Method var1 = methodMap.get("getUrl");
            if (var1 != null) {
                return String.valueOf(var1.invoke(dataSource));
            }
            //HikariDataSource  ComboPooledDataSource  org.apache.tomcat.jdbc.pool.DataSource
            Method var2 = methodMap.get("getJdbcUrl");
            if (var2 != null) {
                return String.valueOf(var2.invoke(dataSource));
            }
        } catch (Exception e) {
            throw new BraveException("尚未适配的数据库连接池");
        }
        throw new BraveException("尚未适配的数据库连接池");
    }

    public static void getAllMethod(Class<?> dataSourceClass, List<Method> methods) {
        if (dataSourceClass == DataSource.class || dataSourceClass == Object.class) {
            return;
        }
        Method[] declaredMethods = dataSourceClass.getDeclaredMethods();
        methods.addAll(Arrays.asList(declaredMethods));
        getAllMethod(dataSourceClass.getSuperclass(), methods);
    }

}
