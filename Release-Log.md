# release/2.1.8 发行说明（存档）

- 新增.selectMax(Fn, Fn)函数
- 新增.selectMin(Fn, Fn)函数
- 新增.insertOrUpdateActive(T)函数
- 优化了所有涉及异常捕捉及资源释放的地方
- 重载orFindInSet()以支持自定义分隔符查询
- 此版本为最终版本，不再更新维护

# release/2.1.7 发行说明

- 新增limit()函数
- 新增find_in_set()函数
- 修复查询数据时偶尔日期转换失败的问题
- 修复ColumnJson注解value值必传的问题
- 修复ContextApplication Api调用异常的问题
- 修复InsertOrUpdate带主键插入时的`Illegal operation on empty result set.`问题
- 创建一个允许嵌套括号的查询条件,并将旧的方法过时
- 优化了一些方法

# release/2.1.6 发行说明

- starter，，没有默认数据源报错的问题
- ID有值插入时 自增值仍然覆盖的问题

# release/2.1.5.hotfix 发行说明

- 修复了Json体更新的问题

# release/2.1.5 发行说明

- 修复了连接泄露
- 优化了日志警告
- 修复了Json空对象存入数据库变成字符串null的问题

# release/2.1.4 发行说明

- 修复了 `isNull`,`isNotNull`的查询问题
- 优化了log日志输出

# release/2.1.3 发行说明

- 解决了SQL注入的问题（紧急！）
- 修复了查询时枚举集合入参不能识别为字符串的题
- 增强了类型转换适配，基本类型转为引用类型的问题
- 加入了SQL读取结果集自定义适配转换器（ConverterAdapter）
- 加入了SQL拦截器（SQLInterceptor）
- 支持实体类忽略字段，被忽略的字段不参与数据库交互
- 增强了聚合函数映射类型
- 修复多数据源动态代理后无法识别的问题
- 修复了springboot默认数据源无法和动态SQL默认数据源兼容的问题
- 修复了Json对象无法作为参数查询的问题
