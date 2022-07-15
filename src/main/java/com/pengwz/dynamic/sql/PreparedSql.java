package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.exception.BraveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreparedSql {
    private static final Log log = LogFactory.getLog(PreparedSql.class);
    private final Class<?> currentClass;
    private String sql;
    private final List<Object> params;
    private final List<List<Object>> batchParams = new ArrayList<>();

    public PreparedSql(Class<?> currentClass, List<Object> params) {
        this.currentClass = currentClass;
        this.params = params;
    }

    public void addParameter(Object param) {
        this.params.add(param);
    }

    public void addParameter(int index, Object param) {
        this.params.add(index, param);
    }

    public List<Object> startBatchParameter() {
        final ArrayList<Object> batch = new ArrayList<>();
        batchParams.add(batch);
        return batch;
    }

    public List<Object> getPreparedParameters() {
        List<Object> fixParams = new ArrayList<>();
        if (!params.isEmpty()) {
            for (Object param : params) {
                if (param instanceof Iterable) {
                    Iterator iterator = ((Iterable) param).iterator();
                    if (!iterator.hasNext()) {
                        throw new BraveException("SQL入参集合不可以为空");
                    }
                    while (iterator.hasNext()) {
                        final Object next = iterator.next();
                        if (next instanceof Enum) {
                            fixParams.add(((Enum<?>) next).name());
                        } else {
                            fixParams.add(next);
                        }
                    }
                } else {
                    if (param instanceof Enum) {
                        fixParams.add(((Enum<?>) param).name());
                    } else {
                        fixParams.add(param);
                    }
                }
            }
        }
        return fixParams;
    }

    public List<List<Object>> getBatchPreparedParameters() {
        List<List<Object>> fixParams = new ArrayList<>();
        if (!batchParams.isEmpty()) {
            for (List<Object> batchParam : batchParams) {
                List<Object> fixParam = new ArrayList<>();
                for (Object param : batchParam) {
                    if (param instanceof Iterable) {
                        Iterator iterator = ((Iterable) param).iterator();
                        if (!iterator.hasNext()) {
                            throw new BraveException("SQL入参集合不可以为空");
                        }
                        while (iterator.hasNext()) {
                            final Object next = iterator.next();
                            if (next instanceof Enum) {
                                fixParam.add(((Enum<?>) next).name());
                            } else {
                                fixParam.add(next);
                            }
                        }
                    } else {
                        if (param instanceof Enum) {
                            fixParam.add(((Enum<?>) param).name());
                        } else {
                            fixParam.add(param);
                        }
                    }
                }
                fixParams.add(fixParam);
            }
        }
        return fixParams;
    }

    public void printSqlAndParams(String sql) {
        this.setSql(sql);
        if (log.isDebugEnabled()) {
            try {
                final ArrayList<String> paramList = new ArrayList<>();
                for (int i = 1; i <= params.size(); i++) {
                    //加 - 是否会引起误解？
                    paramList.add(/*i + " - " +*/ params.get(i - 1) + "");
                }
                final String join = String.join(", ", paramList);
                log.debug("Preparing: " + sql);
                log.debug("Parameters: " + join);
            } catch (Exception ex) {
                log.error(sql);
                log.error("打印SQL参数时发生异常，请检查ToString()方法是否允许被正常输出");
            }

        }
    }

    public void printSqlAndBatchParams(String sql) {
        this.setSql(sql);
        if (log.isDebugEnabled()) {
            try {
                for (List<Object> params : batchParams) {
                    final ArrayList<String> paramList = new ArrayList<>();
                    for (int i1 = 1; i1 <= params.size(); i1++) {
                        paramList.add(/*i + " - " +*/ params.get(i1 - 1) + "");
                    }
                    final String join = String.join(", ", paramList);
                    log.debug("Preparing: " + sql);
                    log.debug("Parameters: " + join);
                }
            } catch (Exception ex) {
                log.error(sql);
                log.error("打印SQL参数时发生异常，请检查ToString()方法是否允许被正常输出");
            }
        }
    }

    public Class<?> getCurrentClass() {
        return currentClass;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public List<List<Object>> getBatchParams() {
        return batchParams;
    }
}

