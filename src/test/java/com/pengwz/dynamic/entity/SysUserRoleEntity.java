package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DatabaseConfig;

import java.time.LocalDateTime;

@Table(value = "t_sys_user_role", dataSourceClass = DatabaseConfig.class)
public class SysUserRoleEntity {
    /**
     * 用户id
     */
    @Column("user_id")
    private Integer userId;

    /**
     * 角色id
     */
    @Column("role_id")
    private Integer roleId;

    /**
     * 创建时间
     */
    @Column("create_date")
    private LocalDateTime createDate;

    /**
     * 创建人
     */
    @Column("create_by")
    private String createBy;

    /**
     * 修改时间
     */
    @Column("update_date")
    private LocalDateTime updateDate;

    /**
     * 修改人
     */
    @Column("update_by")
    private String updateBy;

    /**
     * 逻辑删除标志 1：正常  0:失效
     */
    private Boolean isValid;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Boolean getValid() {
        return isValid;
    }

    public void setValid(Boolean valid) {
        isValid = valid;
    }

    @Override
    public String toString() {
        return "SysUserRoleEntity{" +
                "userId=" + userId +
                ", roleId=" + roleId +
                ", createDate=" + createDate +
                ", createBy='" + createBy + '\'' +
                ", updateDate=" + updateDate +
                ", updateBy='" + updateBy + '\'' +
                ", isValid=" + isValid +
                '}';
    }
}