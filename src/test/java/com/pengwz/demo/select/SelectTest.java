package com.pengwz.demo.select;

import com.pengwz.demo.entities.UserEntity;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.DynamicSql;
import com.pengwz.dynamic.sql.PageInfo;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class SelectTest {
    /**
     * 使用简单函数查询
     */
    @Test
    public void test1() {
        //查询总数量
        Integer count = BraveSql.build(UserEntity.class).selectCount();
        System.out.println(count);
        //对所有ID求和
        BigDecimal sum = BraveSql.build(UserEntity.class).selectSum(UserEntity::getId);
        System.out.println(sum);
    }

    /**
     * 根据主键查询
     */
    @Test
    public void test2() {
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        System.out.println(userEntity);
    }

    /**
     * 一般条件查询
     */
    @Test
    public void test3() {
        //查询用户名=jerry的数据
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        //可以直接使用字段名进行查询，但是i推荐使用表达式
        //dynamicSql.andEqualTo("username","jerry");
        dynamicSql.andEqualTo(UserEntity::getUsername, "jerry");
        List<UserEntity> entities = BraveSql.build(dynamicSql, UserEntity.class).select();
        System.out.println(entities);
    }

    /**
     * 分页查询
     */
    @Test
    public void test4() {
        //按5个为一组分页，取第一页
        PageInfo<UserEntity> pageInfo = BraveSql.build(UserEntity.class).selectPageInfo(1, 5);
        Assert.assertEquals(1, (int) pageInfo.getPageIndex());
        Assert.assertEquals(5, (int) pageInfo.getPageSize());
        Assert.assertEquals(5, pageInfo.getResultList().size());
    }

    /**
     * 复杂查询（创建带括号的查询）
     */
    @Test
    public void test5() {
        //查询用户名为jerry的用户，或者Id等于5或者50的数据
        //ID=50是不存在的，所以只会查询出2条数据
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername, "jerry");
        //使用此方法创建组查询，此方法可以根据业务无线嵌套
        dynamicSql.orComplex(sql -> sql.andEqualTo(UserEntity::getId, 5).orEqualTo(UserEntity::getId, 50)//.orComplex()：这里扔可以选择嵌套括号组
        );
        List<UserEntity> entities = BraveSql.build(dynamicSql, UserEntity.class).select();
        System.out.println(entities);
        System.out.println(entities.size());
    }
}
