package com.pengwz.dynamic.config;

import javax.sql.DataSource;

public interface DataSourceConfig {
    /**
     * 若没有自定义数据源 ，请使用mysql链接驱动包自带的 MysqlDataSource
     */
    DataSource getDataSource();

    /**
     * 默认数据源，默认false，仅支持一个默认数据源
     */
    default boolean defaultDataSource() {
        return false;
    }


}
