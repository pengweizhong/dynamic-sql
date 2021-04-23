package com.pengwz.dynamic.model;

import javax.sql.DataSource;

public class DataSourceInfo {
    private String classPath;
    private String classBeanName;
    private DataSource dataSource;
    private String dataSourceBeanName;
    private boolean isDefault;

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public String getClassBeanName() {
        return classBeanName;
    }

    public void setClassBeanName(String classBeanName) {
        this.classBeanName = classBeanName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSourceBeanName() {
        return dataSourceBeanName;
    }

    public void setDataSourceBeanName(String dataSourceBeanName) {
        this.dataSourceBeanName = dataSourceBeanName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public String toString() {
        return "DataSourceInfo{" +
                "classPath='" + classPath + '\'' +
                ", classBeanName='" + classBeanName + '\'' +
                ", dataSource=" + dataSource +
                ", dataSourceBeanName='" + dataSourceBeanName + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
