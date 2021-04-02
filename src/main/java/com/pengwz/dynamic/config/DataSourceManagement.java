package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DataSourceManagement {
    private static final Log log = LogFactory.getLog(DataSourceManagement.class);

    private static final DataSourceManagement dataSourceManagement = new DataSourceManagement();

    private static final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    private DataSourceManagement() {
    }

    public static synchronized DataSourceManagement getDataSourceManagement(DataSourceConfig dataSourceConfig) {
        if (Objects.isNull(dataSourceConfig.getDataSource())) {
            throw new BraveException("未配置数据源信息");
        }
        String dataSourceName = dataSourceConfig.getClass().toString();
        if (Objects.isNull(dataSourceMap.get(dataSourceName))) {
            log.info("初始化数据源：" + (dataSourceMap.size() + 1) + "，所属类：" + dataSourceConfig.getClass());
            dataSourceMap.put(dataSourceName, dataSourceConfig.getDataSource());
        }
        return dataSourceManagement;
    }

    public Connection getConnection(DataSourceConfig dataSourceConfig) {
        if (Objects.isNull(dataSourceConfig.getDataSource())) {
            throw new BraveException("未配置数据源信息");
        }
        String dataSourceName = dataSourceConfig.getClass().toString();
        DataSource dataSource = dataSourceMap.get(dataSourceName);
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        throw new BraveException("无法获取SQL连接，请检查数据库连接配置");
    }

    public void releaseConnection(Connection connection) {
        if (Objects.nonNull(connection)) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
