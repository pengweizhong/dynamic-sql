package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.Column;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.MyDBConfig;

import java.math.BigDecimal;

@Table(value = "t_user_detail", dataSourceClass = MyDBConfig.class)
public class UserDetailEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column("name")
    private String username;
    private BigDecimal salary;

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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "UserDetailEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", salary=" + salary +
                '}';
    }
}
