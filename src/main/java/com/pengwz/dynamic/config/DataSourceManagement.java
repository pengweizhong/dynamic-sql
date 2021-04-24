package com.pengwz.dynamic.config;

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

}
