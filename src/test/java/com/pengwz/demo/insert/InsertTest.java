package com.pengwz.demo.insert;

import com.pengwz.demo.entities.UserEntity;
import com.pengwz.dynamic.sql.BraveSql;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InsertTest {
    @Test
    public void insert() {
        UserEntity entity = new UserEntity();
        entity.setUsername("tom");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        entity.setPassword("password");
        Integer insert = BraveSql.build(UserEntity.class).insert(entity);
        //1
        System.out.println(insert);
    }

    @Test
    public void insertActive() {
        UserEntity entity = new UserEntity();
        entity.setUsername("jerry");
        entity.setPassword("password");
        Integer insert = BraveSql.build(UserEntity.class).insertActive(entity);
        //1
        System.out.println(insert);
    }

    @Test
    public void batchInsert() {
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity entity = new UserEntity();
            entity.setUsername("list_" + i);
            entity.setPassword("password_" + i);
            entity.setCreateDate(LocalDateTime.now());
            entity.setUpdateDate(LocalDateTime.now());
            userEntities.add(entity);
        }
        Integer insert = BraveSql.build(UserEntity.class).batchInsert(userEntities);
        //10
        System.out.println(insert);
    }
}
