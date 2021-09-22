package com.pengwz.dynamic.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class OracleDatabaseConfig implements DataSourceConfig {

    @Override
    public DataSource getDataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl("jdbc:oracle:thin:@172.16.1.3:1521:ORCL");
        ds.setUsername("C##TESTSYSTTEM");
        ds.setPassword("1231");
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setInitialSize(5);
        ds.setMaxActive(50);
        ds.setMinIdle(5);
        ds.setValidationQuery("select 1 FROM DUAL");
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
//        ds.setJdbcUrl("jdbc:oracle:thin:@172.16.1.3:1521:ORCL");
//        ds.setUsername("C##TESTSYSTTEM");
//        ds.setPassword("1231");
//        ds.setDriverClassName("oracle.jdbc.OracleDriver");
//        ds.setConnectionTestQuery("select 1 FROM DUAL");
//        return ds;
//    }
}
