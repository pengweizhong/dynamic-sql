package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.MyDBConfig;
import com.pengwz.dynamic.entity.UserAndDetailBO;
import com.pengwz.dynamic.entity.UserDetailEntity;
import com.pengwz.dynamic.entity.UserEntity;
import com.pengwz.dynamic.utils.ClassUtils;
import org.junit.Test;
import sun.java2d.pipe.SpanIterator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class DynamicSqlTest {

    @Test
    public void batchInsert() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity data = new UserEntity();
            data.setSex("男");
            data.setUsername("海绵宝宝" + i);
            data.setBirthday(LocalDate.now());
            data.setCreateDate(LocalDateTime.now());
            data.setUpdateDate(LocalDateTime.now());
            userEntities.add(data);
        }
        BraveSql.build(dynamicSql, UserEntity.class).batchInsert(userEntities);
        userEntities.forEach(System.out::println);
    }

    @Test
    public void selectAll() {
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        select.forEach(System.out::println);
    }

    @Test
    public void selectByPrimaryKey() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        System.out.println(userEntity);
    }

    @Test
    public void selectByCondition() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "海绵宝宝6");
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        System.out.println(userEntity);
    }

    @Test
    public void selectPageInfo() {
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(2, 2);
        System.out.println(userEntityPageInfo);
    }

    @Test
    public void testClassUtils() {
        List<Class> classes = ClassUtils.getAllClassByFather(DataSourceConfig.class);
        classes.forEach(System.out::println);
    }

    @Test
    public void test1() throws Exception {
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            UserEntity userEntity = new UserEntity();
            userEntity.setPhone("999999" + i);
            userEntity.setUsername("test");
            userEntity.setSex("男");
            userEntity.setUpdateDate(LocalDateTime.now());
            userEntities.add(userEntity);
        }
        Integer integer = BraveSql.build(UserEntity.class).batchInsert(userEntities);
        System.out.println(integer);
    }

    @Test
    public void test2() throws Exception {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "tom");
        UserEntity select = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        System.out.println(select);

        DynamicSql<UserDetailEntity> sql = DynamicSql.createDynamicSql();
        sql.andEqualTo(UserDetailEntity::getUsername, "tom");
        UserDetailEntity userDetailEntity = BraveSql.build(sql, UserDetailEntity.class).selectSingle();
        System.out.println(userDetailEntity);
    }

    @Test
    public void testPageInfo() throws Exception {
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(1, 10);
        System.out.println(userEntityPageInfo);

    }

    @Test
    public void test3() throws Exception {
        //DML  INSERT UPDATE DELETE
        UserAndDetailBO userEntity = BraveSql.executeSelectSqlAndReturnSingle("select a.name ,a.sex ,a.phone ,b.salary from t_user a join t_user_detail b on a.name =b.name ", UserAndDetailBO.class, MyDBConfig.class);
        System.out.println(userEntity);
//        List<UserEntity> userEntities = BraveSql.executeSelectSqlAndReturnList("select id,name userName,sex,phone,birthday,create_date createDate,update_date updateDate from t_user where id < 100", UserEntity.class, MyDBConfig.class);
//        System.out.println(userEntities);
        int b = BraveSql.executeDMLSql("delete from t_user where id = 1014", MyDBConfig.class);
        System.out.println(b);
    }

}
