package com.pengwz.dynamic.check;

import com.pengwz.dynamic.model.DbType;
import junit.framework.TestCase;
import org.junit.Test;


public class CheckTest extends TestCase {

    @Test
    public void testSplicingName() {
        System.out.println(Check.splicingName(DbType.MYSQL, "hello"));
        System.out.println(Check.splicingName(DbType.MYSQL, "hello`"));
        System.out.println(Check.splicingName(DbType.MYSQL, "`hello"));
        System.out.println(Check.splicingName(DbType.MYSQL, "`hello`"));
        System.out.println(Check.splicingName(DbType.ORACLE, "hello"));
        System.out.println(Check.splicingName(DbType.ORACLE, "hello\""));
        System.out.println(Check.splicingName(DbType.ORACLE, "\"hello"));
        System.out.println(Check.splicingName(DbType.ORACLE, "\"hello\""));
    }

    public void testUnSplicingName() {
    }
}