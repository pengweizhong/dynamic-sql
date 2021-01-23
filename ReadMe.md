# 单表动态查询 Dynamic-sql
    目前项目刚刚创建，有时间的话就会去维护并更新，所以可能会有bug，烦请路过的大佬多多指教。
## 版本号 1.0.0(初版)
```xml
<dependency>
  <groupId>com.pengwz</groupId>
  <artifactId>dynamic-sql</artifactId>
  <version>1.0.0</version>
</dependency>
```
### 项目简述
    1、基于JDBC单表增删改查，可单独启动，不依赖环境
    2、目前仅支持Mysql
    3、支持多数据源（目前不支持默认数据源，需要明确指定）
    4、支持事务（目前不提供主动事务，考虑后期优化，可以使用其他框架加入事务，如Spring事务）
    5、目前不支持数据库连接池，等下期优化；或自己写连接池，或接入优秀的框架
    
### 本次发布的功能
    查询、增加、删除、修改
#### 下面举一个查询的示例

##### 准备工作
1、新建自定义的数据源，实现DataSourceConfig接口，并重写getProperties()方法，此处命名为MyDBConfig，如：
```java
public class MyDBConfig implements DataSourceConfig {
    @Override
    public Properties getProperties() {
        //必须的配置
        Properties properties = new Properties();
        properties.setConfig(DRIVER, "com.mysql.jdbc.Driver");
        properties.setConfig(USERNAME, "root");
        properties.setConfig(PASSWORD, "pengwz");
        properties.setConfig(PORT, "3306");
        properties.setConfig(HOST, "127.0.0.1");
        properties.setConfig(DATABASE, "dynamic");
        //其他参数，比如设置时区，字符集等
        Map<String, String> otherConfigMap = new HashMap<>();
        otherConfigMap.put("serverTimezone", "GMT%2B8");
        otherConfigMap.put("useUnicode", "true");
        otherConfigMap.put("characterEncoding", "utf-8");
        otherConfigMap.put("rewriteBatchedStatements", "true");
        properties.setOtherConfigMap(otherConfigMap);
        return properties;
    }
}
```
2、在指定的数据源中，新建一个表，此处命名为t_user，然后给这个表初始化两条记录，用于测试数据。
```mysql
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `sex` char(2) DEFAULT NULL COMMENT '性别',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO t_user (id, name, sex, birthday, create_date, update_date) VALUES (1, 'tom', '男', '1993-07-14', null, null);
INSERT INTO t_user (id, name, sex, birthday, create_date, update_date) VALUES (2, 'jerry', '女', '1994-01-22', null, null);

```
4、新建一个实体类，用于和数据库交互，此处命名为UserEntity
```java
@Table(value = "t_user", dataSourceClass = MyDBConfig.class)
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Column("name")
    private String username;
    private String sex;
    private LocalDate birthday;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    /* getter and setter */
    /* toString() */
}
```
其中：  
@Table注解用在实体类上，value表示对应的表名，dataSourceClass表示该表所属数据源。目前dataSourceClass必须明确指定数据源，后期会支持默认数据源；  
@Id必须声明，表示该表所对应的主键；  
@GeneratedValue是可选项，表示在插入数据的操作中，是否返回该记录的主键，该注解加在主键属性上时才会生效；  
@Column是可选项，表示该属性所对应表的字段，若不声明@Column，则默认使用驼峰命名规则匹配表字段，若声明则以注解内值为准。  
此外，Dynamic-sql自带类型转换功能，比如当您的表字段类型为date，在实体类希望使用LocalDate接收，程序会自动转换类型。  
   
##### 查询示例
1、查询全部
```java
    List<UserEntity> select = BraveSql.build(UserEntity.class).select();
    select.forEach(System.out::println);

```
    运行结果
    UserEntity{id=1, username='tom', birthday=1993-07-14, createDate=2021-01-23T01:09:51, updateDate=2021-01-23T01:09:51}
    UserEntity{id=2, username='jerry', birthday=1994-01-22, createDate=2021-01-23T01:10:23, updateDate=2021-01-23T01:10:23}
2、带条件的查询，如查询性别等于男的数据   
```java
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    dynamicSql.andEqualTo(UserEntity::getSex, "男");
    List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
    select.forEach(System.out::println);
```
    运行结果
    UserEntity{id=1, username='tom', sex='男', birthday=1993-07-14, createDate=2021-01-23T01:09:51, updateDate=2021-01-23T01:09:51}
3、根据主键查询
```java
    UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(2);
    System.out.println(userEntity);
``` 
    运行结果
    UserEntity{id=2, username='jerry', sex='女', birthday=1994-01-22, createDate=2021-01-23T01:10:23, updateDate=2021-01-23T01:10:23}
4、分页查询
```java
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(1, 1);
        System.out.println(userEntityPageInfo);
```
    运行结果
    PageInfo{pageIndex=1, pageSize=1, realPageSize=1, totalPages=2, totalSize=2, resultList=[UserEntity{id=1, username='tom', sex='男', birthday=1993-07-14, createDate=2021-01-23T01:09:51, updateDate=2021-01-23T01:09:51}]}

其中，DynamicSql主要用于创建where条件，BraveSql用于操作数据库。  
#### 写在后面的话
    由于本人很菜，所以代码中很多不规则的地方，比如变量命名不规范、代码冗余等等。希望路过的大佬多多指教，一起学习进步，谢谢~  
    文档方面，有时间的话后续会补全的~
    