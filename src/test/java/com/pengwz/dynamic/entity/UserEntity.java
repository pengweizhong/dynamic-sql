package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.MyDBConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Table(value = "t_user", dataSourceClass = MyDBConfig.class)
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column("name")
    private String username;
    private String sex;
    private LocalDate birthday;
    private String phone;
    private LocalDateTime createDate;
    private Date updateDate;

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

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", sex='" + sex + '\'' +
                ", birthday=" + birthday +
                ", phone='" + phone + '\'' +
                ", createDate=" + createDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
