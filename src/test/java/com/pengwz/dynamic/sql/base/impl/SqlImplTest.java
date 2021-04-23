package com.pengwz.dynamic.sql.base.impl;

import com.pengwz.dynamic.entity.UserEntity;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import com.pengwz.dynamic.sql.PageInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class SqlImplTest {
    private static final Log log = LogFactory.getLog(SqlImplTest.class);

    //    @BeforeClass
    @Before
    public /*static*/ void setUp() throws ParseException {
        //创建基本数据
        Integer delete = BraveSql.build(UserEntity.class).delete();
        log.info("清除测试数据：" + delete);
        //插入测试数据
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("tom");
        userEntity.setSex("男");
        userEntity.setPhone("13212345678");
        userEntity.setBirthday(LocalDate.of(2021, 1, 11));
        userEntity.setUpdateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-1-11 00:00:00"));
        userEntity.setCreateDate(LocalDateTime.of(LocalDate.of(2021, 1, 11), LocalTime.MIN));
        Integer insert = BraveSql.build(UserEntity.class).insert(userEntity);
        log.info("新增的测试数据" + insert);
    }


    /**
     * 查询不存在的主键
     */
    @Test
    public void selectByPrimaryKey() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey("123");
        log.info(userEntity);
        assertNull(userEntity);
    }

    /**
     * 查询存在的主键
     */
    @Test
    public void selectByPrimaryKey2() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        log.info(userEntity);
        assertNotNull(userEntity);
    }

    /**
     * 测试字符串日期
     */
    @Test
    public void selectSingle() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "tom");
        dynamicSql.andEqualTo(UserEntity::getBirthday, "2021-01-11");
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        log.info(userEntity);
        assertNotNull(userEntity);
    }

    /**
     * 测试LocalDate对象日期
     */
    @Test
    public void selectSingle2() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "tom");
        dynamicSql.andEqualTo(UserEntity::getUsername, null);
        dynamicSql.andEqualTo(UserEntity::getBirthday, LocalDate.of(2021, 1, 11));
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        log.info(userEntity);
        assertNull(userEntity);
    }

    /**
     * 测试LocalDateTime对象日期
     */
    @Test
    public void selectSingle3() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "tom");
        dynamicSql.andEqualTo(UserEntity::getCreateDate, LocalDateTime.of(LocalDate.of(2021, 1, 11), LocalTime.MIN));
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        log.info(userEntity);
        assertNotNull(userEntity);
    }

    @Test
    public void select() {
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        assertTrue(select.size() >= 1);
    }

    /**
     * 测试查询100000条数据
     */
    @Test
    public void select2() {
        List<UserEntity> userEntities = getUserEntities(100000);
        BraveSql.build(UserEntity.class).batchInsert(userEntities);
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        assertTrue(select.size() >= 100000);
    }

    @Test
    public void selectCount() {
        Integer integer = BraveSql.build(UserEntity.class).selectCount();
        assertTrue(integer >= 1);
    }

    @Test
    public void selectCount2() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, -1);
        Integer integer = BraveSql.build(dynamicSql, UserEntity.class).selectCount();
        assertEquals(0, (int) integer);
    }

    @Test
    public void selectPageInfo() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, 1);
        PageInfo<UserEntity> pageInfo = BraveSql.build(dynamicSql, UserEntity.class).selectPageInfo(0, 10);
        assertEquals(1, pageInfo.getResultList().size());
    }

    /**
     * 测试开始页为负值
     */
    @Test
    public void selectPageInfo2() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, 1);
        PageInfo<UserEntity> pageInfo = BraveSql.build(dynamicSql, UserEntity.class).selectPageInfo(-99, 10);
        assertEquals(1, pageInfo.getResultList().size());
    }

    /**
     * 测试开始页为负值
     */
    @Test
    public void selectPageInfo3() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, 1);
        PageInfo<UserEntity> pageInfo = BraveSql.build(dynamicSql, UserEntity.class).selectPageInfo(10000, 100);
        assertEquals(0, pageInfo.getResultList().size());
    }

    private List<UserEntity> getUserEntities(int size) {
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("tom_" + i);
            userEntity.setSex("男");
            userEntity.setPhone("13212345678");
            userEntity.setBirthday(LocalDate.now());
            userEntity.setUpdateDate(new Date());
            userEntity.setCreateDate(LocalDateTime.now());
            userEntities.add(userEntity);
        }
        return userEntities;
    }

    /**
     * 插入10条数据
     */
    @Test
    public void batchInsert() {
        List<UserEntity> userEntities = getUserEntities(10);
        int integer = BraveSql.build(UserEntity.class).batchInsert(userEntities);
        assertEquals(10, integer);
        for (UserEntity userEntity : userEntities) {
            assertNotNull(userEntity.getId());
        }
    }

    @Test
    public void insertOrUpdate() {
        UserEntity userEntity = getUserEntities(1).get(0);
        userEntity.setId(1L);
        int integer = BraveSql.build(UserEntity.class).insertOrUpdate(userEntity);
        assertEquals(1, integer);
    }

    /**
     * 更新或插入100000个
     */
    @Test
    public void insertOrUpdate2() {
        int integer = BraveSql.build(UserEntity.class).batchInsertOrUpdate(getUserEntities(100000));
        assertEquals(100000, integer);
    }

    @Test
    public void update() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        assertEquals(LocalDate.of(2021, 1, 11), userEntity.getBirthday());
        userEntity.setBirthday(LocalDate.of(2020, 10, 31));
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getId, 1);
        int update = BraveSql.build(dynamicSql, UserEntity.class).update(userEntity);
        assertEquals(1, update);
        UserEntity userEntity2 = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        assertEquals(LocalDate.of(2020, 10, 31), userEntity2.getBirthday());
    }

    /**
     * 将某个属性更新为null
     */
    @Test
    public void update2() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        userEntity.setBirthday(null);
        int update = BraveSql.build(UserEntity.class).update(userEntity);
        assertEquals(1, update);
        assertNull(userEntity.getBirthday());
    }

    /**
     * 更新指定属性更新为null
     */
    @Test
    public void updateActive() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.setNullColumnByUpdate(UserEntity::getBirthday);
        BraveSql.build(dynamicSql, UserEntity.class).updateActive(new UserEntity());
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        assertNull(userEntity.getBirthday());
    }

    @Test
    public void updateByPrimaryKey() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        userEntity.setUsername("jerry");
        BraveSql.build(UserEntity.class).updateByPrimaryKey(userEntity);
        UserEntity userEntity2 = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        assertEquals(userEntity.getUsername(), userEntity2.getUsername());
    }

    @Test
    public void delete() {
        UserEntity userEntity = getUserEntities(1).get(0);
        userEntity.setUsername("god");
        BraveSql.build(UserEntity.class).insert(userEntity);
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "god");
        Integer delete = BraveSql.build(dynamicSql, UserEntity.class).delete();
        assertTrue(delete >= 1);
    }

    /**
     * 测试频繁获取、销毁连接
     */
    @Test
    public void testDb() {
        int count = 0;
        for (int i = 0; i < 10000; i++) {
            UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
            userEntity.setUsername("jerry and tom");
            BraveSql.build(UserEntity.class).update(userEntity);
            BraveSql.build(UserEntity.class).select();
            userEntity.setId(999999L);
            BraveSql.build(UserEntity.class).insert(userEntity);
            DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
            dynamicSql.andEqualTo(UserEntity::getId, 999999L);
            BraveSql.build(dynamicSql, UserEntity.class).delete();
            count++;
        }
        assertEquals(10000, count);
    }

    @Test
    public void deleteByPrimaryKey() {
        int integer = BraveSql.build(UserEntity.class).deleteByPrimaryKey(1L);
        assertEquals(1, integer);
    }

    /**
     * 会新增失败
     */
    @Test(expected = BraveException.class)
    public void insertActive() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("pwd");
        userEntity.setPhone("123    ");
        Integer insert = BraveSql.build(UserEntity.class).insert(userEntity);
        log.info("insert " + insert);
    }

    @Test
    public void insertActive2() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("pwd");
        userEntity.setPhone("123    ");
        Integer insert = BraveSql.build(UserEntity.class).insertActive(userEntity);
        log.info("insertActive " + insert);
    }


}