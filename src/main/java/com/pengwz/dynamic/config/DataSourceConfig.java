package com.pengwz.dynamic.config;

import javax.sql.DataSource;

public interface DataSourceConfig {
    /**
     * 若没有自定义数据源 ，请使用mysql链接驱动包自带的 MysqlDataSource
     */
    DataSource getDataSource();

    /**
     * 默认数据源，默认false，仅支持一个默认数据源，非springBootStarter环境请不要重写该属性，因为普通项目目前未能实现扫描该接口下的所有子类。
     * 等找到解决方案时，修复该问题。
     * github地址：{ https://github.com/pengweizhong/dynamic-sql}
     */
    default boolean defaultDataSource() {
        return false;
    }


}
