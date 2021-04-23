package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.utils.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public final class DataSourceManagement {

    private static final Log log = LogFactory.getLog(DataSourceManagement.class);

    private DataSourceManagement() {
    }


    public static void close(ResultSet resultSet, PreparedStatement preparedStatement, Connection connection) {
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

    public static Connection initConnection(String dataSourceName) {
        DataSource dataSource = ContextApplication.getDataSource(dataSourceName);
        if (Objects.isNull(dataSource)) {
            throw new BraveException("无法根据名称：" + dataSourceName + "获取数据源");
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            ExceptionUtils.boxingAndThrowBraveException(e);
        }
        return null;
    }

}
