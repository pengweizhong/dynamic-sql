package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.MyDBConfig;

@Table(value = "t_string_primary_key", dataSourceClass = MyDBConfig.class)
public class StringPrimaryKeyEntity {
    @Id
    @GeneratedValue
    private String uuid;
    private String name;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "StringPrimaryKeyEntity{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
