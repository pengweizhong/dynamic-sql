# 20240417 计划在version.2.1.8 更新的内容

1、新增根据自定义方法匹配枚举，可能长这样： @Column(value = "xxxEnum", enumMappingMethod = XxxEnum::mathValue)  
2、优化申请数据库连接和销毁  
3、~~(已完成)优化整体结构~~  
4、优化实体类托管池，用面向对象新增XXXX，销毁（置为已过时） ContextApplication  
5、加入多查询的支持  
6、~~新增动态的新增或更新方法~~  
7、~~新增max/min时多字段返回组装成Map结果集~~
