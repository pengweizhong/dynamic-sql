package com.pengwz.demo.entities;

import com.pengwz.demo.config.DatabaseConfig;
import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.anno.Table;

import java.time.LocalDateTime;

@Table(value = "t_user", dataSourceClass = DatabaseConfig.class)
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createDate;
    @Column("update_date")
    private LocalDateTime updateDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
