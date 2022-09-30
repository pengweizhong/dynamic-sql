package com.pengwz.dynamic.dto;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data()
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(callSuper = true)
//@Table(isCache = false)
public class SystemDTO extends BaseDTO {

    private Integer systemRoleUserEntityId;
    private Integer systemRoleEntityId;
    /**
     * 姓名
     */
    private String nickName;
    /**
     * 手机号
     */
    private String phone;

    private Integer userId;

    /**
     * 角色id
     */
//    @Column(value = "id", dependentTableClass = SystemRoleEntity.class)
    private Integer roleId;
    /**
     * 角色名称
     */
//    @Column(dependentTableClass = SystemRoleEntity.class)
    private String roleName;

    /**
     * 角色描述
     */
//    @Column(dependentTableClass = SystemRoleEntity.class)
    private String roleDesc;


}
