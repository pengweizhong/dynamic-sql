package com.pengwz.dynamic.sql.base;

import com.google.gson.Gson;
import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.TestDatabaseConfig;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import com.pengwz.dynamic.sql.PageInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        for (int i = 0; i < 1000; i++) {
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
        Assert.assertEquals(entities.size(), 999);
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
        Assert.assertEquals(entities.size(), 999);
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
        Assert.assertEquals(entities.size(), 1000);
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
    @Test(expected = BraveException.class)
    public void insertActive3() {
        BraveSql.build(MysqlUserEntity2.class).insertActive(MysqlUserEntity2.builder().build());
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

    @Test
    public void printSql() {
    }

    @Test
    public void printParams() {
    }


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
        @GeneratedValue
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