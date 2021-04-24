# 单表动态查询 Dynamic-sql
    
## 最新版本号 1.1.4
```xml
<-- 单体项目 -->
<dependency>
    <groupId>com.pengwz</groupId>
    <artifactId>dynamic-sql</artifactId>
    <version>1.1.4</version>
</dependency>
<-- springBoot项目，已集成dynamic-sql -->
<dependency>
    <groupId>com.pengwz</groupId>
    <artifactId>dynamic-spring-boot-starter</artifactId>
    <version>1.1.4</version>
</dependency>

```
### 本次版本变更内容
    1、新增dynamic-spring-boot-starter
    2、新增BraveSql.insertActive()方法，新增时为null的字段将使用数据库默认值
    3、修复执行BraveSql.executeSelectSqlAndReturnSingle方法时，无法映射java.util.Date的问题，并且新增了获取实体类中@colum逻辑

### 项目简述
    1、基于JDBC单表动态增删改查，可单独启动，不依赖环境
    2、目前仅支持Mysql
    3、支持多数据源
    4、支持事务（spring环境）
    5、支持多数据库连接池
    
### 主要功能描述
    查询、增加、删除、修改
#### 快速开始

1、新建自定义的数据源，实现DataSourceConfig接口，并重写getProperties()方法，此处命名为MyDBConfig，如：
```java
public class MyDBConfig implements DataSourceConfig {
    @Override
    public DataSource getDataSource() {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl("jdbc:mysql://127.0.0.1:3306/dynamic?useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
        ds.setUser("root");
        ds.setPassword("pengwz");
        return ds;
    }
}
```
或者使用第三方的数据库连接池，如：阿里巴巴的Druid  
引入Druid的依赖包
```xml
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.2.4</version>
        </dependency>
```
配置Druid数据源  
```java
public class MyDBConfig implements DataSourceConfig {
    @Override
    public DataSource getDataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl("jdbc:mysql://127.0.0.1:3306/dynamic?useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
        ds.setUsername("root");
        ds.setPassword("pengwz");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setInitialSize(3);
        ds.setMaxActive(10);
        ds.setMinIdle(5);
        ds.setValidationQuery("select 1");
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(false);
        ds.setUseUnfairLock(true);
        ds.setTestWhileIdle(true);
        ds.setMinEvictableIdleTimeMillis(10 * 60 * 1000L);
        ds.setTimeBetweenEvictionRunsMillis(5 * 60 * 1000L);
        return ds;
    }
}
```
2、在指定的数据源中，新建一个表，此处命名为t_user，此处为phone列创建了一个索引。
```mysql
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) DEFAULT NULL COMMENT '名称',
  `sex` char(2) DEFAULT NULL COMMENT '性别',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `birthday` date DEFAULT NULL COMMENT '生日',
  `create_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `t_user_un` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=328 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

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
    private String phone;
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
   
##### 主要功能示例
1、批量新增数据  
新增10条测试数据
```java
    @Test
    public void batchInsert() {
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity data = new UserEntity();
            data.setSex("男");
            data.setUsername("海绵宝宝" + i);
            data.setBirthday(LocalDate.now());
            data.setCreateDate(LocalDateTime.now());
            data.setUpdateDate(LocalDateTime.now());
            userEntities.add(data);
        }
        BraveSql.build(dynamicSql, UserEntity.class).batchInsert(userEntities);
        userEntities.forEach(System.out::println);
    }

```
    运行结果
    UserEntity{id=1, username='海绵宝宝0', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=2, username='海绵宝宝1', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=3, username='海绵宝宝2', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=4, username='海绵宝宝3', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=5, username='海绵宝宝4', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=6, username='海绵宝宝5', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=7, username='海绵宝宝6', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=8, username='海绵宝宝7', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=9, username='海绵宝宝8', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}
    UserEntity{id=10, username='海绵宝宝9', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10.045, updateDate=2021-02-22T15:29:10.045}ntity{id=2, username='jerry', birthday=1994-01-22, createDate=2021-01-23T01:10:23, updateDate=2021-01-23T01:10:23}
2、查询全部的数据   
```java
    @Test
    public void selectAll(){
        List<UserEntity> select = BraveSql.build(UserEntity.class).select();
        select.forEach(System.out::println);
    }
```
    运行结果
    UserEntity{id=1, username='海绵宝宝0', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=2, username='海绵宝宝1', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=3, username='海绵宝宝2', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=4, username='海绵宝宝3', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=5, username='海绵宝宝4', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=6, username='海绵宝宝5', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=7, username='海绵宝宝6', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=8, username='海绵宝宝7', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=9, username='海绵宝宝8', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
    UserEntity{id=10, username='海绵宝宝9', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}3、根据主键查询
3、根据主键查询  
查询主键等于1的数据
```java
    @Test
    public void selectByPrimaryKey(){
        UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
        System.out.println(userEntity);
    }
``` 
    运行结果
    UserEntity{id=1, username='海绵宝宝0', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
4、根据条件查询  
查询用户名字是海绵宝宝6的数据
```java
    @Test
    public void selectByCondition(){
        DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
        dynamicSql.andEqualTo(UserEntity::getUsername,"海绵宝宝6");
        UserEntity userEntity = BraveSql.build(dynamicSql, UserEntity.class).selectSingle();
        System.out.println(userEntity);
    }
```   
    运行结果
    UserEntity{id=7, username='海绵宝宝6', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}
5、分页查询  
查询第二页的两条数据
```java
    @Test
    public void selectPageInfo() {
        PageInfo<UserEntity> userEntityPageInfo = BraveSql.build(UserEntity.class).selectPageInfo(2, 2);
        System.out.println(userEntityPageInfo);
    }
```
    运行结果
    PageInfo{pageIndex=2, pageSize=2, realPageSize=2, totalPages=5, totalSize=10, resultList=[UserEntity{id=3, username='海绵宝宝2', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}, UserEntity{id=4, username='海绵宝宝3', sex='男', birthday=2021-02-22, phone='null', createDate=2021-02-22T15:29:10, updateDate=2021-02-22T15:29:10}]}

其中，DynamicSql主要用于创建where条件，BraveSql用于操作数据库。  
(文档待更新... ...  )

    