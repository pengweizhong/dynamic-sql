package com.pengwz.dynamic.config;

import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.CollectionUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static com.pengwz.dynamic.config.DBConfigEnum.*;
import static com.pengwz.dynamic.config.DataSourceConfig.Properties;

public class DataSourceManagement {

    public static void releaseConnection(Connection connection) {
        if (Objects.nonNull(connection)) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection(DataSourceConfig dataSourceConfig) {
        Properties properties = dataSourceConfig.getProperties();
        EnumMap<DBConfigEnum, String> configEnumMap = properties.getConfigEnumMap();
        Map<String, String> otherConfigMap = properties.getOtherConfigMap();
        String url = "jdbc:mysql://" + configEnumMap.get(HOST) + ":" + configEnumMap.get(PORT) + "/" + configEnumMap.get(DATABASE);
        if (CollectionUtils.isNotEmpty(otherConfigMap)) {
            StringBuilder sb = new StringBuilder();
            sb.append("?");
            for (String key : otherConfigMap.keySet()) {
                sb.append(key).append("=").append(otherConfigMap.get(key)).append("&");
            }
            url += sb.toString().substring(0, sb.length() - 1);
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, configEnumMap.get(USERNAME), configEnumMap.get(PASSWORD));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BraveException("获取链接失败，失败原因：" + e.getMessage());
        }
        return connection;
    }

}
