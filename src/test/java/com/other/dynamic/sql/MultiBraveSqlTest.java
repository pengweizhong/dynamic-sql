package com.other.dynamic.sql;

import com.pengwz.dynamic.config.DataSourceManagement;
import com.pengwz.dynamic.config.DatabaseConfig;
import com.pengwz.dynamic.dto.SystemDTO;
import com.pengwz.dynamic.entity.SystemRoleEntity;
import com.pengwz.dynamic.entity.SystemRoleUserEntity;
import com.pengwz.dynamic.entity.SystemUserEntity;
import com.pengwz.dynamic.sql.BraveSql;
import com.pengwz.dynamic.sql.Select;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MultiBraveSqlTest {


    String createRoleSql = "CREATE TABLE `t_system_role` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',\n" +
            "  `role_name` varchar(50) DEFAULT NULL COMMENT '角色名称',\n" +
            "  `role_desc` varchar(255) DEFAULT NULL COMMENT '角色描述',\n" +
            "  `create_id` int(11) DEFAULT NULL COMMENT '创建人id',\n" +
            "  `update_id` int(11) DEFAULT NULL COMMENT '更新人id',\n" +
            "  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
            "  PRIMARY KEY (`id`)" +
            ") ENGINE=InnoDB  COMMENT='角色表'";

    String createUserSql = "CREATE TABLE `t_system_user` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "  `nick_name` varchar(50) DEFAULT NULL COMMENT '姓名',\n" +
            "  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',\n" +
            "  `email` varchar(11) DEFAULT NULL COMMENT '邮箱',\n" +
            "  `create_id` int(11) DEFAULT NULL COMMENT '创建人id',\n" +
            "  `update_id` int(11) DEFAULT NULL COMMENT '更新人id',\n" +
            "  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
            "  PRIMARY KEY (`id`)" +
            ") ENGINE=InnoDB  COMMENT='用户表'";

    String createRoleUserSql = "CREATE TABLE `t_system_role_user` (\n" +
            "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',\n" +
            "  `role_id` int(11) DEFAULT NULL COMMENT '角色id',\n" +
            "  `user_id` int(11) DEFAULT NULL COMMENT '用户id',\n" +
            "  `create_id` int(11) DEFAULT NULL COMMENT '创建人id',\n" +
            "  `update_id` int(11) DEFAULT NULL COMMENT '更新人id',\n" +
            "  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',\n" +
            "  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',\n" +
            "  PRIMARY KEY (`id`)\n" +
            ") ENGINE=InnoDB COMMENT='角色&用户表'";

    @Test
    public void doBefore() {
        if (!BraveSql.existTable("t_system_role", DatabaseConfig.class)) {
            BraveSql.executeSql(createRoleSql, DatabaseConfig.class);
            BraveSql.build(SystemRoleEntity.class).insertActive(SystemRoleEntity.builder()
                    .createId(1)
                    .updateId(1)
                    .id(1)
                    .roleName("admin")
                    .roleDesc("管理员")
                    .build());
        }
        if (!BraveSql.existTable("t_system_user", DatabaseConfig.class)) {
            BraveSql.executeSql(createUserSql, DatabaseConfig.class);
            BraveSql.build(SystemUserEntity.class).insertActive(SystemUserEntity.builder()
                    .createId(1)
                    .updateId(1)
                    .id(1)
                    .nickName("jerry")
                    .build());
        }
        if (!BraveSql.existTable("t_system_role_user", DatabaseConfig.class)) {
            BraveSql.executeSql(createRoleUserSql, DatabaseConfig.class);
            BraveSql.build(SystemRoleUserEntity.class).insertActive(SystemRoleUserEntity.builder()
                    .createId(1)
                    .updateId(1)
                    .userId(1)
                    .roleId(1)
                    .build());
        }
    }
    /*

     */

    /**
     * 对比SQL：
     * <p>
     * select
     * a.*,
     * b.*,
     * c.*
     * from
     * t_system_user a
     * join t_system_role_user b on
     * a.id = b.user_id
     * or a.id = 1
     * join t_system_role c on
     * b.role_id = c.id
     * where
     * a.id = 1
     *//*

    @Test
    public void test() {
//        final DynamicSql<SystemDTO> dynamicSql = DynamicSql.createDynamicSql();
//        dynamicSql.andEqualTo(SystemDTO::getId, 1);
//
//        final BraveSql<SystemDTO> braveSql = MultiBraveSql.builder(SystemRoleEntity.class).alias("a")
//                .join(SystemRoleUserEntity.class).alias("b")
//                .on(SystemRoleEntity::getId).equalTo(SystemRoleUserEntity::getRoleId).andEqualTo(SystemRoleUserEntity::getCreateId, 123).end()
//                .join(SystemUserEntity.class)
//                .on(SystemUserEntity::getId).equalTo(SystemRoleUserEntity::getUserId).end()
//                .where(dynamicSql).build()
//                .receiveResult(SystemDTO.class);
//
//        final PageInfo<SystemDTO> pageInfo = braveSql.selectPageInfo(1, 2);
//        System.out.println(pageInfo);
    }

    @Test
    public void test2() {
        final DynamicSql<SystemDTO> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(SystemDTO::getId, 1);

        final BraveSql<SystemDTO> braveSql = BraveSql.multiBuilder(SystemRoleEntity.class).alias("a")
                .join(SystemRoleUserEntity.class).alias("b")
                .on(SystemRoleEntity::getId).equalTo(SystemRoleUserEntity::getRoleId).andEqualTo(SystemRoleUserEntity::getCreateId, 123).end()
                .join(SystemUserEntity.class)
                .on(SystemUserEntity::getId).equalTo(SystemRoleUserEntity::getUserId).end()
                .where(dynamicSql).build()
                .receiveResult(SystemDTO.class);


        final PageInfo<SystemDTO> pageInfo = braveSql.selectPageInfo(1, 2);
        System.out.println(pageInfo);
    }

    @Test
    public void test3() {
        final BraveSql<SystemDTO> braveSql = BraveSql.multiBuilder(SystemRoleEntity.class).alias("a")
                .join(SystemRoleUserEntity.class).alias("b")
                .on(SystemRoleEntity::getId).equalTo(SystemRoleUserEntity::getRoleId).andEqualTo(SystemRoleUserEntity::getCreateId, 123).end()
                .join(SystemUserEntity.class)
                .on(SystemUserEntity::getId).equalTo(SystemRoleUserEntity::getUserId).end()
                .where(() -> {
                    final DynamicSql<SystemDTO> dynamicSql = DynamicSql.createDynamicSql();
                    dynamicSql.andEqualTo(SystemDTO::getId, 1);
                    return dynamicSql;
                }).build()
                .receiveResult(SystemDTO.class);
        final PageInfo<SystemDTO> pageInfo = braveSql.selectPageInfo(1, 2);
        System.out.println(pageInfo);
    }
*/
//    @Test
//    public void test4() {
//        Select<SystemDTO> select = Select.builder(SystemDTO.class)
//                .column(SystemDTO::getRoleName).end()
//                .column(SystemDTO::getRoleDesc).end()
//                .columnAll().build();
//        MultiBraveSql<SystemDTO> multiBraveSql = select.from(SystemRoleEntity.class).as("别名1").build();
//        List<SystemDTO> list = multiBraveSql.select();
//        System.out.println(list);
//    }
//
//    @Test
//    public void test5() {
//        Select<SystemDTO> select = Select.builder(SystemDTO.class)
//                .column(SystemDTO::getRoleName).end()
//                .column(SystemDTO::getRoleDesc).end()
//                .columnAll().build();
//
//        MultiBraveSql<SystemDTO> multiBraveSql = select.from(SystemRoleEntity.class).as("别名1").where(() -> {
//            DynamicSql<SystemDTO> dynamicSql = DynamicSql.createDynamicSql();
//            dynamicSql.andEqualTo(SystemDTO::getId, 1);
//            return dynamicSql;
//        }).build();
//
//        List<SystemDTO> list = multiBraveSql.select();
//        System.out.println(list);
//    }
    @Test
    public void testSql() throws Exception {
        String databaseName = DatabaseConfig.class.getCanonicalName();
        DataSourceManagement.initDataSourceConfig(DatabaseConfig.class, null);
        String sql = "select trim(replace (t.nick_name,?,?)),lpad(t.nick_name,?,?) nick_name  from t_system_user t";
        Connection connection = DataSourceManagement.initConnection(databaseName);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            final List<Object> preparedParameters = new ArrayList<>();
            preparedParameters.add("r");
            preparedParameters.add("x");
            preparedParameters.add(20);
            preparedParameters.add("*");
            preparedStatement = connection.prepareStatement(sql);
            for (int i = 1; i <= preparedParameters.size(); i++) {
                preparedStatement.setObject(i, preparedParameters.get(i - 1));
            }
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getObject(1));
                System.out.println(resultSet.getObject(2));
            }
        } finally {
            DataSourceManagement.close(databaseName, resultSet, preparedStatement, connection);
        }
    }

    /*
     *    TODO
     * 1 结果集映射规则。根据注解找到字段名，然后根据字段名匹配结果集对象字段名
     * 2 函数需要支持传入其他字段,如ifnull
     * 3 加入limit
     * 4 使用聚合函数时，接收对象不是自定义对象的 问题
     */
    @Test
    public void test6() {
        final Select.SelectBuilder<SystemDTO> selectBuilder = Select.builder(SystemDTO.class)
                .column(SystemRoleEntity::getRoleName).end()
                .column(SystemRoleEntity::getRoleName).left(1).repeat(2).subString(10, 99).dayName().lPad(1, "212121").end()
//                .column(SystemRoleEntity::getRoleDesc).trim().end()
                .column(SystemRoleUserEntity::getId).alias(SystemDTO::getSystemRoleUserEntityId).end()
//                .column(SystemRoleUserEntity::getCreateId).alias(SystemDTO::getSystemRoleUserEntityId).end()
//                .column(SystemRoleEntity::getId).alias(SystemDTO::getSystemRoleEntityId).end()
                .column(SystemRoleEntity::getId).end()
                .column("   id  ").end()
                .column("t_system_role_user.id +1  `id`  ").end()
                .allColumn(SystemRoleUserEntity.class).end()
                .allColumn(SystemRoleUserEntity.class).end();
        selectBuilder.ignoreColumn(SystemRoleEntity::getRoleName).end();
        final Select<SystemDTO> select = selectBuilder.build();

//        final Select.SelectBuilder<SystemDTO> builder = Select.builder(SystemDTO.class);
//        builder.column(null).end().
//        select.from(SystemUserEntity.class)
//                .join(SystemRoleUserEntity.class)
//                .on(SystemUserEntity::getId).equalTo(SystemRoleUserEntity::getUserId)
//                .andIsNotNull(SystemRoleUserEntity::getUserId).end()
//                .join(SystemRoleEntity.class)
//                .on(SystemRoleEntity::getId).equalTo(SystemRoleUserEntity::getRoleId).end()
//                .where(() -> {
//                    final DynamicSql<SystemDTO> dynamicSql = DynamicSql.createDynamicSql();
////                    dynamicSql.andIsNotNull(SystemDTO::getNickName);
//                    return dynamicSql;
//                });
        System.out.println("====================================================================");
        System.out.println(select);
        System.out.println("====================================================================");


        //查看缓存中的对象
//        final Map<Class<?>, TableInfo> allTableDataBaseMap = ContextApplication.getAllTableDataBaseMap();
//        allTableDataBaseMap.forEach((cls, table) -> {
//            System.out.println(cls + " = " + table);
//        });
    }

    @Test
    public void test7() {
        for (int i = 0; i < 500; i++) {
            System.out.println("================= " + i);
            final List<SystemUserEntity> select = BraveSql.build(SystemUserEntity.class).select();
            System.out.println(select);
        }
    }

}


















