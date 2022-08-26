package com.pengwz.dynamic.sql.base;

import com.google.gson.Gson;
import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.TestDatabaseConfig;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.interceptor.SQLInterceptor;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.utils.InterceptorHelper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pengwz.dynamic.anno.GenerationType.AUTO;

/**
 * mysql 测试用例
 */
@Slf4j
public class SqlsTest {

    private static String dropTableUser = "drop table if exists `t_mysql_user`;";
    private static String dropTableUser2 = "drop table if exists `t_mysql_user2`;";
    private static String truncateTableUser = "truncate  `t_mysql_user`;";


    private static final String createTableUser = "create table `t_mysql_user` ( `id` bigint(20) not null auto_increment COMMENT '主键',\n" +
            "`account_no` varchar(100) default null COMMENT '账号',\n" +
            "`username` varchar(50) default null COMMENT '用户名',\n" +
            "`password` varchar(50) default null COMMENT '密码',\n" +
            "`gender` enum('男','女','未知') default null COMMENT '性别',\n" +
            " `date_of_birth` date default null COMMENT '出生日期',\n" +
            " `time_of_birth` time default null COMMENT '出生时间',\n" +
            " `hobby` json default null COMMENT '爱好',\n" +
            "`desc` varchar(5000) default null COMMENT '描述',\n" +
            "`is_deleted` tinyint(1) default null COMMENT '是否删除 true 已删除 false 未删除',\n" +
            "`create_date` timestamp null default CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "`update_date` timestamp null default CURRENT_TIMESTAMP on update\tCURRENT_TIMESTAMP COMMENT '修改时间',\t\n" +
            "primary key (`id`),\tunique key `t_user_UN` (`account_no`) ) ENGINE = InnoDB COMMENT = 'Mysql用户表';";
    private static final String createTableUser2 = "   create table `t_mysql_user2` ( `id` bigint(20) not null COMMENT '主键',\n" +
            "            `account_no` varchar(100) default null COMMENT '账号',\n" +
            "            `username` varchar(50) default null COMMENT '用户名',\n" +
            "            `password` varchar(50) default null COMMENT '密码',\n" +
            "            unique key `t_user_UN` (`account_no`) ) ENGINE = InnoDB COMMENT = 'Mysql2用户表';";

    /**
     * step1 全局测试前，先创建表
     */
    @BeforeClass
    public static void doBeforeClass() {
        BraveSql.executeSql(dropTableUser, TestDatabaseConfig.class);
        BraveSql.executeSql(dropTableUser2, TestDatabaseConfig.class);
        BraveSql.executeSql(createTableUser, TestDatabaseConfig.class);
        BraveSql.executeSql(createTableUser2, TestDatabaseConfig.class);
    }

