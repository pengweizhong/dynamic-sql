# Dynamic-SQL

**注意：此项目已停止维护。**  
**推荐使用新项目 `Dynamic-SQL2`，它包含了更丰富的功能和改进的性能！**

### 停止维护的原因

随着实际生产场景需求的变化，我决定将精力转向 [Dynamic-SQL2](https://github.com/pengweizhong/dynamic-sql2)，以更好地覆盖实际应用场景。  
由于此项目的局限性、不合理的设计等原因，已不具备二次开发的价值。现有的代码仍然可以使用，但将不再提供更新或技术支持。
如有需要，您仍可参考现有代码或提交问题，我看到时可能会提供帮助。

**新项目亮点**  
🌟 功能更全面：新项目新增了诸如拦截器、插件、现代化的分页等功能。  
⚡ 性能更优：针对常见场景进行了深度优化。  
🔄 更强的兼容性：支持更广泛的数据库提供商。
  

---

---

# 项目说明

动态SQL基于JDBC，旨在简化开发人员在数据库访问层面的工作。基于此背景打造的低耦合的最小化依赖。  
同时解决贴合项目中的自定义映射、字段解析、SQL拦截、自定义ID自增功能，可完美结合各个项目中的底层通用包使用而不依赖spring环境。  
还可以根据不同的条件动态生成语句，更便捷的对数据库单表的增删改查，使得开发人员只需注重业务，提高代码的灵活性和可维护性。

**主要特性：**

1. 单表动态增删改查
2. 不依赖其他框架环境，可单独启动
3. 支持多数据源
4. 支持事务（目前仅spring环境）
5. 支持直接执行自定义SQL语句
6. 支持`Mysql`,`Oracle`
7. 提供了`spring-boot-starter`用于快速启动

# 资源引入

## Maven

``` xml
	<-- 单体项目 -->
	<dependency>
	    <groupId>com.pengwz</groupId>
	    <artifactId>dynamic-sql</artifactId>
	    <version>2.1.8</version>
	</dependency>
	
	<-- SpringBoot项目，已集成 Dynamic-SQL -->
	<dependency>
	    <groupId>com.pengwz</groupId>
	    <artifactId>dynamic-sql-spring-boot-starter</artifactId>
	    <version>2.1.8</version>
	</dependency>
```

## Gradle

```properties
implementation group: 'com.pengwz', name: 'dynamic-sql', version: '2.1.8'
```

--- 

# 快速开始

> 以下案例均可以在`test/java/com.pengwz.demo`测试包下找到。

1. 配置数据源  
   1.1 [使用`JDBC`创建数据源](#jdbcCreate)  
   1.2 使用`Druid`创建数据源
2. [配置实体类](#entityConfig)
3. 新增  
   3.1 [新增单条记录](#insertSingle)  
   3.2 [有选择的新增](#insertByChoose)     
   3.3 [新增多条记录](#insertBatch)  
   3.4 [新增或更新](#insertOrUpdate)
4. 查询  
   4.1 [使用简单函数查询总数量](#selectByFunction)   
   4.2 [根据主键查询](#selectByPrimaryKey)  
   4.3 根据条件查询
    - 4.3.1 [一般条件查询](#selectByCondition)
    - 4.3.2 [分页查询](#selectByPages)
    - 4.3.3 [嵌套查询](#selectHard)
    - 4.3.4 [分割查询](#findInSet)
5. 更新  
   5.1 [根据条件更新](#updateByCondition)  
   5.2 [根据主键更新](#updateByPrimaryKey)  
   5.3 [有选择的更新](#updateByChoose)  
   5.4 [批量更新](#updateBatch)
6. 删除  
   6.1 [根据条件删除](#deleteByCondition)  
   6.2 [根据主键删除](#deleteByPrimaryKey)

--- 

> 准备些测试数据（mysql 8.x版本及以上，否则会执行报错，若使用的mysql8以下的版本，则删除新语法即可）
> 此处简单的准备了两个表，一张用户表，一张角色表。

```sql
drop table if exists `t_user`;
CREATE TABLE `t_user`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `account_no`  varchar(50)         DEFAULT NULL COMMENT '账号',
    `username`    varchar(50)         DEFAULT NULL COMMENT '用户名',
    `password`    varchar(50)         DEFAULT NULL COMMENT '密码',
    `email`       varchar(50)         DEFAULT NULL COMMENT '邮箱',
    `birthday`    datetime            DEFAULT NULL COMMENT '生日',
    `desc`        varchar(50)         DEFAULT NULL COMMENT '邮箱',
    `is_delete`   tinyint(1)          DEFAULT NULL COMMENT '是否删除 true 已删除 false 未删除',
    `create_date` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_date` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `t_user_UN` (`account_no`)
) ENGINE = InnoDB COMMENT ='用户表';

drop table if exists `t_user_role`;
CREATE TABLE `t_user_role`
(
    `uid`         varchar(50) NOT NULL COMMENT 'UUID主键',
    `username`    varchar(50)          DEFAULT NULL COMMENT '用户名',
    `role`        varchar(50)          DEFAULT NULL COMMENT '角色',
    `create_date` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_date` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='用户和角色表';

```

---

## 1. 配置数据源

### 1.1 <span id="jdbcCreate"/>使用`JDBC`创建数据源

创建一个实体类，实现`DataSourceConfig`接口，重写`getDataSource()`方法。此处命名为`DatabaseConfig`。

```java
public class DatabaseConfig implements DataSourceConfig {
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

`DataSourceConfig`提供了两个方法：

```java
    DataSource getDataSource();

default boolean defaultDataSource() {
    return false;
}
```

- `getDataSource`是用于获取数据源连接，可以使用jdbc自带的连接池，如`MysqlDataSource`。
- `defaultDataSource` 是在多数据源情况下，用于指定默认数据源，默认值`false`
  。该条件目前仅使用spring环境，其他环境目前即使重写该值，也是没有用的。（单体项目也许有解决办法，但是目前没想到。。。）

### 1.2 <span id="druidCreate"/>使用`druid`创建数据源

首先，在pom中引入druid的依赖：

```xml

<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.4</version>
</dependency>
```

然后再配置类中构建连接参数：

```java
public class DatabaseConfig implements DataSourceConfig {
    @Override
    public DataSource getDataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl("jdbc:mysql://127.0.0.1:3306/dynamic?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
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

## 2. <span id="entityConfig"/>配置实体类

执行完上面的SQL后，随后在项目中分别创建对应的实体类

```java
// 在spring环境下设置好默认数据源后，就无需在此处声明
@Table(value = "t_user", dataSourceClass = DatabaseConfig.class)
public class UserEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createDate;
    @Column("update_date")
    private LocalDateTime updateDate;
    /** getter and setter **/
    /** toString() **/
}

@Table(value = "t_user", dataSourceClass = DatabaseConfig.class)
public class UserRoleEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String role;
    private LocalDateTime createDate;
    @Column("update_date")
    private LocalDateTime updateDate;
    /** getter and setter **/
    /** toString() **/
}
```

`@Table`注解用于维护数据表和实体类关系，该注解提供了两个属性:

- value：表示对应数据库表名，必须提供该值
- dataSourceClass：表示该表所属数据源，非spring环境必须提供

`@Id`注解用于表示那个属性对应表主键，在实体类中标识该主键后，即可用于后续根据主键查询、更新等操作，该注解非强制添加，但建议使用  
`@GeneratedValue`注解用于在新增数据后，返回主键值。该注解非强制添加，但建议使用  
`@Column`注解用于维护表列和实体类属性的关系，对于该情况下有下列处理办法：

- 当实体类属性标注了` @Column`注解，则以注解内value匹配表列；
- 若没有` @Column`注解，则以实体类的属性，根据驼峰命名规则匹配表列，如：实体类属性`createDate`匹配表列`create_date`。

> `dymamic-sql` 对实体类类型映射进行了加强，比如日期类在实体类中可以使用`java.util.Date`
> 接收，或者使用`java.sql.Date`、`java.time.LocalDateTime`接收等等。
> **实体类属性必须使用引用类型，因为基本类型在任何时候都不等于null**

## 3. 新增

### 3.1 <span id="insertSingle"/>新增单条记录

为`t_user`表新增一条数据

```java

@Test
public void insert() {
    UserEntity entity = new UserEntity();
    entity.setUsername("tom");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    entity.setPassword("password");
    Integer insert = BraveSql.build(UserEntity.class).insert(entity);
    //1
    System.out.println(insert);
}
```

执行结果：  
[![gkU3LT.png](https://z3.ax1x.com/2021/04/29/gkU3LT.png)](https://imgtu.com/i/gkU3LT)  
也可以对`UserEntity`原始对象进行输出，其自增ID已经映射到该实体类中，方便在业务中进行其他操作。此处便不再打印输出结果。

### 3.2 <span id="insertByChoose"/>有选择的新增

```java

@Test
public void insertActive() {
    UserEntity entity = new UserEntity();
    entity.setUsername("jerry");
    entity.setPassword("password");
    Integer insert = BraveSql.build(UserEntity.class).insertActive(entity);
    //1
    System.out.println(insert);
}
```

执行结果：  
[![gkaPk4.png](https://z3.ax1x.com/2021/04/29/gkaPk4.png)](https://imgtu.com/i/gkaPk4)
`BraveSql.build(UserEntity.class).insert(entity)`和`BraveSql.build(UserEntity.class).insertActive(entity)`区别在于`insert`
插入值时，如果该属性为null，则插入null; `insertActive`插入值时，如果该属性为null，则使用数据库默认值。

### 3.3 <span id="insertBatch"/>新增多条记录

批量插入10条记录

```java

@Test
public void batchInsert() {
    List<UserEntity> userEntities = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        UserEntity entity = new UserEntity();
        entity.setUsername("list_" + i);
        entity.setPassword("password_" + i);
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        userEntities.add(entity);
    }
    Integer insert = BraveSql.build(UserEntity.class).batchInsert(userEntities);
    //10
    System.out.println(insert);
}
```

执行结果：  
[![gkdkVS.png](https://z3.ax1x.com/2021/04/29/gkdkVS.png)](https://imgtu.com/i/gkdkVS)

### 3.4 <span id="insertOrUpdate"/>新增或更新

新增或更新，主要场景用于数据新增时，不确定数据是否存在，可以使用`insertOrUpdate`方法，避免插入前还需要查询数据是否存在。
判断原始数据是否存在是以主键约束、唯一约束等等来决定数据是插入还是新增。  
如：插入Id为1的数据（Id为1的数据表中已经存在）

```java

@Test
public void insertOrUpdate() {
    UserEntity entity = new UserEntity();
    entity.setId(1L);
    entity.setUsername("tom_update");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    entity.setPassword("password_update");
    Integer insert = BraveSql.build(UserEntity.class).insertOrUpdate(entity);
    System.out.println(insert);
}
```

执行结果：  
[![gkw3OP.png](https://z3.ax1x.com/2021/04/29/gkw3OP.png)](https://imgtu.com/i/gkw3OP)

## 4. 查询

### 4.1 <span id="selectByFunction"/>使用简单函数查询

```java

@Test
public void test1() {
    //查询总数量
    Integer count = BraveSql.build(UserEntity.class).selectCount();
    System.out.println(count);
    //对所有ID求和
    BigDecimal sum = BraveSql.build(UserEntity.class).selectSum(UserEntity::getId);
    System.out.println(sum);
    // ......
}
 ```

### 4.2 <span id="selectByPrimaryKey"/>根据主键查询

```java

@Test
public void test2() {
    UserEntity userEntity = BraveSql.build(UserEntity.class).selectByPrimaryKey(1);
    System.out.println(userEntity);
}
 ```

### 4.3 根据条件查询

#### 4.3.1 <span id="selectByCondition"/>一般条件查询

```java

@Test
public void test3() {
    //查询用户名=jerry的数据
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    //可以直接使用字段名进行查询，但是i推荐使用表达式
    //dynamicSql.andEqualTo("username","jerry");
    dynamicSql.andEqualTo(UserEntity::getUsername, "jerry");
    List<UserEntity> entities = BraveSql.build(dynamicSql, UserEntity.class).select();
    System.out.println(entities);
}
 ```

#### 4.3.2 <span id="selectByPages"/>分页查询

```java

@Test
public void test4() {
    //按5个为一组分页，取第一页
    PageInfo<UserEntity> pageInfo = BraveSql.build(UserEntity.class).selectPageInfo(1, 5);
    Assert.assertEquals(1, (int) pageInfo.getPageIndex());
    Assert.assertEquals(5, (int) pageInfo.getPageSize());
    Assert.assertEquals(5, pageInfo.getResultList().size());
}
 ```

#### 4.3.3 <span id="selectHard"/>嵌套查询

```java

@Test
public void test5() {
    //查询用户名为jerry的用户，或者Id等于5或者50的数据
    //ID=50是不存在的，所以只会查询出2条数据
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    dynamicSql.andEqualTo(UserEntity::getUsername, "jerry");
    //使用此方法创建组查询，此方法可以根据业务无限制嵌套
    dynamicSql.orComplex(sql -> sql.andEqualTo(UserEntity::getId, 5).orEqualTo(UserEntity::getId, 50)//.orComplex()：这里扔可以选择嵌套括号组
    );
    List<UserEntity> entities = BraveSql.build(dynamicSql, UserEntity.class).select();
    System.out.println(entities);
    System.out.println(entities.size());
}
 ```

#### 4.3.4 <span id="findInSet"/>分割查询

```java

/**
 *默认使用英文逗号对词组拆词进行查询
 */
@Test
public void findInSet3() {
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    dynamicSql.orFindInSet(UserEntity::getUsername, "世界");
    List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
    System.out.println(select);
}

/**
 *也可以自定义分隔符查询，例如空格、下划线等等
 */
@Test
public void findInSet5() {
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    dynamicSql.orFindInSet(UserEntity::getUsername, "世界", "-");
    List<UserEntity> select = BraveSql.build(dynamicSql, UserEntity.class).select();
    System.out.println(select);
}

```

## 5. 更新

### 5.1 <span id="updateByCondition"/>根据条件更新

```java

@Test
public void test1() {
    //将用户名 list_0 改为 tom
    UserEntity userEntity = new UserEntity();
    userEntity.setUsername("tom");
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    dynamicSql.andEqualTo(UserEntity::getUsername, "list_0");
    //这里会生成语句： update `t_user` set `username` = ? where `username` = ?
    int updated = BraveSql.build(dynamicSql, UserEntity.class).updateActive(userEntity);
    Assert.assertEquals(updated, 1);
}
 ```

### 5.2 <span id="updateByPrimaryKey"/>根据主键更新

```java  

@Test
public void test2() {
    //将ID=3的用户名改为duck
    UserEntity userEntity = new UserEntity();
    userEntity.setUsername("duck");
    //设置主键值
    userEntity.setId(3L);
    userEntity.setCreateDate(LocalDateTime.now());
    userEntity.setUpdateDate(LocalDateTime.now());
    int updated = BraveSql.build(UserEntity.class).updateByPrimaryKey(userEntity);
    Assert.assertEquals(updated, 1);
}  
```  

### 5.3 <span id="updateByChoose"/>批量更新

```java

@Test
public void test3() {
    //将所有用户的密码改为 123@abc
    UserEntity userEntity = new UserEntity();
    userEntity.setPassword("123@abc");
    BraveSql.build(UserEntity.class).updateActive(userEntity);
}
```

## 6. 删除

### 6.1 <span id="deleteByCondition"/>根据条件删除

```java

@Test
public void test1() {
    //删除ID等于7、8、9的用户
    DynamicSql<UserEntity> dynamicSql = DynamicSql.createDynamicSql();
    dynamicSql.andIn(UserEntity::getId, Arrays.asList(7, 8, 9));
    BraveSql.build(dynamicSql, UserEntity.class).delete();
}
```

### 6.2 <span id="deleteByPrimaryKey"/>根据主键删除

```java

@Test
public void test2() {
    //删除ID等于 10的用户
    Integer deleted = BraveSql.build(UserEntity.class).deleteByPrimaryKey(10);
    Assert.assertTrue(deleted == 1);
}
```









