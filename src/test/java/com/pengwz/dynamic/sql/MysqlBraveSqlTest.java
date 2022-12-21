package com.pengwz.dynamic.sql;


import com.pengwz.dynamic.config.DatabaseConfig;
import com.pengwz.dynamic.entity.JobUserEntity;
import com.pengwz.dynamic.entity.UserEntity;
import com.pengwz.dynamic.entity.UserRoleEntity;
import com.pengwz.dynamic.entity.oracle.ActEvtLogEntity;
import com.pengwz.dynamic.exception.BraveException;
import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class MysqlBraveSqlTest {
    private static final Log log = LogFactory.getLog(MysqlBraveSqlTest.class);
    private static final List<String> ROLE_NAME_LIST = Arrays.asList("游客", "管理员", "超级管理员", "普通成员", "VIP成员", "穷B VIP（活动送的那种）");
    private static final List<String> USER_NAME_LIST = Arrays.asList("jerry", "tom", "王昭君", "妲己", "貂蝉", "李白", "亚瑟", "项羽", "程咬金", "刘邦", "韩信", "虞美人", "雷神",
            "小可nai", "天下无敌", "天下第一", "我就呵呵哒", "哎，女人呀", "致青春", "我的同桌", "小小", "夏天的风", "可可爱爱的昵称", "保家卫国", "敢于冲锋", "我就是我，颜色不一样的烟火");
    //单表插入数据量
    private static final int tableDataRows = 10_000;
    private static final String dropUserTable = "drop table if exists `t_user`;";
    private static final String dropUserRoleTable = "drop table if exists `t_user_role`;";
    private static final String dropJobUser1Table = "drop table if exists test2.job_user_1;";

    private static final String createUserTable = "CREATE TABLE `t_user` (\n" +
            "  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
            "  `account_no` varchar(100) DEFAULT NULL COMMENT '账号',\n" +
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
    private static final String createJobUser1Table = "CREATE TABLE `test2`.`job_user_1` (\n" +
            "  `id` int NOT NULL AUTO_INCREMENT,\n" +
            "  `username` varchar(100) DEFAULT NULL,\n" +
            "  `password` varchar(100) DEFAULT NULL,\n" +
            "  `role` varchar(100) DEFAULT NULL,\n" +
            "  `permission` varchar(700) DEFAULT NULL,\n" +
            "   `times` time DEFAULT NULL,\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci";

    /**
     * 每次 整体 测试前 建表、 插入数据
     */
    @BeforeClass
    public static void doBeforeClass() {
        BraveSql.build(Void.class).executeSql(dropUserRoleTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(createUserRoleTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(dropUserTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(createUserTable, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(dropJobUser1Table, DatabaseConfig.class);
        BraveSql.build(Void.class).executeSql(createJobUser1Table, DatabaseConfig.class);
        //插入数据
        //插入 user表，
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < tableDataRows; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setAccountNo("account_" + i);
            userEntity.setUsername(USER_NAME_LIST.get(RandomUtils.nextInt(0, USER_NAME_LIST.size() - 1)));
            userEntity.setBirthday(LocalDate.now().minusDays(RandomUtils.nextLong(100, 3000)));
            userEntity.setIsDelete(RandomUtils.nextBoolean());
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
    @AfterClass
    public static void doAfterClass() {
//        BraveSql.build(Void.class).executeSql(dropUserRoleTable, DatabaseConfig.class);
//        BraveSql.build(Void.class).executeSql(dropUserTable, DatabaseConfig.class);
//        BraveSql.build(Void.class).executeSql(dropUserTable, DatabaseConfig.class);
//        BraveSql.build(Void.class).executeSql(dropJobUser1Table, DatabaseConfig.class);
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

    /**
     * 主要测试数字类型
     */
    @Test
    public void testExecuteQuery() {
        //TODO 当执行sql未指定 数据源时，不应该直接抛出空指针   需要补充异常文案
        List<UserEntity> userEntities = BraveSql.build(UserEntity.class).executeQuery("select id from t_user limit 1000", DatabaseConfig.class);
        assertEquals(userEntities.size(), 1000);
        List<Long> longs = BraveSql.build(Long.class).executeQuery("select id from t_user where id = 1", DatabaseConfig.class);
        assertEquals(longs.size(), 1);
        List<Double> doubles = BraveSql.build(Double.class).executeQuery("select id from t_user where id = 1", DatabaseConfig.class);
        assertEquals(doubles.size(), 1);
        List<Integer> integers = BraveSql.build(Integer.class).executeQuery("select id from t_user where id <= 100", DatabaseConfig.class);
        assertEquals(integers.size(), 100);
        List<BigInteger> bigIntegers = BraveSql.build(BigInteger.class).executeQuery("select id from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(bigIntegers.size(), 500);
    }

    /**
     * 主要测试数字类型
     */
    @Test(expected = BraveException.class)
    public void testExecuteQuery2() {
        List<BigDecimal> bigDecimals = BraveSql.build(BigDecimal.class).executeQuery("select id from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(bigDecimals.size(), 500);
        List<AtomicInteger> atomicIntegers = BraveSql.build(AtomicInteger.class).executeQuery("select id from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(atomicIntegers.size(), 500);
        List<AtomicLong> atomicLongs = BraveSql.build(AtomicLong.class).executeQuery("select id from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(atomicLongs.size(), 500);
    }

    /**
     * 主要测试Object类型
     */
    @Test
    public void testExecuteQuery3() {
        //TODO : 此处待优化 应该将Object类排除【映射实体类未发现可用属性，发生在类：java.lang.Object】
        List<Object> objects = BraveSql.build(Object.class).executeQuery("select id from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(objects.size(), 500);
    }

    /**
     * 主要测试 日期 类型
     */
    @Test
    public void testExecuteQuery4() {
        List<java.util.Date> dates = BraveSql.build(java.util.Date.class).executeQuery("select create_date from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(dates.size(), 500);
        List<java.sql.Date> dates1 = BraveSql.build(java.sql.Date.class).executeQuery("select create_date from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(dates1.size(), 500);
        List<LocalDateTime> localDateTimes = BraveSql.build(LocalDateTime.class).executeQuery("select create_date from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(localDateTimes.size(), 500);
        List<LocalDate> localDates = BraveSql.build(LocalDate.class).executeQuery("select create_date from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(localDates.size(), 500);
        List<LocalTime> localTimes = BraveSql.build(LocalTime.class).executeQuery("select create_date from t_user where id <= 500", DatabaseConfig.class);
        assertEquals(localTimes.size(), 500);
    }

    @Test
    public void testExecuteQuerySingle() {
        String single = BraveSql.build(String.class).executeQuerySingle("select `desc` from t_user where id = 1", DatabaseConfig.class);
        assertNotNull(single);
        UserEntity userEntity = BraveSql.build(UserEntity.class).executeQuerySingle("select `desc` from t_user where id = 1", DatabaseConfig.class);
        assertNotNull(userEntity.getDesc());
    }

    @Test
    public void testExecuteSql() {
        //增 查 改 删
        //增---------------------
        BraveSql.build(Void.class).executeSql("insert into t_user (username,account_no) values ('testExecuteSql','testExecuteSql')", DatabaseConfig.class);
        //判断是否插入成功
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getAccountNo, "testExecuteSql");
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        assertNotNull(userEntity);
        //查 ---------------------(使用此API查询没有意义，此处仅做测试)
        BraveSql.build(Void.class).executeSql("select * from t_user", DatabaseConfig.class);
        //改--------------------- 将username改成 哈哈哈哈哈哈哈哈
        BraveSql.build(Void.class).executeSql("update t_user set username = '哈哈哈哈哈哈哈哈' where account_no = 'testExecuteSql'", DatabaseConfig.class);
        UserEntity userEntity2 = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        assertEquals(userEntity2.getUsername(), "哈哈哈哈哈哈哈哈");
        //删---------------------
        BraveSql.build(Void.class).executeSql("delete  from t_user where account_no = 'testExecuteSql'", DatabaseConfig.class);
        //判断是否删除完毕
        UserEntity userEntity3 = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        assertNull(userEntity3);
    }

//    @Test
//    public void testExecuteSql2() {
//        DynamicSql<UserChildEntity> dynamicSql = DynamicSql.createDynamicSql();
//        dynamicSql.andLessThan(UserChildEntity::getId, 50);
//        List<UserChildEntity> select = BraveSql.build(dynamicSql, UserChildEntity.class).select();
//        System.out.println(select.size());
//        select.forEach(System.out::println);
//    }

    @Test(expected = BraveException.class)
    public void testTestExecuteSql() {
        BraveSql.build(UserEntity.class).executeQuery("");
    }

    public void testExistTable() {
    }

    public void testTestExistTable() {
    }

    @Test
    public void testSelect() {
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        System.out.println(select.size());
        System.out.println(select.subList(0, 1));
    }

    @Test
    public void testSelect2() {
        List<JobUserEntity> select = BraveSql.build(JobUserEntity.class).select();
        System.out.println(select.size());
        Assert.assertTrue(select.size() > 1);
    }


    @Test
    public void testSelectSingle() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, 1);
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        System.out.println(userEntity);
    }

    @Test
    public void testSelectByPrimaryKey() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        System.out.println(userEntity);
    }

    @Test
    public void testSelectCount() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThan(UserEntity::getId, 0);
        Integer integer = BraveSql.build(dynamicSql, UserEntity.class).selectCount();
        System.out.println(integer);
        Integer integer2 = BraveSql.build(UserEntity.class).selectCount();
        System.out.println(integer2);
    }

    @Test
    public void testSelectPageInfo() {
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(3);
        System.out.println(userEntityPageInfo);
        System.out.println("===================================");
        PageInfo<UserEntity> userEntityPageInfo2 = BraveSql.build(UserEntity.class).selectPageInfo(0);
        System.out.println(userEntityPageInfo2);
    }

    public void testTestSelectPageInfo() {
    }

    @Test
    public void testInsert() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        userEntity.setId(null);
        userEntity.setAccountNo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Integer integer = BraveSql.build(UserEntity.class).insertActive(userEntity);
        System.out.println(integer);
    }

    @Test
    public void testInsertActive() {
        final UserEntity userEntity = new UserEntity();
        userEntity.setAccountNo("peng");
        long id = 111111L;
        userEntity.setId(id);
        BraveSql.build(UserEntity.class).insertActive(userEntity);
        System.out.println(userEntity);
        System.out.println(BraveSql.build(UserEntity.class).selectByPrimaryKey(id));
    }

    @Test
    public void testBatchInsert() {
        BraveSql.build(UserEntity.class).delete();
        final ArrayList<UserEntity> objects = new ArrayList<>();
        for (long i = 0; i < 10; i++) {
            final UserEntity userEntity = new UserEntity();
            userEntity.setAccountNo("peng" + i);
            if (i == 5) {
                userEntity.setId(null);
            } else {
                userEntity.setId(i);
            }
            objects.add(userEntity);
        }
        System.out.println(objects);
        BraveSql.build(UserEntity.class).batchInsert(objects);
        System.out.println(objects);

    }

    @Test
    public void testBrackets() {
        final DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getIsDelete, Boolean.FALSE);
//        dynamicSql.andBetween(UserEntity::getId, 1, 111111);
        dynamicSql.startBrackets();
        dynamicSql.andEqualTo(UserEntity::getAccountNo, "peng");
        dynamicSql.endBrackets();
        final List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
        System.out.println(select);

    }


    public void testInsertOrUpdate() {
    }

    public void testBatchInsertOrUpdate() {
    }

    public void testUpdate() {
    }

    @Test
    public void testUpdateActive() {
        DynamicSql<JobUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(JobUserEntity::getId, 0);
        JobUserEntity jobUserEntity = new JobUserEntity();
        jobUserEntity.setId(0);
        Integer integer = BraveSql.build(dynamicSql, JobUserEntity.class).updateActive(jobUserEntity);
        System.out.println(integer);

    }

    public void testUpdateByPrimaryKey() {
    }

    public void testUpdateActiveByPrimaryKey() {
    }

    public void testDelete() {
    }

    public void testDeleteByPrimaryKey() {
    }

    @Test
    public void testOrderByAsc() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.orderByAsc(UserEntity::getId);
        List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
        assertEquals(select.get(0).getId().intValue(), 1);
        assertEquals(select.get(select.size() - 1).getId().intValue(), tableDataRows);
    }

    @Test
    public void testTestOrderByAsc() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.orderByDesc("id");
        List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
        assertEquals(select.get(0).getId().intValue(), tableDataRows);
        assertEquals(select.get(select.size() - 1).getId().intValue(), 1);
    }

    @Test
    public void testTestOrderByAsc2() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThan(UserEntity::getId, 200);
        dynamicSql.orderByDesc(UserEntity::getUsername)/*.thenOrderByAsc(UserEntity::getId).thenOrderByAsc(UserEntity::getId).thenOrderByAsc(UserEntity::getId).thenOrderByAsc(UserEntity::getId)*/;
        dynamicSql.orderByAsc(UserEntity::getId);
        dynamicSql.orderByDesc(UserEntity::getId);
        dynamicSql.orderByDesc(UserEntity::getId);
        dynamicSql.orderByAsc(UserEntity::getId);
        List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
        select.forEach(System.out::println);
    }

    public void testOrderByDesc() {
    }

    public void testTestOrderByDesc() {
    }

    //临时
    @Test
    public void testJobUserEntity() {
        JobUserEntity jobUserEntity = BraveSql.build(JobUserEntity.class).selectByPrimaryKey(11);
        JobUserEntity jobUserEntity2 = new JobUserEntity();
        jobUserEntity2.setPassword("12121");
//        jobUserEntity2.setRole("role");
        jobUserEntity2.setUsername("hello3");
        jobUserEntity2.setPermission(jobUserEntity);
        Integer integer = BraveSql.build(JobUserEntity.class).insertActive(jobUserEntity2);
        System.out.println(integer);
    }

    @Test
    public void sum() {
        System.out.println(BraveSql.build(JobUserEntity.class).selectSum("id"));
        DynamicSql<JobUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(JobUserEntity::getId, 0);
        System.out.println(BraveSql.build(dynamicSql, JobUserEntity.class).selectSum("id"));
    }

    @Test
    public void testJsonReadAndWrite() {
//        ConverterUtils.putConverterAdapter(JobUserEntity.class, new JobUserEntityConverterAdapter());
        BraveSql.build(JobUserEntity.class).delete();
        final JobUserEntity jobUserEntity = new JobUserEntity();
        jobUserEntity.setPassword("13720002902");
        jobUserEntity.setUsername("pengwz");
        jobUserEntity.setRole("admin");
        jobUserEntity.setPermission(jobUserEntity);
        jobUserEntity.setTimes(LocalTime.now());
        BraveSql.build(JobUserEntity.class).insertActive(jobUserEntity);
        final JobUserEntity selectSingle = BraveSql.build(JobUserEntity.class).select().get(0);
        log.info(selectSingle);
        assertNotNull(selectSingle);
    }

    @Test
    public void testMaxAndMin() {
        BraveSql.build(JobUserEntity.class).delete();
        for (int i = 0; i < 10; i++) {
            final JobUserEntity jobUserEntity = new JobUserEntity();
            jobUserEntity.setPassword("13720002902");
            jobUserEntity.setUsername("pengwz");
            jobUserEntity.setRole("admin");
            jobUserEntity.setPermission(jobUserEntity);
            jobUserEntity.setTimes(LocalTime.now().minusHours(RandomUtils.nextInt(1, 10)));
            BraveSql.build(JobUserEntity.class).insertActive(jobUserEntity);
        }
//        ConverterUtils.putConverterAdapter(LocalTime.class, new LocalTimeConverterAdapter());
        final BigDecimal bigDecimal = BraveSql.build(JobUserEntity.class).selectMax(JobUserEntity::getId);
        log.info("selectMax(JobUserEntity::getId) = " + bigDecimal);
        final BigDecimal bigDecimal2 = BraveSql.build(JobUserEntity.class).selectMin(JobUserEntity::getId);
        log.info("selectMin(JobUserEntity::getId) = " + bigDecimal2);
        final LocalTime localTime = BraveSql.build(JobUserEntity.class).selectMax(JobUserEntity::getTimes, LocalTime.class);
        log.info("selectMax(JobUserEntity::getTimes) = " + localTime);
        final LocalTime localTime1 = BraveSql.build(JobUserEntity.class).selectMin(JobUserEntity::getTimes, LocalTime.class);
        log.info("selectMin(JobUserEntity::getTimes) = " + localTime1);
        final Double aDouble = BraveSql.build(JobUserEntity.class).selectAvg(JobUserEntity::getId, Double.class);
        log.info("selectAvg(JobUserEntity::getId) = " + aDouble);
        final LocalTime localTime2 = BraveSql.build(JobUserEntity.class).selectAvg(JobUserEntity::getTimes, LocalTime.class);
        log.info("selectAvg(JobUserEntity::getTimes) = " + localTime2);
        final LocalDateTime LocalDateTime = BraveSql.build(JobUserEntity.class).selectAvg(JobUserEntity::getTimes, LocalDateTime.class);
        log.info("selectAvg(JobUserEntity::getTimes) = " + LocalDateTime);
        final LocalDate LocalDate = BraveSql.build(JobUserEntity.class).selectAvg(JobUserEntity::getTimes, LocalDate.class);
        log.info("selectAvg(JobUserEntity::getTimes) = " + LocalDate);
    }

//    public static class LocalTimeConverterAdapter implements ConverterAdapter<LocalTime> {
//
//        @Override
//        public LocalTime converter(Class<?> entityClass, Class<LocalTime> fieldClass, Object columnValue) {
//            return null;
//        }
//    }
//
//    public static class JobUserEntityConverterAdapter implements ConverterAdapter<JobUserEntity> {
//
//        @Override
//        public JobUserEntity converter(Class<?> entityClass, Class<JobUserEntity> fieldClass, Object columnValue) {
//            return null;
//        }
//    }

    /**
     * 测试SQL注入
     */
    @Test
    public void testSQLInjection_selectByPrimaryKey() {
        BraveSql.build(JobUserEntity.class).delete();
        final JobUserEntity jobUserEntity = new JobUserEntity();
        jobUserEntity.setTimes(LocalTime.now());
        jobUserEntity.setUsername("pengwz");
        jobUserEntity.setId(999);
        BraveSql.build(JobUserEntity.class).insertActive(jobUserEntity);
        /////////////////
        final JobUserEntity jobUserEntity1 = BraveSql.build(JobUserEntity.class).selectByPrimaryKey(999);
        System.out.println(jobUserEntity1);

    }

    /**
     * 测试SQL注入
     */
    @Test
    public void testSQLInjection_selectSingle() {
        BraveSql.build(JobUserEntity.class).delete();
        final JobUserEntity jobUserEntity = new JobUserEntity();
        jobUserEntity.setTimes(LocalTime.now());
        jobUserEntity.setUsername("pengwz");
        jobUserEntity.setId(999);
        BraveSql.build(JobUserEntity.class).insertActive(jobUserEntity);
        /////////////////
        final DynamicSql<JobUserEntity> dynamicSql = DynamicSql.createDynamicSql();
//        dynamicSql.andEqualTo(JobUserEntity::getUsername, "' or ( 1=1) or '' = '");
        dynamicSql.andEqualTo(JobUserEntity::getUsername, "\\'");
        final JobUserEntity jobUserEntity1 = BraveSql.build(dynamicSql, JobUserEntity.class).selectSingle();
        System.out.println(jobUserEntity1);
        assertNull(jobUserEntity1);
    }

    //    @Test
    public void testSQLInjection_selectSingle2() {
        BraveSql.build(ActEvtLogEntity.class).delete();

        ActEvtLogEntity entity = new ActEvtLogEntity();
        entity.setTaskId("任务");
        entity.setLogNr(1);
        entity.setTimeStamp(LocalDateTime.now());
        BraveSql.build(ActEvtLogEntity.class).insertActive(entity);
        final List<ActEvtLogEntity> select = BraveSql.build(ActEvtLogEntity.class).select();
        System.out.println(select);

        final DynamicSql<ActEvtLogEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(ActEvtLogEntity::getTaskId, "任务1'");
        final List<ActEvtLogEntity> select1 = BraveSql.build(dynamicSql, ActEvtLogEntity.class).select();
        System.out.println("select1 = =" + select1);

    }

}
