package com.pengwz.dynamic.dto;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DatabaseConfig;
import com.pengwz.dynamic.entity.SystemRoleEntity;
import com.pengwz.dynamic.entity.SystemUserEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data()
@AllArgsConstructor
@NoArgsConstructor
//@SuperBuilder
@Builder
@ToString(callSuper = true)
@Table(value = "12", dataSourceClass = DatabaseConfig.class)
public class SystemDTO extends BaseDTO {
    @Column(dependentTableClass = SystemUserEntity.class)
    private Integer id;
    /**
     * 姓名
     */
    @Column(dependentTableClass = SystemUserEntity.class)
    private String nickName;

    /**
     * 手机号
     */
    @Column(dependentTableClass = SystemUserEntity.class)
    private String phone;
    /**
     * 角色名称
     */
    @Column(value = "role_name", dependentTableClass = SystemRoleEntity.class)
    private String roleName;

    /**
     * 角色描述
     */
    @Column(dependentTableClass = SystemRoleEntity.class)
    private String roleDesc;

    /**
     * 创建人id
     */
    @Column(dependentTableClass = SystemRoleEntity.class)
    private Integer createId;

    /**
     * 更新人id
     */
    @Column(dependentTableClass = SystemRoleEntity.class)
    private Integer updateId;

    /**
     * 创建时间
     */
    @Column(dependentTableClass = SystemRoleEntity.class)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(dependentTableClass = SystemRoleEntity.class)
    private LocalDateTime updateTime;

//    private Integer id;
//
//    /**
//     * 角色id
//     */
//    private Integer roleId;
//
//    /**
//     * 用户id
//     */
//    private Integer userId;
//
//    /**
//     * 创建人id
//     */
//    private Integer createId;
//
//    /**
//     * 更新人id
//     */
//    private Integer updateId;
//
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 更新时间
//     */
//    private LocalDateTime updateTime;
//
//    private Integer id;
//
//    /**
//     * 姓名
//     */
//    private String nickName;
//
//    /**
//     * 手机号
//     */
//    private String phone;
//
//    /**
//     * 邮箱
//     */
//    private String email;
//
//    /**
//     * 创建人id
//     */
//    private Integer createId;
//
//    /**
//     * 更新人id
//     */
//    private Integer updateId;
//
//    /**
//     * 创建时间
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 更新时间
//     */
//    private LocalDateTime updateTime;

}
