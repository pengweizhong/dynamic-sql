package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.MyDBConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Table(value = "t_dynamic",dataSourceClass = MyDBConfig.class)
public class DynamicEntity {
    @Id
    @GeneratedValue
    @Column("id")
    private Long id;
    @Column("name")
    private String name;
    @Column("test_one")
    private String testOne;
    private String testTwo;
    @Column("brithday")
    private LocalDate date;
    private LocalDateTime createDate;
    private BigDecimal money;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTestOne() {
        return testOne;
    }

    public void setTestOne(String testOne) {
        this.testOne = testOne;
    }

    public String getTestTwo() {
        return testTwo;
    }

    public void setTestTwo(String testTwo) {
        this.testTwo = testTwo;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "DynamicEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", testOne='" + testOne + '\'' +
                ", testTwo='" + testTwo + '\'' +
                ", date=" + date +
                ", createDate=" + createDate +
                ", money=" + money +
                '}';
    }
}
