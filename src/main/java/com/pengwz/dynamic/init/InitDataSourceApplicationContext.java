package com.pengwz.dynamic.init;

import com.pengwz.dynamic.config.DataSourceConfig;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.utils.ClassUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 程序启动时，初始化数据库连接信息
 *
 */
public class InitDataSourceApplicationContext {

    private InitDataSourceApplicationContext(){}

    /**
     * 目前还未实现如何完整的获取子类信息。。。
     */
    public static void init(){

    }

    /**
     * 校验数据库
     */
    private void checkDataSource(){
        List<Class> dataSourceClasses = ClassUtils.getAllClassByFather(DataSourceConfig.class);
        if(CollectionUtils.isEmpty(dataSourceClasses)){
            throw new BraveException("未检测到数据源");
        }


    }
}
