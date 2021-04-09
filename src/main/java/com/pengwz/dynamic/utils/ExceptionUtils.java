package com.pengwz.dynamic.utils;

import com.pengwz.dynamic.exception.BraveException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.Objects;

public class ExceptionUtils {

    private static final Log log = LogFactory.getLog(ExceptionUtils.class);

    public static <T extends Throwable> void boxingAndThrowBraveException(T throwable) {
        if (Objects.isNull(throwable)) {
            throw new BraveException("意外的异常抛出", "异常不可为null");
        }
        log.error(throwable.getMessage(), throwable);
        if (throwable instanceof SQLException) {
            String sqlState = ((SQLException) throwable).getSQLState();
            if (StringUtils.isBlank(sqlState)) {
                throw new BraveException(throwable.getMessage());
            }
            if (sqlState.startsWith("23")) {
                throw new BraveException("违反表约束条件", throwable.getMessage());
            }
            if (sqlState.startsWith("25")) {
                throw new BraveException("无效的事务操作", throwable.getMessage());
            }
            if (sqlState.startsWith("26") || sqlState.startsWith("42")) {
                throw new BraveException("错误的SQL语句", throwable.getMessage());
            }
            if (sqlState.startsWith("28")) {
                throw new BraveException("没有足够的权限操作", throwable.getMessage());
            }
            if (sqlState.startsWith("40")) {
                throw new BraveException("事务回滚异常", throwable.getMessage());
            }
            if (sqlState.startsWith("54") || sqlState.startsWith("22")) {
                throw new BraveException("超出数据库表限制", throwable.getMessage());
            }
            if (sqlState.startsWith("58")) {
                throw new BraveException("数据库异常", throwable.getMessage());
            }
            if (sqlState.startsWith("S0")) {
                throw new BraveException("实体类属性不在SQL查询返回的结果集内", throwable.getMessage());
            }
        }

        if (throwable instanceof ReflectiveOperationException) {
            throw new BraveException("无法创建对象，可能不存在无参构造器", throwable.getMessage());
        }

        if (throwable instanceof BraveException) {
            throw (BraveException) throwable;
        }

        //尚未匹配的异常，先行抛出
        throw new BraveException(throwable.getMessage(), throwable);
    }
}
