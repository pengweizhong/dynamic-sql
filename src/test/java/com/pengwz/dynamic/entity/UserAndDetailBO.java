package com.pengwz.dynamic.entity;

import java.math.BigDecimal;

public class UserAndDetailBO {

    //    private String sex;
//    private String phone;
    private BigDecimal salary;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public String getSex() {
//        return sex;
//    }
//
//    public void setSex(String sex) {
//        this.sex = sex;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "UserAndDetailBO{" +
                "name='" + name + '\'' +
//                ", sex='" + sex + '\'' +
//                ", phone='" + phone + '\'' +
                ", salary=" + salary +
                '}';
    }
}


