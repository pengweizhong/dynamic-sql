package com.pengwz.dynamic.sql;


import com.pengwz.dynamic.config.OracleDatabaseConfig;
import org.junit.Test;

public class OracleBraveSqlTest {

    String createStudentTableSql = " ---1、创建模拟的数据表 ---\n" +
            "  --1.1.创建学生表Student\n" +
            "  create table Student(\n" +
            "         StuId NUMBER NOT NULL,     --学生ID\n" +
            "         StuName VARCHAR2(10) NOT NULL, --名称\n" +
            "         Gender VARCHAR2(10)NOT NULL,  -- 性别\n" +
            "         Age NUMBER(2) NOT NULL,    -- 年龄     \n" +
            "         JoinDate DATE NULL,       --入学时间\n" +
            "         ClassId NUMBER NOT NULL,   --班级ID\n" +
            "         Address VARCHAR2(50) NULL   --家庭住址           \n" +
            "  ); ";


    @Test
    public void createStudentTable() {
        BraveSql.build(Void.class).executeSql(createStudentTableSql, OracleDatabaseConfig.class);

    }
}