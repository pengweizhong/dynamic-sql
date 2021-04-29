//package com.pengwz.dynamic.config;
//
//import com.alibaba.druid.pool.DruidDataSource;
//
//import javax.sql.DataSource;
//
//public class MyDBConfig implements DataSourceConfig {
//
//    @Override
//    public DataSource getDataSource() {
//        DruidDataSource ds = new DruidDataSource();
//        ds.setUrl("jdbc:mysql://127.0.0.1:3306/dynamic?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
//        ds.setUsername("root");
//        ds.setPassword("pengwz");
//        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        ds.setInitialSize(3);
//        ds.setMaxActive(10);
//        ds.setMinIdle(5);
//        ds.setValidationQuery("select 1");
//        ds.setTestOnBorrow(true);
//        ds.setTestOnReturn(false);
//        ds.setUseUnfairLock(true);
//        ds.setTestWhileIdle(true);
//        ds.setMinEvictableIdleTimeMillis(10 * 60 * 1000L);
//        ds.setTimeBetweenEvictionRunsMillis(5 * 60 * 1000L);
//        return ds;
//    }
//
//    @Override
//    public boolean defaultDataSource() {
//        return Boolean.TRUE;
//    }
////    @Override
////    public DataSource getDataSource() {
////        MysqlDataSource ds = new MysqlDataSource();
////        ds.setUrl("jdbc:mysql://127.0.0.1:3306/dynamic?useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
////        ds.setUser("root");
////        ds.setPassword("pengwz");
////        return ds;
////    }
//}