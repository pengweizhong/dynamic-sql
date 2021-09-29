package com.pengwz.dynamic.sql;


import com.pengwz.dynamic.config.OracleDatabaseConfig;
import com.pengwz.dynamic.entity.oracle.TBCopyEntity;
import com.pengwz.dynamic.entity.oracle.TBCopyEntity2;
import com.pengwz.dynamic.exception.BraveException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OracleBraveSqlTest {


    @Test(expected = BraveException.class)
    public void executeQuery() {
        String sql = "select * from TB_copy666 ";
        BraveSql.build(TBCopyEntity.class).executeQuery(sql);
    }

    @Test
    public void executeQuery2() {
        String sql = "SELECT * FROM \"TB_copy666\" ";
        List<TBCopyEntity> tbCopyEntityList = BraveSql.build(TBCopyEntity.class).executeQuery(sql, OracleDatabaseConfig.class);
        Assert.assertNotNull(tbCopyEntityList);
        System.out.println(tbCopyEntityList.size());
        System.out.println(tbCopyEntityList.get(0));
    }


    @Test
    public void select() {
        List<TBCopyEntity> tbCopyEntityList = BraveSql.build(TBCopyEntity.class).select();
        Assert.assertNotNull(tbCopyEntityList);
        System.out.println(tbCopyEntityList.size());
        System.out.println(tbCopyEntityList.get(0));
    }

    @Test
    public void select2() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getTbColumn0, "TbColumn0000_0");
        List<TBCopyEntity> tbCopyEntityList = BraveSql.build(dynamicSql, TBCopyEntity.class).select();
        Assert.assertNotNull(tbCopyEntityList);
        System.out.println(tbCopyEntityList.size());
        System.out.println(tbCopyEntityList.get(0));
    }

    @Test
    public void selectSingle() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getTbColumn1, "TbColumn1111_0");
        TBCopyEntity entity = BraveSql.build(dynamicSql, TBCopyEntity.class).selectSingle();
        Assert.assertNotNull(entity);
        System.out.println(entity);
    }

    @Test
    public void selectByPrimaryKey() {
        TBCopyEntity entity = BraveSql.build(TBCopyEntity.class).selectByPrimaryKey(1135);
        Assert.assertNotNull(entity);
        System.out.println(entity);
    }

    @Test
    public void selectCount() {
        Integer selectCount = BraveSql.build(TBCopyEntity.class).selectCount();
        Assert.assertNotNull(selectCount);
        System.out.println(selectCount);
    }

    @Test
    public void selectCount2() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getTbColumn1, "TbColumn1111_0");
        Integer selectCount = BraveSql.build(dynamicSql, TBCopyEntity.class).selectCount();
        Assert.assertNotNull(selectCount);
        System.out.println(selectCount);
    }

    @Test
    public void selectPageInfo() {
        PageInfo<TBCopyEntity> tbCopyEntityPageInfo = BraveSql.build(TBCopyEntity.class).selectPageInfo(1, 10);
        Assert.assertNotNull(tbCopyEntityPageInfo);
        System.out.println(tbCopyEntityPageInfo.getTotalSize());
        tbCopyEntityPageInfo.getResultList().forEach(System.out::println);
    }

    @Test
    public void selectPageInfo2() {
        PageInfo<TBCopyEntity> tbCopyEntityPageInfo = BraveSql.build(TBCopyEntity.class).selectPageInfo(0, 5);
        Assert.assertNotNull(tbCopyEntityPageInfo);
        System.out.println(tbCopyEntityPageInfo.getTotalSize());
        System.out.println(tbCopyEntityPageInfo);
        tbCopyEntityPageInfo.getResultList().forEach(System.out::println);
    }

    @Test
    public void selectPageInfo3() {
        PageInfo<TBCopyEntity> tbCopyEntityPageInfo = BraveSql.build(TBCopyEntity.class).selectPageInfo(0, -1);
        Assert.assertNotNull(tbCopyEntityPageInfo);
        System.out.println(tbCopyEntityPageInfo.getTotalSize());
        System.out.println(tbCopyEntityPageInfo);
        tbCopyEntityPageInfo.getResultList().forEach(System.out::println);
    }

    @Test
    public void selectPageInfo4() {
        PageInfo<TBCopyEntity> tbCopyEntityPageInfo = BraveSql.build(TBCopyEntity.class).selectPageInfo(0, 0);
        Assert.assertNotNull(tbCopyEntityPageInfo);
        System.out.println(tbCopyEntityPageInfo.getTotalSize());
        System.out.println(tbCopyEntityPageInfo);
        tbCopyEntityPageInfo.getResultList().forEach(System.out::println);
    }

    @Test
    public void selectPageInfo5() {
        PageInfo<TBCopyEntity> tbCopyEntityPageInfo = BraveSql.build(TBCopyEntity.class).selectPageInfo(0, 1);
        Assert.assertNotNull(tbCopyEntityPageInfo);
        System.out.println(tbCopyEntityPageInfo.getTotalSize());
        System.out.println(tbCopyEntityPageInfo);
        tbCopyEntityPageInfo.getResultList().forEach(System.out::println);
    }

    //测试序列自动插入主键
    @Test
    public void insert() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("aaa");
        entity.setTbColumn1("bbb");
        entity.setTbColumn2("ccc");
        Integer insert = BraveSql.build(TBCopyEntity.class).insert(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }

    //测试插入指定主键
    @Test
    public void insert2() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(1);
        entity.setTbColumn0("aaa");
        entity.setTbColumn1("bbb");
        Integer insert = BraveSql.build(TBCopyEntity.class).insert(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }

    //TODO 测试触发器插入 报错 com.pengwz.dynamic.exception.BraveException: 不支持的特性
    @Test
    public void insert3() {
        TBCopyEntity2 entity = new TBCopyEntity2();
        entity.setTbColumn0("aaa");
        entity.setTbColumn1("bbb");
        entity.setTbColumn2("ccc");
        Integer insert = BraveSql.build(TBCopyEntity2.class).insert(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }

    @Test
    public void insertActive() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("1111");
        entity.setTbColumn1("2222");
        Integer insert = BraveSql.build(TBCopyEntity.class).insertActive(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }

    @Test
    public void insertActive2() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("1111");
        entity.setTbColumn1("2222");
        entity.setTbColumn2("3333");
        Integer insert = BraveSql.build(TBCopyEntity.class).insertActive(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }


    @Test
    public void testInsert() {
        List<TBCopyEntity> tbCopyEntityList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TBCopyEntity entity = new TBCopyEntity();
            entity.setTbColumn0("TbColumn0000_" + i);
            entity.setTbColumn1("TbColumn1111_" + i);
            entity.setTbColumn2("TbColumn2222_" + i);
            tbCopyEntityList.add(entity);
        }
        Integer integer = BraveSql.build(TBCopyEntity.class).batchInsert(tbCopyEntityList);
        System.out.println(integer);
        System.out.println(tbCopyEntityList);
    }

    @Test
    public void testSelect() {
        List<TBCopyEntity> select = BraveSql.build(TBCopyEntity.class).select();
        System.out.println(select.size());
        System.out.println(select.subList(0, 1));
    }


}