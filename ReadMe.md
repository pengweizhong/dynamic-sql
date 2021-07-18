
# 致谢
   本项目是受到 `abel533`大神的启发，了解了他很多优秀的项目，其中也有我很感兴趣的单表动态增删改查，于是我在闲暇之余，借鉴了该项目，基于`JDBC`开发了`dynamic-sql`。虽功能不及`tk-mybatis`强大，但是凭借个人爱好和对大佬的膜拜，写下本项目，也算是在平常工作或学习中做了一些小小的总结吧~

> tk-mybatis地址：
> https://gitee.com/free/Mapper/wikis/Home?sort_id=14104

---

# 项目说明
1. 单表动态增删改查
2. 不依赖其他框架环境，可单独启动
3. 支持多数据源
4. 支持事务（目前仅spring环境）
5. 支持直接执行自定义SQL语句
6. 目前仅支持`mysql`
7. 提供了`spring-boot`环境的版本

# maven版本

``` xml

	<-- 单体项目 -->
	<dependency>
	    <groupId>com.pengwz</groupId>
	    <artifactId>dynamic-sql</artifactId>
	    <version>2.0.0</version>
	</dependency>
	
	<-- springBoot项目，已集成dynamic-sql -->
	<dependency>
	    <groupId>com.pengwz</groupId>
	    <artifactId>dynamic-sql-spring-boot-starter</artifactId>
	    <version>2.0.0</version>
	</dependency>

```

- **1.1.6之前的版本请勿使用**，因为之前的版本有很多bug，而且好多东西我还没想好就上传了，目前maven仓库好像不支持删除已经上传的jar的... ... 以后真的是应该先跑下case再上传比较好 [手动尴尬] ... ...

--- 

# 目录
1. 配置数据源  
	1.1 [使用`JDBC`创建数据源](#jdbcCreate)  
	1.2 [使用`druid`创建数据源](#druidCreate)  
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
	  - 4.3.2 [复杂查询](#selectHard)  
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
CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) DEFAULT NULL COMMENT '密码',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT ='用户表';

drop table if exists `t_user_role`;
CREATE TABLE `t_user_role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `role` varchar(50) DEFAULT NULL COMMENT '角色',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT ='用户和角色表';

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
- `defaultDataSource` 是在多数据源情况下，用于指定默认数据源，默认值`false`。该条件目前仅使用spring环境，其他环境目前即使重写该值，也是没有用的。（单体项目也许有解决办法，但是目前没想到。。。）


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
        ds.setUrl("jdbc:mysql://127.0.0.1:3306/project-demo?useOldAliasMetadataBehavior=true&useUnicode=true&rewriteBatchedStatements=true&serverTimezone=GMT%2B8&characterEncoding=utf-8");
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


> `dymamic-sql` 对实体类类型映射进行了加强，比如日期类在实体类中可以使用`java.util.Date`接收，或者使用`java.sql.Date`、`java.time.LocalDateTime`接收等等。
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
`BraveSql.build(UserEntity.class).insert(entity)`和`BraveSql.build(UserEntity.class).insertActive(entity)`区别在于`insert`插入值时，如果该属性为null，则插入null; `insertActive`插入值时，如果该属性为null，则使用数据库默认值。
### 3.3 <span id="insertBatch"/>新增多条记录
批量插入10条记录
```java
    @Test
    public void batchInsert() {
        List<UserEntity> userEntities = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserEntity entity = new UserEntity();
            entity.setUsername("list_"+i);
            entity.setPassword("password_"+i);
            entity.setCreateDate(LocalDateTime.now());
            entity.setUpdateDate(LocalDateTime.now());
            userEntities.add(entity);
        }
        Integer insert  = BraveSql.build(UserEntity.class).batchInsert(userEntities);
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
### 4.1 <span id="selectByFunction"/>使用简单函数查询总数量
> TODO
### 4.2 <span id="selectByPrimaryKey"/>根据主键查询
> TODO
### 4.3 根据条件查询
#### 4.3.1 <span id="selectByCondition"/>一般条件查询
> TODO
#### 4.3.2 <span id="selectByPages"/>分页查询
> TODO
#### 4.3.3 <span id="selectHard"/>复杂查询
> TODO

## 5. 更新
### 5.1 <span id="updateByCondition"/>根据条件更新
> TODO
### 5.2 <span id="updateByPrimaryKey"/>根据主键更新
> TODO
### 5.3 <span id="updateByChoose"/>有选择的更新
> TODO
### 5.4 <span id="updateBatch"/>批量更新
> TODO

## 6. 删除
### 6.1 <span id="deleteByCondition"/>根据条件删除
> TODO
### 6.2 <span id="deleteByPrimaryKey"/>根据主键删除
> TODO









