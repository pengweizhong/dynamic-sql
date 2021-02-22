package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class DataSourceManagement {
    private static final Log log = LogFactory.getLog(DataSourceManagement.class);

    private DataSourceManagement() {
    }

    private static DataSourceManagement dataSourceManagement = new DataSourceManagement();

    private DataSource dataSource;

    public void releaseConnection(Connection connection) {
        if (Objects.nonNull(connection)) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public Connection getConnection(DataSourceConfig dataSourceConfig) {
        if (Objects.isNull(dataSource)) {
            dataSource = dataSourceConfig.getDataSource();
            if (Objects.isNull(dataSource)) {
                throw new BraveException("未配置数据源信息");
            }
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        throw new BraveException("无法获取SQL连接，请检查数据库连接配置");
    }

    public static synchronized DataSourceManagement getDataSourceManagement() {
        return dataSourceManagement;
    }

}
