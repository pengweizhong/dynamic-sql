package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.DatabaseConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(value = "t_user", dataSourceClass = DatabaseConfig.class)
public class UserEntity {
    //      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column("id")
    private Long id;
    //  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
    @Column("account_no")
    private String accountNo;
    @Column("username")
    private String username;
    //  `password` varchar(50) DEFAULT NULL COMMENT '密码',
    @Column("password")
    private String password;
    //    `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
    @Column("email")
    private String email;
    // `desc` varchar(50) DEFAULT NULL COMMENT '邮箱',
    @Column("desc")
    private String desc;
    //    `birthday` datetime DEFAULT NULL COMMENT '生日',
    @Column("birthday")
    private LocalDate birthday;
    //    `is_delete` tinyint(1)  DEFAULT NULL COMMENT '是否删除 true 已删除 false 未删除',
    @Column("is_delete")
    private Boolean isDelete;
    //  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    @Column("create_date")
    private LocalDateTime createDate;
    //  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    @Column("update_date")
    private LocalDateTime updateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(Boolean delete) {
        isDelete = delete;
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
        return "UserEntity{" +
                "id=" + id +
                ", accountNo='" + accountNo + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", desc='" + desc + '\'' +
                ", birthday=" + birthday +
                ", isDelete=" + isDelete +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
