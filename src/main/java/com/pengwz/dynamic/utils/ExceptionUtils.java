package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.exception.BraveException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;

public class ExceptionUtils {

    private static final Log log = LogFactory.getLog(ExceptionUtils.class);

    public static <T extends Throwable> void boxingAndThrowBraveException(T throwable) {
        boxingAndThrowBraveException(throwable, null);
    }

    public static <T extends Throwable> void boxingAndThrowBraveException(T throwable, String sql) {
        if (Objects.isNull(throwable)) {
            throw new BraveException("意外的异常抛出", "异常不可为null");
        }
        log.error(throwable.getMessage(), throwable);

        if (throwable instanceof SQLFeatureNotSupportedException) {
            throw new BraveException("数据库/驱动不支持或版本过低，请检查");
        }
        if (throwable instanceof SQLException) {
            String sqlState = ((SQLException) throwable).getSQLState();
            if (StringUtils.isBlank(sqlState)) {
                throw new BraveException(throwable.getMessage());
            }
            if (sqlState.startsWith("22")) {
                throw new BraveException("违反表字段类型限制", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("23")) {
                throw new BraveException("违反表约束条件", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("25")) {
                throw new BraveException("无效的事务操作", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("28")) {
                throw new BraveException("没有足够的权限操作", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("40")) {
                throw new BraveException("事务回滚异常", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("42")) {
                throw new BraveException("SQL执行错误，可能传入了类型不匹配的值", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("58")) {
                throw new BraveException("数据库异常", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("99")) {
                throw new BraveException(throwable.getMessage(), throwable);
            }
            if (sqlState.startsWith("S0")) {
                throw new BraveException("实体类属性不在SQL查询返回的结果集内", sql, throwable.getMessage());
            }
            if (sqlState.startsWith("HY")) {
                throw new BraveException("缺少必要的字段", sql, throwable.getMessage());
            }
            if (StringUtils.isBlank(sql)) {
                throw new BraveException(throwable.getMessage(), throwable);
            } else {
                throw new BraveException("错误的SQL语句", sql, throwable.getMessage());
            }
        }
        if (throwable instanceof ReflectiveOperationException) {
            String message = StringUtils.isBlank(sql) ? "无法创建对象，可能不存在无参构造器" : "无法创建对象，可能不存在无参构造器。ERROR SQL：" + sql;
            throw new BraveException(message, throwable);
        }
        if (throwable instanceof BraveException) {
            throw (BraveException) throwable;
        }
        //尚未匹配的异常，先行抛出
        throw new BraveException(throwable.getMessage(), throwable);
    }

}