    /**
     * step2 单元测试前，先执行此步，创建100条数据
     */
    @Before
    public void doBefore() {
        List<MysqlUserEntity> list = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            final HobbyEntity hobbyEntity = HobbyEntity.builder()
                    .sing(true)
                    .maxDouble(Double.MAX_VALUE)
                    .maxLong(Long.MAX_VALUE)
                    .playBasketball(false)
                    .dance(null)
                    .build();
            final MysqlUserEntity mysqlUserEntity = MysqlUserEntity.builder()
                    .accountNo("账号：" + i)
                    .gender(GenderEnum.未知)
                    .hobby(hobbyEntity)
                    .desc(hobbyEntity)
                    .dateOfBirth(LocalDate.now().minusDays(i))
                    .createDate(LocalDateTime.now())
                    .updateDate(new Date())
                    .timeOfBirth(LocalTime.now())
                    .isDeleted(Boolean.FALSE)
                    .build();
            if (i % 200 == 0) {
                mysqlUserEntity.setGender(null);
                mysqlUserEntity.setDesc(null);
                mysqlUserEntity.setAccountNo(null);
                mysqlUserEntity.setDateOfBirth(LocalDate.now());
            }
            list.add(mysqlUserEntity);
        }
        final Integer integer = BraveSql.build(MysqlUserEntity.class).batchInsert(list);
        log.info("成功创建的条数：{}", integer);
    }

    /**
     * step3 单元测试后，删除数据
     */
    @After
    public void doAfter() {
        BraveSql.executeSql(truncateTableUser, TestDatabaseConfig.class);
    }

    @Test
    public void selectIsNull() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIsNull(MysqlUserEntity::getDesc);
        dynamicSql.andIsNull(MysqlUserEntity::getGender);
        dynamicSql.andIsNull(MysqlUserEntity::getAccountNo);
        dynamicSql.andBetween(MysqlUserEntity::getId, 1, 100000);
        final List<MysqlUserEntity> select = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        select.forEach(System.out::println);
    }

    @Test
    public void selectByPrimaryKey() {
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(MysqlUserEntity.class).selectByPrimaryKey(1);
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试SQL注入
     */
    @Test
    public void selectByPrimaryKey2() {
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(MysqlUserEntity.class).selectByPrimaryKey("0 or 1 = 1");
        Assert.assertNull(mysqlUserEntity);
    }

    @Test
    public void selectSingle() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试重复ID
     */
    @Test
    public void selectSingle2() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(MysqlUserEntity::getId, Arrays.asList(1, 1, 1, 1, 1, 1));
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试重复查询条件
     */
    @Test
    public void selectSingle3() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(MysqlUserEntity::getId, Arrays.asList(1, 1, 1, 1, 1, 1));
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试重复查询条件 + 常量入参
     */
    @Test
    public void selectSingle4() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(MysqlUserEntity::getId, Arrays.asList(1, 1, 1, 1, 1, 1));
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        dynamicSql.andEqualTo(MysqlUserEntity::getGender, "未知");
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试重复查询条件 + 枚举入参
     */
    @Test
    public void selectSingle5() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(MysqlUserEntity::getId, Arrays.asList(1, 1, 1, 1, 1, 1));
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        dynamicSql.andEqualTo(MysqlUserEntity::getGender, GenderEnum.未知);
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试重复查询条件 + 集合枚举入参
     */
    @Test
    public void selectSingle6() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(MysqlUserEntity::getId, Arrays.asList(1, 1, 1, 1, 1, 1));
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        dynamicSql.andIn(MysqlUserEntity::getGender, Arrays.asList(GenderEnum.未知, GenderEnum.未知, GenderEnum.未知));
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNotNull(mysqlUserEntity);
        log.info("mysqlUserEntity : {}", mysqlUserEntity);
    }

    /**
     * 测试查询条件为年月日
     */
    @Test
    public void selectSingle7() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getDateOfBirth, LocalDate.now());
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertFalse(entities.isEmpty());
        log.info("entities :{}", entities);
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试查询条件为多个年月日
     */
    @Test
    public void selectSingle8() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(MysqlUserEntity::getDateOfBirth, Arrays.asList(LocalDate.now(), LocalDate.now(), LocalDate.now(), LocalDate.now()));
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertFalse(entities.isEmpty());
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试查询条件为字符串年月日
     */
    @Test
    public void selectSingle9() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        final String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        dynamicSql.andEqualTo(MysqlUserEntity::getDateOfBirth, format);
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertFalse(entities.isEmpty());
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试查询条件为多个字符串年月日
     */
    @Test
    public void selectSingle10() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        final String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        dynamicSql.andIn(MysqlUserEntity::getDateOfBirth, Arrays.asList(format, format, format, format, format));
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertFalse(entities.isEmpty());
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试查询条件为时分秒
     */
    @Test
    public void selectSingle11() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(MysqlUserEntity::getDateOfBirth, LocalDate.now());
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertEquals(entities.size(), 1000);
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试查询条件为时分秒2
     */
    @Test
    public void selectSingle12() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThan(MysqlUserEntity::getDateOfBirth, LocalDate.now());
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertEquals(entities.size(), 995);
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试多条件多日期入参
     */
    @Test
    public void select() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThan(MysqlUserEntity::getDateOfBirth, LocalDate.now());
        dynamicSql.startBrackets();
        dynamicSql.andLike(MysqlUserEntity::getGender, "未%");
        dynamicSql.endBrackets();
        dynamicSql.andLike(MysqlUserEntity::getCreateDate, LocalDate.now() + "%");
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertEquals(entities.size(), 995);
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 测试JSON条件入参
     */
    @Test
    public void select2() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        String json = "{\"sing\": true, \"maxLong\": 9223372036854775807, \"maxDouble\": 1.7976931348623157e308, \"playBasketball\": false}";
        final Gson gson = new Gson();
        final HobbyEntity hobbyEntity = gson.fromJson(json, HobbyEntity.class);
        //执行的字符串json
        dynamicSql.andEqualTo(MysqlUserEntity::getDesc, hobbyEntity);
        final List<MysqlUserEntity> entities = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        Assert.assertEquals(entities.size(), 995);
        log.info("entities.size() :{}", entities.size());
    }

    /**
     * 聚合函数，查询总数量
     */
    @Test
    public void selectAggregateFunction() {
        final Integer integer = BraveSql.build(MysqlUserEntity.class).selectCount();
        Assert.assertEquals(integer.intValue(), 1000);
        log.info("selectCount :{}", integer);
    }

    /**
     * 聚合函数，根据条件查询总数量
     */
    @Test
    public void selectAggregateFunction2() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andBetween(MysqlUserEntity::getId, 1, 100);
        final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectCount();
        Assert.assertEquals(integer.intValue(), 100);
        log.info("selectCount :{}", integer);
    }

    /**
     * 聚合函数，根据条件查询平均数
     */
    @Test
    public void selectAggregateFunction3() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andBetween(MysqlUserEntity::getId, 1, 100);
        final Double aDouble = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectAvg(MysqlUserEntity::getId, Double.class);
        Assert.assertTrue(aDouble > 40);
        log.info("selectAvg :{}", aDouble);
    }

    /**
     * 聚合函数，根据条件查询平均数(浮点数转整数)
     */
    @Test
    public void selectAggregateFunction4() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andBetween(MysqlUserEntity::getId, 1, 100);
        final Number number = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectAvg(MysqlUserEntity::getId, Integer.class);
        Assert.assertTrue(number.doubleValue() > 40);
        log.info("selectAvg :{}", number);
    }

    /**
     * 聚合函数，根据条件查询最大值
     */
    @Test
    public void selectAggregateFunction5() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andBetween(MysqlUserEntity::getId, 1, 100);
        final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectMax(MysqlUserEntity::getId, Integer.class);
        Assert.assertEquals(100, (int) integer);
        log.info("selectMax :{}", integer);
    }

    @Test
    public void selectAll() {
        final List<MysqlUserEntity> entities = BraveSql.build(MysqlUserEntity.class).select();
        Assert.assertEquals(entities.size(), 1000);
        log.info("entities.size() :{}", entities.size());
    }


    @Test(expected = BraveException.class)
    public void selectPageInfo() {
        final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(-1, -1);
    }

    @Test
    public void selectPageInfo2() {
        final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(-1, 0);
        Assert.assertTrue(pageInfo.getResultList().isEmpty());
        log.info("pageInfo: {}", pageInfo);
    }

    @Test
    public void selectPageInfo3() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(MysqlUserEntity::getId, 456);
        final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectPageInfo(0, 1);
        final List<MysqlUserEntity> resultList = pageInfo.getResultList();
        Assert.assertTrue(resultList.size() == 1);
        Assert.assertTrue(pageInfo.getTotalSize() == 456);
        log.info("pageInfo: {}", pageInfo);
    }

    @Test
    public void batchInsert() {
        final MysqlUserEntity build = MysqlUserEntity.builder().gender(GenderEnum.女).build();
        final Integer integer = BraveSql.build(MysqlUserEntity.class).batchInsert(Collections.singleton(build));
        Assert.assertEquals(integer.intValue(), 1);
    }

    /**
     * 测试插入对象，null
     */
    @Test
    public void insertActive() {
        final Integer integer = BraveSql.build(MysqlUserEntity.class).insertActive(null);
        Assert.assertEquals(integer.intValue(), 0);
    }

    /**
     * 测试插入空对象，实体类包含主键值
     */
    @Test
    public void insertActive2() {
        final Integer integer = BraveSql.build(MysqlUserEntity.class).insertActive(MysqlUserEntity.builder().build());
        log.info("测试包含主键的时候，插入空对象，期待返回1");
        Assert.assertEquals(integer.intValue(), 1);
    }

    /**
     * 测试插入空对象，实体类不包含主键
     */
    @Test
    public void insertActive3() {
        final Integer integer = BraveSql.build(MysqlUserEntity2.class).insertActive(MysqlUserEntity2.builder().build());
        log.info("测试不包含主键的时候，插入空对象，期待返回0");
        Assert.assertEquals(integer.intValue(), 0);
    }

    /**
     * 测试更新
     */
    @Test
    public void insertOrUpdate() {
        final MysqlUserEntity build = MysqlUserEntity.builder().build();
        build.setAccountNo("update");
        build.setId(1);
        build.setGender(GenderEnum.男);
        final Integer integer = BraveSql.build(MysqlUserEntity.class).insertOrUpdate(build);
        Assert.assertEquals(integer.intValue(), 1);
    }

    /**
     * 测试新增
     */
    @Test
    public void insertOrUpdate2() {
        final MysqlUserEntity build = MysqlUserEntity.builder().build();
        build.setAccountNo("insert");
        build.setId(1001);
        build.setGender(GenderEnum.男);
        final Integer integer = BraveSql.build(MysqlUserEntity.class).insertOrUpdate(build);
        Assert.assertEquals(integer.intValue(), 1);
    }

    @Test
    public void update() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        dynamicSql.andEqualTo(MysqlUserEntity::getAccountNo, "账号：0");
        final MysqlUserEntity build = MysqlUserEntity.builder().id(1).accountNo("accountNo").build();
        BraveSql.build(dynamicSql, MysqlUserEntity.class).update(build);
    }

    @Test
    public void updateActive() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getAccountNo, "账号：0");
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        final MysqlUserEntity build = MysqlUserEntity.builder().id(1).accountNo("accountNo_updateActive").build();
        BraveSql.build(dynamicSql, MysqlUserEntity.class).updateActive(build);
        final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
        Assert.assertNull(mysqlUserEntity);
    }

    /**
     * 测试包含主键的更新
     */
    @Test
    public void updateByPrimaryKey() {
        final MysqlUserEntity build = MysqlUserEntity.builder().id(1).accountNo("updateByPrimaryKey").build();
        BraveSql.build(MysqlUserEntity.class).updateByPrimaryKey(build);
    }

    /**
     * 测试不包含主键的更新
     */
    @Test(expected = BraveException.class)
    public void updateByPrimaryKey2() {
        final MysqlUserEntity build = MysqlUserEntity.builder().accountNo("updateByPrimaryKey").build();
        BraveSql.build(MysqlUserEntity.class).updateByPrimaryKey(build);
    }

    @Test
    public void updateActiveByPrimaryKey() {
        final MysqlUserEntity build = MysqlUserEntity.builder().id(1).accountNo("updateActiveByPrimaryKey").build();
        final Integer integer = BraveSql.build(MysqlUserEntity.class).updateActiveByPrimaryKey(build);
        Assert.assertEquals(integer.intValue(), 1);
    }

    @Test(expected = BraveException.class)
    public void updateActiveByPrimaryKey2() {
        final MysqlUserEntity build = MysqlUserEntity.builder().accountNo("updateActiveByPrimaryKey").build();
        final Integer integer = BraveSql.build(MysqlUserEntity.class).updateActiveByPrimaryKey(build);
    }

    @Test
    public void updateActiveByPrimaryKey3() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        final MysqlUserEntity build = MysqlUserEntity.builder().id(1).accountNo("updateActiveByPrimaryKey").build();
        final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).updateActiveByPrimaryKey(build);
        Assert.assertEquals(integer.intValue(), 1);
    }

    /**
     * 测试删除全表数据
     */
    @Test
    public void delete() {
        BraveSql.build(MysqlUserEntity.class).delete();
    }

    /**
     * 测试删除ID等于1的数据
     */
    @Test
    public void delete2() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getId, 1);
        final Integer delete = BraveSql.build(dynamicSql, MysqlUserEntity.class).delete();
        Assert.assertEquals(delete.intValue(), 1);
    }

    /**
     * 测试删除ID小于700的数据
     */
    @Test
    public void delete3() {
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThan(MysqlUserEntity::getId, 700);
        final Integer delete = BraveSql.build(dynamicSql, MysqlUserEntity.class).delete();
        Assert.assertEquals(delete.intValue(), 699);
    }


    @Test
    public void deleteByPrimaryKey() {
        final Integer delete = BraveSql.build(MysqlUserEntity.class).deleteByPrimaryKey(1);
        Assert.assertEquals(delete.intValue(), 1);
    }

    /**
     * 测试SQL拦截器
     */
    @Test
    public void testSQLInterceptor() {
        final List<MysqlUserEntity> entities = BraveSql.build(MysqlUserEntity.class).select();
        Assert.assertEquals(entities.size(), 1000);
        log.info("entities.size() :{}", entities.size());
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andGreaterThanOrEqualTo(MysqlUserEntity::getId, 900);
        final List<MysqlUserEntity> select = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        log.info("select.size() :{}", select.size());
    }

    /**
     * 测试SQL拦截器,测试SQL不执行的情况
     */
    @Test
    public void testSQLInterceptor2() {
//        InterceptorHelper.initSQLInterceptor(new FalseSQLInterceptor());
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        //设置一个类型不匹配的错误
        dynamicSql.andGreaterThanOrEqualTo(MysqlUserEntity::getId, null);
        final List<MysqlUserEntity> select = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        log.info("select.size() :{}", select.size());
        Assert.assertEquals(0, select.size());
    }

    /**
     * 测试SQL拦截器,测试SQL执行的情况
     */
    @Test
    public void testSQLInterceptor3() {
        InterceptorHelper.initSQLInterceptor(new CustomSQLInterceptor());
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        //设置一个类型不匹配的错误
        dynamicSql.andGreaterThanOrEqualTo(MysqlUserEntity::getId, 300);
        final List<MysqlUserEntity> select = BraveSql.build(dynamicSql, MysqlUserEntity.class).select();
        log.info("select.size() :{}", select.size());
        Assert.assertEquals(701, select.size());
    }

    /**
     * 测试SQL拦截器,测试SQL报错
     */
    @Test(expected = BraveException.class)
    public void testSQLInterceptor4() {
        InterceptorHelper.initSQLInterceptor(new CustomSQLInterceptor());
        final AtomicInteger atomicInteger = BraveSql.build(MysqlUserEntity.class).selectAvg(MysqlUserEntity::getId, AtomicInteger.class);
        System.out.println(atomicInteger);
    }

    /**
     * 测试SQL拦截器,重复插入测试SQL报错
     */
    @Test(expected = BraveException.class)
    public void testSQLInterceptor5() {
        InterceptorHelper.initSQLInterceptor(new CustomSQLInterceptor());
        final MysqlUserEntity build = MysqlUserEntity.builder().id(9999).accountNo("9999").build();
        BraveSql.build(MysqlUserEntity.class).batchInsert(Arrays.asList(build, build, build));
    }

    @Test
    public void testTryCatch() {
        execute(false);
    }

    /**
     * 测试连接泄露
     */
    @Test
    public void testConnection() {
        for (int i = 0; i < 1000; i++) {
            System.out.println("开始测试连接 " + i);
            final Integer integer = BraveSql.build(MysqlUserEntity.class).updateActive(MysqlUserEntity.builder().build());
            System.out.println(integer);
        }
    }

    /**
     * 测试 json的曾删改查
     */
    @Test
    public void testJson() {
        final MysqlUserEntity entity = new MysqlUserEntity();
        HobbyEntity desc = new HobbyEntity();
        desc.setMaxDouble(12.0);
        entity.setDesc(desc);

        //insert
        entity.setId(null);
        BraveSql.build(MysqlUserEntity.class).insertOrUpdate(entity);
        entity.setId(null);
        BraveSql.build(MysqlUserEntity.class).batchInsertOrUpdate(Arrays.asList(entity));
        entity.setId(null);
        BraveSql.build(MysqlUserEntity.class).insert(entity);
        entity.setId(null);
        BraveSql.build(MysqlUserEntity.class).batchInsert(Arrays.asList(entity));
        entity.setId(null);
        BraveSql.build(MysqlUserEntity.class).insertActive(entity);
        System.out.println(entity);
        MysqlUserEntity entity2 = new MysqlUserEntity();
        entity2.setId(entity.getId());
        entity2.setHobby(new HobbyEntity());
//        entity2.setHobby(desc);
        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(MysqlUserEntity::getId, entity2.getId());
        //update
        BraveSql.build(dynamicSql, MysqlUserEntity.class).update(entity2);
        BraveSql.build(dynamicSql, MysqlUserEntity.class).updateActive(entity2);
        BraveSql.build(MysqlUserEntity.class).updateByPrimaryKey(entity2);
        BraveSql.build(MysqlUserEntity.class).updateActiveByPrimaryKey(entity2);
        System.out.println(entity2);
    }

    private void execute(boolean b) {
        try {
            System.out.println(b);
            throw new BraveException("123");
        } catch (Exception exception) {
            exception.printStackTrace();
            if (!b) {
                b = true;
            }
        } finally {
            System.out.println(b);
        }
    }

    public static class CustomSQLInterceptor implements SQLInterceptor {

        @Override
        public boolean doBefore(Class<?> entityClass, String sql, List<List<Object>> sqlParams) {
            log.info("entityClass :{}" + entityClass);
            log.info("sql :{}" + sql);
            log.info("sqlParams :{}" + sqlParams);
            log.info("true 开始执行SQL");
            return true;
        }

        @Override
        public void doAfter(Class<?> entityClass, BraveException braveException) {
            if (braveException != null) {
                throw braveException;
            }
            log.info("doAfter==================================================");
        }
    }

