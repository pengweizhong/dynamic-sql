package com.pengwz.dynamic.config;

import javax.sql.DataSource;

public interface DataSourceConfig {
    /**
     * 若没有自定义数据源 ，请使用mysql链接驱动包自带的 MysqlDataSource
     */
    DataSource getDataSource();

}
