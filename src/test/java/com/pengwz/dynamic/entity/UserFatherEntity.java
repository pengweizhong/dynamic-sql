package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.*;
import com.pengwz.dynamic.config.DatabaseConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table(value = "t_user", dataSourceClass = DatabaseConfig.class)
public class UserFatherEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column("id")
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserFatherEntity{" +
                "id=" + id +
                '}';
    }
}
