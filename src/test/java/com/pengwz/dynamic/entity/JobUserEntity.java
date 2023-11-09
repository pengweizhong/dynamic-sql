package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.ColumnJson;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DatabaseConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.time.LocalTime;

@Table(value = "test2.job_user_1", dataSourceClass = DatabaseConfig.class)
public class JobUserEntity implements Serializable {

    private transient Log log = LogFactory.getLog(getClass());
    @Id
    @GeneratedValue
    private Integer id;
    private String username;
    private String password;
    private String role;
    private String hobby;
    @ColumnJson(value = "permission")
    private JobUserEntity permission;
    private LocalTime times;

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public LocalTime getTimes() {
        return times;
    }

    public void setTimes(LocalTime times) {
        this.times = times;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public JobUserEntity getPermission() {
        return permission;
    }

    public void setPermission(JobUserEntity permission) {
        this.permission = permission;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    @Override
    public String toString() {
        return "JobUserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", hobby='" + hobby + '\'' +
                ", permission=" + permission +
                ", times=" + times +
                '}';
    }
}
