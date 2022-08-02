package com.pengwz.dynamic.dto;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.config.DatabaseConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(dataSourceClass = DatabaseConfig.class)
public class SystemDTO {
    @Column("id")
    private Integer id;

    /**
     * 角色名称
     */
    @Column(value = "role_name", tableAlias = "t1")
    private String roleName;

    /**
     * 角色描述
     */
    private String roleDesc;

    /**
     * 创建人id
     */
    private Integer createId;

    /**
     * 更新人id
     */
    private Integer updateId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
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
