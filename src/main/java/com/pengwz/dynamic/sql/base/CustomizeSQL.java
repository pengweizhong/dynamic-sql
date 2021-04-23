package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.ConverterUtils;
import com.pengwz.dynamic.utils.ExceptionUtils;
import com.pengwz.dynamic.utils.ReflectUtils;
import com.pengwz.dynamic.utils.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 执行自定义SQL
 */
public class CustomizeSQL<T> {

    private static final Log log = LogFactory.getLog(CustomizeSQL.class);

    private final Class<T> target;

    private final String sql;

    private Connection connection;

    public CustomizeSQL(Class<? extends DataSourceConfig> dataSource, Class<T> target, String sql) {
        this.target = target;
        this.sql = sql;
        this.connection = DataSourceManagement.initConnection(dataSource.toString());
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
                    Column column = field.getAnnotation(Column.class);
                    Object object;
                    if (Objects.isNull(column)) {
                        object = ConverterUtils.convertJdbc(resultSet, StringUtils.caseField(field.getName()), field.getType());
                    } else {
                        object = ConverterUtils.convertJdbc(resultSet, column.value().trim(), field.getType());
                    }
                    ReflectUtils.setFieldValue(field, obj, object);
                }
                selectResult.add(obj);
            }
        } catch (Exception e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(resultSet, preparedStatement, connection);
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
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            ExceptionUtils.boxingAndThrowBraveException(e, sql);
        } finally {
            DataSourceManagement.close(null, preparedStatement, connection);
        }
        return -1;
    }
}
