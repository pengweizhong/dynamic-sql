package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.config.DatabaseConfig;
import com.pengwz.dynamic.dto.SystemDTO;
import com.pengwz.dynamic.entity.SystemRoleEntity;
import com.pengwz.dynamic.entity.SystemRoleUserEntity;
import com.pengwz.dynamic.entity.SystemUserEntity;
import org.junit.Test;

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

    /**
     * 对比SQL：

     select
     a.*,
     b.*,
     c.*
     from
     t_system_user a
     join t_system_role_user b on
     a.id = b.user_id
     or a.id = 1
     join t_system_role c on
     b.role_id = c.id
     where
     a.id = 1

     */
    @Test
    public void test() {
        final DynamicSql<SystemDTO> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(SystemDTO::getId, 1);

        final BraveSql<SystemDTO> braveSql = MultiBraveSql.builder(SystemRoleEntity.class).alias("a")
                .join(SystemRoleUserEntity.class).alias("b")
                .on(SystemRoleEntity::getId).equalTo(SystemRoleUserEntity::getRoleId).andEqualTo(SystemRoleUserEntity::getCreateId, 123).end()
                .join(SystemUserEntity.class)
                .on(SystemUserEntity::getId).equalTo(SystemRoleUserEntity::getUserId).end()
                .where(dynamicSql).build()
                .receiveResult(SystemDTO.class);

        final PageInfo<SystemDTO> pageInfo = braveSql.selectPageInfo(1, 2);

        System.out.println(pageInfo);

    }


}


















