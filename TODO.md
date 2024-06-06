# 20240417 计划在version.2.1.8 更新的内容

1、新增根据自定义方法匹配枚举，可能长这样： @Column(value = "xxxEnum", enumMappingMethod = XxxEnum::mathValue)  
2、优化申请数据库连接和销毁  
3、优化整体结构  
4、优化实体类托管池，用面向对象新增XXXX，销毁（置为已过时） ContextApplication  
5、加入多查询的支持  
6、新增动态的新增或更新方法  
7、~~(已完成)新增max/min时多字段返回，比如实现这个SQL：~~  
    SELECT  x.trusteeship_api ,max(x.sync_time) FROM trumgu_analysis_db.t_ta_sync_record x  
    where x.team_id =1 and x.trusteeship_code ='CMS' group by x.trusteeship_api  
    #################################################  
    #|trusteeship_api          |max(x.sync_time)   |  
    -+-------------------------+-------------------+  
    1|https://cms.com/aaa/nn/分红|2024-03-19 14:37:49|  
    2|https://cms.com/aaa/nn/净值|2024-01-13 14:37:49|  
