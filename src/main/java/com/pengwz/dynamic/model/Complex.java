package com.pengwz.dynamic.model;

import com.pengwz.dynamic.sql.DynamicSql;

public class Complex<T> {
    private DynamicSql<T> dynamicSql;

    private RelationEnum relationEnum;

    public static <T> Complex<T> instance(RelationEnum relationEnum) {
        Complex<T> objectComplex = new Complex<>();
        objectComplex.dynamicSql = DynamicSql.createDynamicSql();
        objectComplex.relationEnum = relationEnum;
        return objectComplex;
    }

    public DynamicSql<T> getDynamicSql() {
        return dynamicSql;
    }

    public RelationEnum getRelationEnum() {
        return relationEnum;
    }

    @Override
    public String toString() {
        return "Complex{" +
                "dynamicSql=" + dynamicSql +
                ", relationEnum=" + relationEnum +
                '}';
    }
}
