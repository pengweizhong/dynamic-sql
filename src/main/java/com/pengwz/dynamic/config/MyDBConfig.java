package com.pengwz.dynamic.config;

import java.util.HashMap;
import java.util.Map;

import static com.pengwz.dynamic.config.DBConfigEnum.*;
public class MyDBConfig implements DataSourceConfig {
    @Override
    public Properties getProperties() {
        //必须的配置
        Properties properties = new Properties();
        properties.setConfig(DRIVER, "com.mysql.jdbc.Driver");
        properties.setConfig(USERNAME, "root");
        properties.setConfig(PASSWORD, "pengwz");
        properties.setConfig(PORT, "3306");
        properties.setConfig(HOST, "127.0.0.1");
        properties.setConfig(DATABASE, "dynamic");
        //其他参数，比如设置时区，字符集等
        Map<String, String> otherConfigMap = new HashMap<>();
        otherConfigMap.put("serverTimezone", "GMT%2B8");
        otherConfigMap.put("useUnicode", "true");
        otherConfigMap.put("characterEncoding", "utf-8");
        otherConfigMap.put("rewriteBatchedStatements", "true");
        properties.setOtherConfigMap(otherConfigMap);
        return properties;
    }
}
