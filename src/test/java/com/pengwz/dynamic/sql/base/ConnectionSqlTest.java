package com.pengwz.dynamic.sql.base;

import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import com.pengwz.dynamic.sql.PageInfo;
import com.pengwz.dynamic.sql.base.SqlsTest.HobbyEntity;
import com.pengwz.dynamic.sql.base.SqlsTest.MysqlUserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 主要测试连接泄露
 */
@Slf4j
public class ConnectionSqlTest {
    int loopNum = 1000;

    @Test
    public void selectByPrimaryKey() {
        for (int i = 0; i < loopNum; i++) {
            final MysqlUserEntity mysqlUserEntity = BraveSql.build(MysqlUserEntity.class).selectByPrimaryKey(1);
            System.out.println(mysqlUserEntity);
        }
    }

    @Test
    public void selectByPrimaryKey2() {
        for (int i = 0; i < loopNum; i++) {
            final MysqlUserEntity mysqlUserEntity = BraveSql.build(MysqlUserEntity.class).selectByPrimaryKey(1111111);
            System.out.println(mysqlUserEntity);
        }
    }

    @Test
    public void selectSingle() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity mysqlUserEntity = BraveSql.build(MysqlUserEntity.class).selectSingle();
                System.out.println(mysqlUserEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void selectSingle2() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
                dynamicSql.andIn(MysqlUserEntity::getId, Collections.singleton(1));
                final MysqlUserEntity mysqlUserEntity = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectSingle();
                System.out.println(mysqlUserEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void selectAggregateFunction() {
        for (int i = 0; i < loopNum; i++) {
            final BigDecimal bigDecimal = BraveSql.build(MysqlUserEntity.class).selectAvg(MysqlUserEntity::getId);
            System.out.println(bigDecimal);
        }
    }

    @Test
    public void selectAggregateFunction2() {
        for (int i = 0; i < loopNum; i++) {
            final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
            dynamicSql.andIn(MysqlUserEntity::getId, Arrays.asList(1, 2, 3, 4, 5, 6));
            final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectAvg(MysqlUserEntity::getId, Integer.class);
            System.out.println(integer);
        }
    }

    @Test
    public void selectAggregateFunction3() {
        for (int i = 0; i < loopNum; i++) {
            final BigDecimal bigDecimal = BraveSql.build(MysqlUserEntity.class).selectMin(MysqlUserEntity::getId);
            System.out.println(bigDecimal);
        }
    }

    @Test
    public void selectAggregateFunction4() {
        for (int i = 0; i < loopNum; i++) {
            final BigDecimal bigDecimal = BraveSql.build(MysqlUserEntity.class).selectMax(MysqlUserEntity::getId);
            System.out.println(bigDecimal);
        }
    }

    @Test
    public void selectAggregateFunction5() {
        for (int i = 0; i < loopNum; i++) {
            final BigDecimal bigDecimal = BraveSql.build(MysqlUserEntity.class).selectSum(MysqlUserEntity::getId);
            System.out.println(bigDecimal);
        }
    }

    @Test
    public void selectAggregateFunction6() {
        for (int i = 0; i < loopNum; i++) {
            final Integer integer = BraveSql.build(MysqlUserEntity.class).selectCount(MysqlUserEntity::getId);
            System.out.println(integer);
        }
    }

    @Test
    public void selectPageInfo() {
        for (int i = 0; i < loopNum; i++) {
            final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(1, 10);
            System.out.println(pageInfo);
        }
    }

    @Test
    public void selectPageInfo2() {
        for (int i = 0; i < loopNum; i++) {
            final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
            dynamicSql.andLessThan(MysqlUserEntity::getId, 1);
            final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(dynamicSql, MysqlUserEntity.class).selectPageInfo(1, 10);
            System.out.println(pageInfo);
        }
    }

    @Test
    public void selectPageInfo3() {
        for (int i = 0; i < loopNum; i++) {
            final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(1, 0);
            System.out.println(pageInfo);
        }
    }

    @Test
    public void selectPageInfo4() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(1, -1);
                System.out.println(pageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void selectPageInfo5() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(0, 0);
                System.out.println(pageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void selectPageInfo6() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(-1, 0);
                System.out.println(pageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void selectPageInfo7() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(-1, -1);
                System.out.println(pageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void selectPageInfo8() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final PageInfo<MysqlUserEntity> pageInfo = BraveSql.build(MysqlUserEntity.class).selectPageInfo(-1, 1);
                System.out.println(pageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void insertActive() {
        for (int i = 0; i < loopNum; i++) {
            final Integer integer = BraveSql.build(MysqlUserEntity.class).insertActive(null);
            System.out.println(integer);
        }
    }

    @Test
    public void insertActive2() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final Integer integer = BraveSql.build(MysqlUserEntity.class).insertActive(MysqlUserEntity.builder().build());
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void insertActive3() {
        for (int i = loopNum; i < loopNum * 2; i++) {
            try {
                final Integer integer = BraveSql.build(MysqlUserEntity.class).insertActive(MysqlUserEntity.builder().accountNo(i + "").hobby(new HobbyEntity()).build());
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void batchInsert() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final ArrayList<MysqlUserEntity> userEntities = new ArrayList<>();
//                userEntities.add(null);
                userEntities.add(MysqlUserEntity.builder().accountNo(i + "").build());
                final Integer integer = BraveSql.build(MysqlUserEntity.class).batchInsert(userEntities);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void batchInsert2() {
        final ArrayList<MysqlUserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            userEntities.add(MysqlUserEntity.builder().accountNo(i + "").build());
            try {
                final Integer integer = BraveSql.build(MysqlUserEntity.class).batchInsert(userEntities);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void batchInsert3() {
        final ArrayList<MysqlUserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            userEntities.add(MysqlUserEntity.builder().accountNo(i + "").build());
            final Integer integer = BraveSql.build(MysqlUserEntity.class).batchInsert(userEntities);
            System.out.println(integer);
        }
    }

    @Test
    public void insertOrUpdate() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().id(i + 1).build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).insertOrUpdate(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void insertOrUpdate2() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).insertOrUpdate(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void update() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).update(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void update2() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().id(i + 1).build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).update(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void update3() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
                dynamicSql.andEqualTo(MysqlUserEntity::getId, i + 1);
                final MysqlUserEntity build = MysqlUserEntity.builder().id(i + 1).build();
                final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).update(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void updateByPrimaryKey() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().id(i + 1).build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).updateByPrimaryKey(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void updateActiveByPrimaryKey() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().id(i + 1).build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).updateActiveByPrimaryKey(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void updateActiveByPrimaryKey2() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final MysqlUserEntity build = MysqlUserEntity.builder().id(null).build();
                final Integer integer = BraveSql.build(MysqlUserEntity.class).updateActiveByPrimaryKey(build);
                System.out.println(build);
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void delete() {
        for (int i = 0; i < loopNum; i++) {
            try {
                final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
                dynamicSql.andGreaterThanOrEqualTo(MysqlUserEntity::getId, 50000);
                final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).delete();
                System.out.println(integer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void test() {
//        final DynamicSql<MysqlUserEntity> dynamicSql = DynamicSql.createDynamicSql();
//        dynamicSql.andIsNull(MysqlUserEntity::getAccountNo);
//        final Integer integer = BraveSql.build(dynamicSql, MysqlUserEntity.class).delete();
//        System.out.println(integer);

//        final List<MysqlUserEntity> select = BraveSql.build(MysqlUserEntity.class).select();
//        System.out.println(select);

        System.out.println(BraveSql.build(MysqlUserEntity.class).selectByPrimaryKey(514524));
    }
}
