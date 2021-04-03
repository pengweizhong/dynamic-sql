package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.ContextApplication;
import com.pengwz.dynamic.utils.ReflectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 执行自定义SQL
 */
public class CustomizeSQL<T> {

    private static final Log log = LogFactory.getLog(CustomizeSQL.class);

    private final Class<? extends DataSourceConfig> dataSource;

    private final Class<T> target;

    private final String sql;

    private Connection connection;

    public CustomizeSQL(Class<? extends DataSourceConfig> dataSource, Class<T> target, String sql) {
        this.dataSource = dataSource;
        this.target = target;
        this.sql = sql;
        this.connection = DataSourceManagement.initConnection(ContextApplication.getDataSource(this.dataSource));
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public T selectSqlAndReturnSingle() {
        List<T> ts = selectSqlAndReturnList();
        if (ts.size() > 1) {
            throw new BraveException("期待返回1条结果，实际返回了" + ts.size() + "条");
        }
        return ts.size() == 1 ? ts.get(0) : null;
    }

    public List<T> selectSqlAndReturnList() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        List<T> selectResult = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                T obj = target.newInstance();
                Field[] declaredFields = target.getDeclaredFields();
                for (Field field : declaredFields) {
                    Object object = null;
                    try {
                        object = resultSet.getObject(field.getName(), field.getType());
                    } catch (SQLException e) {
                        log.warn("SQL查询返回的结果不包含" + field.getName() + "列");
                    }
                    ReflectUtils.setFieldValue(field, obj, object);
                }
                selectResult.add(obj);
            }
        } catch (SQLException e) {
            log.error("错误的SQL：" + sql);
            log.error(e.getMessage(), e);
            throw new BraveException("无效的SQL");
        } catch (ReflectiveOperationException e) {
            log.error(e.getMessage(), e);
            throw new BraveException("查询对象无法实例化：" + target + "，可能不存在无参构造器");
        } finally {
            DataSourceManagement.close(ContextApplication.getDataSource(this.dataSource), resultSet, preparedStatement, connection);
        }
        return selectResult;
    }

    public int executeDMLSql() {
        if (log.isDebugEnabled()) {
            log.debug(sql);
        }
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            int execute = preparedStatement.executeUpdate();
            connection.commit();
            return execute;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new BraveException(ex.getMessage(), ex);
            }
            log.error("错误的SQL：" + sql);
            log.error(e.getMessage(), e);
            throw new BraveException("无效的SQL");
        } finally {
            DataSourceManagement.close(ContextApplication.getDataSource(this.dataSource), null, preparedStatement, connection);
        }
    }
}
