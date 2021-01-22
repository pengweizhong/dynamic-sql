package com.pengwz.dynamic.sql;

import com.pengwz.dynamic.entity.DynamicEntity;
import com.pengwz.dynamic.utils.ConverterUtils;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BraveSqlTest {

    @Test
    public void test1() {

        List<DynamicEntity> select = BraveSql.build(DynamicEntity.class).select();
        System.out.println(select);

    }


}





















