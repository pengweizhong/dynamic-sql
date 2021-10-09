package com.pengwz.dynamic.entity;

import com.pengwz.dynamic.anno.ColumnJson;
import com.pengwz.dynamic.anno.GeneratedValue;
import com.pengwz.dynamic.anno.Id;
import com.pengwz.dynamic.anno.Table;
import com.pengwz.dynamic.config.DatabaseConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Table(value = "test2.job_user_1", dataSourceClass = DatabaseConfig.class)
public class JobUserEntity {
    private transient Log log = LogFactory.getLog(getClass());
    @Id
    @GeneratedValue
    private Integer id;
    private String username;
    private String password;
    private String role;
    @ColumnJson(value = "permission")
    private JobUserEntity permission;

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

    @Override
    public String toString() {
        return "JobUserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }
}
