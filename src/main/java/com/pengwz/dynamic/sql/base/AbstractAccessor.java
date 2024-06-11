package com.pengwz.dynamic.sql.base;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public abstract class AbstractAccessor {

    protected abstract String getDataSourceName();

    protected abstract ResultSet getResultSet();

    protected abstract void setConnection(Connection connection);

    protected abstract Connection getConnection();

    protected abstract Statement getStatement();

    protected abstract String getSqlString();

//    protected abstract PreparedSql getPreparedSql();
}
