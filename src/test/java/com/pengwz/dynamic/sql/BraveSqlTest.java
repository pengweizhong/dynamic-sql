package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.entity.UserEntity;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BraveSqlTest {

    @Test
    public void executeSelectSqlAndReturnSingle() {
    }

    @Test
    public void executeSelectSqlAndReturnList() {
    }

    @Test
    public void executeDMLSql() {
    }

    @Test
    public void getDynamicSql() {
    }

    @Test
    public void getCurrentClass() {
    }

    @Test
    public void getPageInfo() {
    }

    @Test
    public void select() {
    }

    @Test
    public void selectSingle() {

    }

    @Test
    public void selectByPrimaryKey() {
    }

    @Test
    public void selectCount() {
    }

    @Test
    public void selectPageInfo() {
    }

    @Test
    public void selectPageInfo1() {
    }

    @Test
    public void insert() {
        UserEntity entity = new UserEntity();
        entity.setUsername("tom");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        entity.setPassword("password");
        Integer insert = BraveSql.build(UserEntity.class).insert(entity);
        System.out.println(insert);
    }

    @Test
    public void insertActive() {
        UserEntity entity = new UserEntity();
        entity.setUsername("jerry");
        entity.setPassword("password");
        Integer insert = BraveSql.build(UserEntity.class).insertActive(entity);
        System.out.println(insert);
    }

    @Test
    public void batchInsert() {
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity entity = new UserEntity();
            entity.setUsername("list_"+i);
            entity.setPassword("password_"+i);
            entity.setCreateDate(LocalDateTime.now());
            entity.setUpdateDate(LocalDateTime.now());
            userEntities.add(entity);
        }
        Integer insert  = BraveSql.build(UserEntity.class).batchInsert(userEntities);
        System.out.println(insert);
    }

    @Test
    public void insertOrUpdate() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("tom_update");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        entity.setPassword("password_update");
        Integer insert = BraveSql.build(UserEntity.class).insertOrUpdate(entity);
        System.out.println(insert);
    }

    @Test
    public void batchInsertOrUpdate() {
    }

    @Test
    public void update() {
    }

    @Test
    public void updateActive() {
    }

    @Test
    public void updateByPrimaryKey() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void deleteByPrimaryKey() {
    }

    @Test
    public void orderByAsc() {
    }

    @Test
    public void orderByAsc1() {
    }

    @Test
    public void orderByDesc() {
    }

    @Test
    public void orderByDesc1() {
    }
}