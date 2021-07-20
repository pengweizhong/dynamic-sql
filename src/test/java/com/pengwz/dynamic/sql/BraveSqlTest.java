package com.pengwz.dynamic.sql;


import com.pengwz.dynamic.config.DatabaseConfig;
import com.pengwz.dynamic.entity.UserEntity;
import com.pengwz.dynamic.entity.UserRoleEntity;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BraveSqlTest {
    private static final List<String> ROLE_NAME_LIST = Arrays.asList("游客", "管理员", "超级管理员", "普通成员", "VIP成员", "穷B VIP（活动送的那种）");
    private static final List<String> USER_NAME_LIST = Arrays.asList("jerry", "tom", "王昭君", "妲己", "貂蝉", "李白", "亚瑟", "项羽", "程咬金", "刘邦", "韩信", "虞美人", "雷神",
            "小可nai", "天下无敌", "天下第一", "我就呵呵哒", "哎，女人呀", "致青春", "我的同桌", "小小", "夏天的风", "可可爱爱的昵称", "保家卫国", "敢于冲锋", "我就是我，颜色不一样的烟火");
    private static final Log log = LogFactory.getLog(BraveSqlTest.class);
    //单表插入数据量
    private static final int tableDataRows = 10_000;
    private static final String dropUserTable = "drop table if exists `t_user`;";
    private static final String dropUserRoleTable = "drop table if exists `t_user_role`;";
    private static final String createUserTable = "CREATE TABLE `t_user` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
            "  `account_no` varchar(50) DEFAULT NULL COMMENT '账号',\n" +
            "  `username` varchar(50) DEFAULT NULL COMMENT '用户名',\n" +
            "  `password` varchar(50) DEFAULT NULL COMMENT '密码',\n" +
            "    `email` varchar(50) DEFAULT NULL COMMENT '邮箱',\n" +
            "    `birthday` datetime DEFAULT NULL COMMENT '生日',\n" +
            "    `desc` varchar(500) DEFAULT NULL COMMENT '邮箱',\n" +
            "    `is_delete` tinyint(1)  DEFAULT NULL COMMENT '是否删除 true 已删除 false 未删除',\n" +
            "  `create_date` timestamp  NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "  `update_date` timestamp  NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',\n" +
            "  PRIMARY KEY (`id`),\n" +
            "  UNIQUE KEY `t_user_UN` (`account_no`)\n" +
            ") ENGINE=InnoDB COMMENT ='用户表';";
    private static final String createUserRoleTable = "CREATE TABLE `t_user_role` (\n" +
            "  `uid` varchar(50) NOT NULL  COMMENT 'UUID主键',\n" +
            "  `account_no` varchar(50) DEFAULT NULL COMMENT '账号',\n" +
            "  `role` varchar(50) DEFAULT NULL COMMENT '角色',\n" +
            "  `create_date` timestamp  NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "  `update_date` timestamp  NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',\n" +
            "  PRIMARY KEY (`uid`)\n" +
            ") ENGINE=InnoDB COMMENT ='账号和角色表';";

    /**
     * 每次 整体 测试前 建表、 插入数据
     */
    @BeforeClass
    public static void doBeforeClass() {
        BraveSql.build(Void.class).executeSql(dropUserRoleTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(createUserRoleTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(dropUserTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(createUserTable, DatabaseConfig.class);
        //插入数据
        //插入 user表，
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < tableDataRows; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setAccountNo("account_" + i);
            userEntity.setUsername(USER_NAME_LIST.get(RandomUtils.nextInt(0, USER_NAME_LIST.size() - 1)));
            userEntity.setBirthday(LocalDate.now().minusDays(RandomUtils.nextLong(100, 3000)));
            userEntity.setDelete(RandomUtils.nextBoolean());
            userEntity.setCreateDate(LocalDateTime.now());
            userEntity.setUpdateDate(LocalDateTime.now());
            userEntity.setDesc("　　锦瑟无端五十弦，一弦一柱思华年。\n" +
                    "　　庄生晓梦迷蝴蝶，望帝春心托杜鹃。\n" +
                    "　　沧海月明珠有泪，蓝田日暖玉生烟。\n" +
                    "　　此情可待成追忆，只是当时已惘然。");
            userEntity.setEmail("pengwz@hotmail.com");
            userEntity.setPassword(RandomUtils.nextInt(100000, 99999999) + "");
            userEntities.add(userEntity);
        }
        Integer integer = BraveSql.build(UserEntity.class).batchInsert(userEntities);
        assertEquals(integer.intValue(), tableDataRows);

        //插入 user role 表，
        List<UserRoleEntity> userRoleEntities = new ArrayList<>();
        for (int i = 0; i < tableDataRows; i++) {
            UserRoleEntity userRoleEntity = new UserRoleEntity();
            userRoleEntity.setAccountNo("account_" + i);
            userRoleEntity.setRole(ROLE_NAME_LIST.get(RandomUtils.nextInt(0, ROLE_NAME_LIST.size() - 1)));
            userRoleEntity.setCreateDate(LocalDateTime.now());
            userRoleEntity.setUpdateDate(LocalDateTime.now());
            userRoleEntities.add(userRoleEntity);
        }
        Integer integer2 = BraveSql.build(UserRoleEntity.class).batchInsert(userRoleEntities);
        assertEquals(integer2.intValue(), tableDataRows);
    }

    /**
     * 测试结束后删表
     */
//    @AfterClass
    public static void doAfterClass() {
        BraveSql.build(Void.class).executeSql(dropUserRoleTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(dropUserTable, DatabaseConfig.class);
    }

    /**
     * 测试 build 正常实体类
     */
    @Test
    public void testBuild() {
        BraveSql<UserEntity> build = BraveSql.build(UserEntity.class);
        Class<UserEntity> currentClass = build.getCurrentClass();
        assertEquals(currentClass, UserEntity.class);
    }

    /**
     * 测试 Void 正常实体类
     */
    @Test
    public void testBuild2() {
        BraveSql<Void> build = BraveSql.build(Void.class);
        Class<Void> currentClass = build.getCurrentClass();
        assertEquals(currentClass, Void.class);
    }

    /**
     * 测试 null 正常实体类
     */
    @Test
    public void testBuild3() {
        BraveSql<?> build = BraveSql.build(null);
        Class<?> currentClass = build.getCurrentClass();
        assertNull(currentClass);
    }


    @Test
    public void testGetDynamicSql() {
        assertNotNull(BraveSql.build(UserEntity.class).getDynamicSql());
    }

    /**
     * 测试 无条件查询100条数据
     */
    @Test
    public void testGetPageInfo() {
        PageInfo<UserEntity> pageInfo = BraveSql.build(UserEntity.class).selectPageInfo(100);
        assertEquals(pageInfo.getTotalSize().intValue(), tableDataRows);
        assertEquals(pageInfo.getPageSize().intValue(), 100);
    }

    public void testExecuteQuery() {
    }

    public void testTestExecuteQuery() {
    }

    public void testExecuteQuerySingle() {
    }

    public void testTestExecuteQuerySingle() {
    }

    public void testExecuteSql() {
    }

    public void testTestExecuteSql() {
    }

    public void testExistTable() {
    }

    public void testTestExistTable() {
    }

    public void testSelect() {
    }

    public void testSelectSingle() {
    }

    public void testSelectByPrimaryKey() {
    }

    public void testSelectCount() {
    }

    public void testSelectPageInfo() {
    }

    public void testTestSelectPageInfo() {
    }

    public void testInsert() {
    }

    public void testInsertActive() {
    }

    public void testBatchInsert() {
    }

    public void testInsertOrUpdate() {
    }

    public void testBatchInsertOrUpdate() {
    }

    public void testUpdate() {
    }

    public void testUpdateActive() {
    }

    public void testUpdateByPrimaryKey() {
    }

    public void testUpdateActiveByPrimaryKey() {
    }

    public void testDelete() {
    }

    public void testDeleteByPrimaryKey() {
    }

    public void testOrderByAsc() {
    }

    public void testTestOrderByAsc() {
    }

    public void testOrderByDesc() {
    }

    public void testTestOrderByDesc() {
    }
}