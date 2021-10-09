package com.pengwz.dynamic.sql;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengwz.dynamic.config.OracleDatabaseConfig;
import com.pengwz.dynamic.entity.oracle.TBCopyEntity;
import com.pengwz.dynamic.entity.oracle.TBCopyEntity2;
import com.pengwz.dynamic.exception.BraveException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

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

    @Test
    public void selectPageInfo6() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIsNotNull(TBCopyEntity::getJson);
        PageInfo<TBCopyEntity> tbCopyEntityPageInfo = BraveSql.build(dynamicSql, TBCopyEntity.class).selectPageInfo(1, 10);
        Assert.assertNotNull(tbCopyEntityPageInfo);
        System.out.println(tbCopyEntityPageInfo);
        tbCopyEntityPageInfo.getResultList().forEach(System.out::println);
    }

    //测试序列自动插入主键
    @Test
    public void insert() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("aaaaa");
        entity.setTbColumn1("bbbbb");
        entity.setTbColumn2("ccccc");
        TBCopyEntity entity1 = new TBCopyEntity();
        BeanUtils.copyProperties(entity, entity1);
        entity.setJson(entity1);
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
        entity.setTbColumn3(BigInteger.valueOf(1L));
        Integer insert = BraveSql.build(TBCopyEntity.class).insertActive(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }

    @Test
    public void insertActive3() {
        TBCopyEntity entity = new TBCopyEntity();
        Integer insert = BraveSql.build(TBCopyEntity.class).insertActive(entity);
        Assert.assertNotNull(insert);
        System.out.println(insert);
        System.out.println(entity);
    }


    @Test
    public void batchInsert() {
        List<TBCopyEntity> tbCopyEntityList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TBCopyEntity entity = new TBCopyEntity();
            entity.setTbColumn0("TbColumn0000_" + i);
            entity.setTbColumn1("TbColumn1111_" + i);
            entity.setTbColumn2("TbColumn2222_" + i);
            tbCopyEntityList.add(entity);
        }
        Integer integer = BraveSql.build(TBCopyEntity.class).batchInsert(tbCopyEntityList);
        Assert.assertNotNull(integer);
        System.out.println(integer);
        System.out.println(tbCopyEntityList);
    }

    @Test
    public void insertOrUpdate() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("TbColumn0000_");
        entity.setTbColumn1("TbColumn1111_");
        entity.setTbColumn2("TbColumn2222_");
        Integer integer = BraveSql.build(TBCopyEntity.class).insertOrUpdate(entity);
        Assert.assertNotNull(integer);
        System.out.println(integer);
    }

    @Test
    public void batchInsertOrUpdate() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("TbColumn0000_");
        entity.setTbColumn1("TbColumn1111_");
        entity.setTbColumn2("TbColumn2222_");
        Integer integer = BraveSql.build(TBCopyEntity.class).batchInsertOrUpdate(Collections.singleton(entity));
        Assert.assertNotNull(integer);
        System.out.println(integer);
    }

    @Test
    public void update() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getId, 1200);
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("update1");
        entity.setTbColumn1("update2");
        entity.setTbColumn2("update3");
        entity.setId(1200);
        entity.setTbColumn3(BigInteger.valueOf(9999L));
        Integer update = BraveSql.build(dynamicSql, TBCopyEntity.class).update(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void update2() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getId, 1200);
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("update1");
        entity.setTbColumn1("update2");
        entity.setId(1200);
        entity.setTbColumn3(BigInteger.valueOf(9999L));
        Integer update = BraveSql.build(dynamicSql, TBCopyEntity.class).update(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateActive() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getId, 1200);
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("update1");
        entity.setTbColumn1("update2");
        entity.setId(1200);
        Integer update = BraveSql.build(dynamicSql, TBCopyEntity.class).updateActive(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateActive2() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setTbColumn0("hahahahah");
        entity.setTbColumn1("hahahahah");
        entity.setId(1200);
        TBCopyEntity entity1 = new TBCopyEntity();
        BeanUtils.copyProperties(entity, entity1);
        entity.setJson(entity1);
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andIn(TBCopyEntity::getId, Collections.singleton(1200));
        Integer update = BraveSql.build(dynamicSql, TBCopyEntity.class).updateActive(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateActive3() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getId, 1);
        dynamicSql.setNullColumnByUpdateActive(TBCopyEntity::getTbColumn2);
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(1);
        Integer update = BraveSql.build(dynamicSql, TBCopyEntity.class).updateActive(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateByPrimaryKey() {
        TBCopyEntity entity = new TBCopyEntity();
        Integer update = BraveSql.build(TBCopyEntity.class).updateByPrimaryKey(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateByPrimaryKey2() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(1200);
        Integer update = BraveSql.build(TBCopyEntity.class).updateByPrimaryKey(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateByPrimaryKey3() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(1200);
        entity.setTbColumn0("not");
        entity.setTbColumn1("not");
        Integer update = BraveSql.build(TBCopyEntity.class).updateByPrimaryKey(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateActiveByPrimaryKey() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(1200);
        entity.setTbColumn0("not11");
        entity.setTbColumn1("not222");
        Integer update = BraveSql.build(TBCopyEntity.class).updateActiveByPrimaryKey(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void updateActiveByPrimaryKey2() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(1200);
        entity.setTbColumn0("aaaa");
        entity.setTbColumn1("aaaa");
        entity.setTbColumn2("aaaa");
        Integer update = BraveSql.build(TBCopyEntity.class).updateActiveByPrimaryKey(entity);
        Assert.assertNotNull(update);
        System.out.println(update);
    }

    @Test
    public void delete() {
        Integer delete = BraveSql.build(TBCopyEntity.class).delete();
        Assert.assertNotNull(delete);
        System.out.println(delete);
    }

    @Test
    public void delete2() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getTbColumn1, 2);
        Integer delete = BraveSql.build(dynamicSql, TBCopyEntity.class).delete();
        Assert.assertNotNull(delete);
        System.out.println(delete);
    }

    @Test
    public void deleteByPrimaryKey() {
        Integer delete = BraveSql.build(TBCopyEntity.class).deleteByPrimaryKey(1200);
        Assert.assertNotNull(delete);
        System.out.println(delete);
    }

    @Test
    public void deleteByPrimaryKey2() {
        Integer delete = BraveSql.build(TBCopyEntity.class).deleteByPrimaryKey("abc");
        Assert.assertNotNull(delete);
        System.out.println(delete);
    }

    @Test
    public void orderByAsc() {
        List<TBCopyEntity> tbColumn0 = BraveSql.build(TBCopyEntity.class).orderByAsc("tbColumn0").select();
        Assert.assertNotNull(tbColumn0);
        System.out.println(tbColumn0);
    }

    @Test
    public void orderByAsc2() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andGreaterThanOrEqualTo(TBCopyEntity::getTbColumn0, 2);
        List<TBCopyEntity> tbColumn0 = BraveSql.build(dynamicSql, TBCopyEntity.class).orderByAsc("tbColumn0", "tbColumn1").select();
        Assert.assertNotNull(tbColumn0);
        System.out.println(tbColumn0);
    }

    @Test
    public void orderByAsc3() {
        List<TBCopyEntity> tbColumn0 = BraveSql.build(TBCopyEntity.class).orderByAsc("tbColumn0").orderByAsc("tbColumn1").select();
        Assert.assertNotNull(tbColumn0);
        System.out.println(tbColumn0);
    }


    @Test
    public void testSelect() {
        List<TBCopyEntity> select = BraveSql.build(TBCopyEntity.class).select();
        System.out.println(select.size());
        System.out.println(select.subList(0, 1));
    }

    @Test
    public void testSelect2() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andBetween(TBCopyEntity::getId, 1200, 1400);
        dynamicSql.andLike("json", "%tbColumn0%");
        List<TBCopyEntity> select = BraveSql.build(dynamicSql, TBCopyEntity.class).select();
        System.out.println(select.size());
        select.forEach(System.out::println);
    }

    @Test
    public void testSelect3() {
        String sql = "select * from \"TB_copy666\" where ID = 1200";
        List<TBCopyEntity> copyEntities = BraveSql.build(TBCopyEntity.class).executeQuery(sql, OracleDatabaseConfig.class);
        System.out.println(copyEntities.get(0));
        String sql2 = "select ID,JSON_COL from \"TB_copy666\" where ID = 1200";
        List<TBCopyEntity> copyEntities2 = BraveSql.build(TBCopyEntity.class).executeQuery(sql2, OracleDatabaseConfig.class);
        System.out.println(copyEntities2.get(0));
    }

    @Test
    public void testColumnJson() {
        List<TBCopyEntity> select = BraveSql.build(TBCopyEntity.class).select();
        select.forEach(System.out::println);
        System.out.println(select.size());


    }

    @Test
    public void testGson() {
        TBCopyEntity entity = new TBCopyEntity();
        entity.setId(123);
        Gson gson = new Gson();
        String s = gson.toJson(entity);
        System.out.println(s);
        System.out.println(gson.toJson(null));

        Gson gson1 = new GsonBuilder().serializeNulls().create();
        System.out.println(gson1.toJson(entity));

        System.out.println("---------------------");

        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.setDate(new Date());
        jsonEntity.setId(23);
        jsonEntity.setJsonEntity(jsonEntity);
        jsonEntity.setList(Collections.singletonList("hello"));
        jsonEntity.setLocalDateTime(LocalDateTime.now());
//        jsonEntity.setMap(new HashMap<>());
        System.out.println(gson.toJson(jsonEntity));
        JsonEntity jsonEntity1 = gson.fromJson(gson.toJson(jsonEntity), JsonEntity.class);
        System.out.println(jsonEntity1);

    }

    @Test
    public void testExistTable() {
        boolean att = BraveSql.build(Void.class).existTable("ATT", OracleDatabaseConfig.class);
        System.out.println(att);
        boolean att2 = BraveSql.existTable("att", OracleDatabaseConfig.class);
        System.out.println(att2);
    }

    @Test
    public void min() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(TBCopyEntity::getTbColumn0, "TbColumn0000_2");
        dynamicSql.andMin(TBCopyEntity::getId);
        List<TBCopyEntity> select = BraveSql.build(dynamicSql, TBCopyEntity.class).select();
        System.out.println(select);
    }

    @Test
    public void max() {
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
//        dynamicSql.andEqualTo(TBCopyEntity::getTbColumn0, "TbColumn0000_2");
        dynamicSql.andMax(TBCopyEntity::getId);
        List<TBCopyEntity> select = BraveSql.build(dynamicSql, TBCopyEntity.class).select();
        System.out.println(select);
    }

    @Test
    public void count() {
        System.out.println(BraveSql.build(TBCopyEntity.class).selectCount());
        System.out.println(BraveSql.build(TBCopyEntity.class).selectCount("tbColumn2"));
        System.out.println(BraveSql.build(TBCopyEntity.class).selectCount(TBCopyEntity::getTbColumn3));
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(TBCopyEntity::getId, 0);
        System.out.println(BraveSql.build(dynamicSql, TBCopyEntity.class).selectCount());
    }

    @Test
    public void sum() {
        System.out.println(BraveSql.build(TBCopyEntity.class).selectSum("id"));
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(TBCopyEntity::getId, 100);
        System.out.println(BraveSql.build(dynamicSql, TBCopyEntity.class).selectSum("id"));
    }

    @Test
    public void avg() {
        System.out.println(BraveSql.build(TBCopyEntity.class).selectAvg("id"));
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(TBCopyEntity::getId, 0);
        System.out.println(BraveSql.build(dynamicSql, TBCopyEntity.class).selectAvg("id"));
    }

    @Test
    public void min2() {
        System.out.println(BraveSql.build(TBCopyEntity.class).selectMin("id"));
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(TBCopyEntity::getId, 0);
        System.out.println(BraveSql.build(dynamicSql, TBCopyEntity.class).selectMin("id"));
    }

    @Test
    public void max2() {
        System.out.println(BraveSql.build(TBCopyEntity.class).selectMax("id"));
        DynamicSql<TBCopyEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andLessThanOrEqualTo(TBCopyEntity::getId, 0);
        System.out.println(BraveSql.build(dynamicSql, TBCopyEntity.class).selectMax("id"));
    }


}

class JsonEntity {
    private Integer id;
    private Date date;
    private LocalDateTime localDateTime;
    private List<String> list;
    private Map<Integer, List<Integer>> map;
    private JsonEntity jsonEntity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Map<Integer, List<Integer>> getMap() {
        return map;
    }

    public void setMap(Map<Integer, List<Integer>> map) {
        this.map = map;
    }

    public JsonEntity getJsonEntity() {
        return jsonEntity;
    }

    public void setJsonEntity(JsonEntity jsonEntity) {
        this.jsonEntity = jsonEntity;
    }

    @Override
    public String toString() {
        return "JsonEntity{" +
                "id=" + id +
                ", date=" + date +
                ", localDateTime=" + localDateTime +
                ", list=" + list +
                ", map=" + map +
                ", jsonEntity=" + jsonEntity +
                '}';
    }
}