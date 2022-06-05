package com.pengwz.dynamic;

import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.base.Sqls;
import com.pengwz.dynamic.sql.base.enumerate.FunctionEnum;
import org.junit.Test;

import java.util.List;

public class SimpleTest {


    @Test
    public void test1() {
        System.out.println(SimpleTest.class.getCanonicalName());
    }

    public class SqlsImpl2 implements Sqls {

        @Override
        public Object selectByPrimaryKey(Object primaryKeyValue) {
            return null;
        }

        @Override
        public Object selectSingle() {
            return null;
        }

        @Override
        public List select() {
            return null;
        }

        @Override
        public List selectAll() {
            return null;
        }

        @Override
        public PageInfo selectPageInfo() {
            return null;
        }

        @Override
        public Integer batchInsert() {
            return null;
        }

        @Override
        public Integer insertActive() {
            return null;
        }

        @Override
        public Integer insertOrUpdate() {
            return null;
        }

        @Override
        public Integer update() {
            return null;
        }

        @Override
        public Integer updateActive() {
            return null;
        }

        @Override
        public Integer updateByPrimaryKey() {
            return null;
        }

        @Override
        public Integer updateActiveByPrimaryKey() {
            return null;
        }

        @Override
        public Integer delete() {
            return null;
        }

        @Override
        public Integer deleteByPrimaryKey(Object primaryKeyValue) {
            return null;
        }

        @Override
        public Object selectAggregateFunction(String property, FunctionEnum functionEnum, Class returnType) {
            return null;
        }
    }
}
