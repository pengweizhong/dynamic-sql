# 单表动态查询 Dynamic-sql

## 版本号 1.0.0(初版)

### 项目简述
    1、基于JDBC单表增删改查，可单独启动，不依赖环境
    2、目前仅支持Mysql
    3、支持多数据源
    4、支持事务（目前不提供主动事务，考虑后期优化，可以使用其他框架加入事务，如Spring事务）
    5、目前不支持数据库，等下期优化；或自己写连接池，或接入优秀的框架
    
### 本次发布的功能
    查询、增加、删除、修改
#### 简单的例子

##### 接入数据源
    
           