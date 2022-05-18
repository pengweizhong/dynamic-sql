package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.DatabaseConfig;

import java.time.LocalDateTime;

@Table(value = "t_user_role", dataSourceClass = DatabaseConfig.class)
public class UserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SIMPLE_UUID)
    @Column("uid")
    private String uid;
    @Column("account_no")
    private String accountNo;
    @Column("role")
    private String role;
    @Column("create_date")
    private LocalDateTime createDate;
    @Column("update_date")
    private LocalDateTime updateDate;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "UserRoleEntity{" +
                "uid='" + uid + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", role='" + role + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