//    public static class FalseSQLInterceptor implements SQLInterceptor {
//
//        @Override
//        public boolean doBefore(Class<?> entityClass, String sql, List<List<Object>> sqlParams) {
//            log.info("entityClass :{}" + entityClass);
//            log.info("sql :{}" + sql);
//            log.info("sqlParams :{}" + sqlParams);
//            log.info("false,不会执行SQL");
//            return false;
//        }
//
//        @Override
//        public void doAfter(Class<?> entityClass, BraveException braveException) {
//
//        }
//    }

    /**
     * 初始化语句
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    @Table(value = "`t_mysql_user`", dataSourceClass = TestDatabaseConfig.class)
    public static class MysqlUserEntity {
        /**
         * 主键
         */
        @Id
        @GeneratedValue(strategy = AUTO)
        private Integer id;
        /**
         * 测试忽略字段
         */
        @ColumnIgnore
        private String other;

        /**
         * 账号
         */
        private String accountNo;

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 性别
         */
        private GenderEnum gender;

        /**
         * 出生日期
         */
        private LocalDate dateOfBirth;

        /**
         * 出生时间
         */
        private LocalTime timeOfBirth;

        /**
         * 爱好
         */
        @ColumnJson("hobby")
        private HobbyEntity hobby;
        /**
         * 描述
         */
        @ColumnJson("desc")
        private HobbyEntity desc;

        /**
         * 是否删除 true 已删除 false 未删除
         */
        private Boolean isDeleted;

        /**
         * 创建时间
         */
        private LocalDateTime createDate;

        /**
         * 修改时间
         */
        private Date updateDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    @Table(value = "`t_mysql_user2`", dataSourceClass = TestDatabaseConfig.class)
    public static class MysqlUserEntity2 {
        /**
         * 主键
         */
        private Integer id;
        /**
         * 测试忽略字段
         */
        @ColumnIgnore
        private String other;

        /**
         * 账号
         */
        private String accountNo;
        /**
         * 其他字段不需要了
         */
    }

    /**
     * 唱 跳 Rap 打篮球
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    public static class HobbyEntity {
        private Long maxLong;
        private Double maxDouble;
        private Boolean sing;
        private Boolean dance;
        private Boolean rap;
        private Boolean playBasketball;
    }

    /**
     * 故意使用的中文枚举，测试不规范代码的兼容性
     */
    enum GenderEnum {
        男, 女, 未知;
    }
}