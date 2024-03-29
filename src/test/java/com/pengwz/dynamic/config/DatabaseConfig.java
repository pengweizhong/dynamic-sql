package com.pengwz.dynamic.config;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

public class DatabaseConfig implements DataSourceConfig {
    @Override
    public DataSource getDataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl("jdbc:mysql://127.0.0.1:3306/test?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
        ds.setUsername("root");
        ds.setPassword("root");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setInitialSize(5);
        ds.setMaxActive(10);
        ds.setMinIdle(5);
        ds.setValidationQuery("select 1");
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(false);
        ds.setUseUnfairLock(true);
        ds.setTestWhileIdle(true);
        ds.setMinEvictableIdleTimeMillis(10 * 60 * 1000L);
        ds.setTimeBetweenEvictionRunsMillis(5 * 60 * 1000L);
        return ds;
    }
//    @Override
//    public DataSource getDataSource() {
//        HikariDataSource ds = new HikariDataSource();
//        ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
//        ds.setUsername("root");
//        ds.setPassword("root");
//        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        ds.setConnectionTestQuery("select 1");
//        return ds;
//    }

//    @Override
//    public DataSource getDataSource() {
//        ComboPooledDataSource ds = new ComboPooledDataSource();
//        ds.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
//        ds.setUser("root");
//        ds.setPassword("root");
//        try {
//            ds.setDriverClass("com.mysql.cj.jdbc.Driver");
//        } catch (PropertyVetoException e) {
//            e.printStackTrace();
//        }
//        return ds;
//    }
}
