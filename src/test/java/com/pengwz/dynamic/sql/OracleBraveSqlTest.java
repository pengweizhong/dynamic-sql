package com.pengwz.dynamic.sql;


import com.pengwz.dynamic.entity.oracle.TBCopyEntity;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OracleBraveSqlTest {

    @Test
    public void testInsert() {
        List<TBCopyEntity> tbCopyEntityList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            TBCopyEntity entity = new TBCopyEntity();
            entity.setTbColumn0("TbColumn0000_" + i);
            entity.setTbColumn1("TbColumn1111_" + i);
            entity.setTbColumn2("TbColumn2222_" + i);
            tbCopyEntityList.add(entity);
        }
        Integer integer = BraveSql.build(TBCopyEntity.class).batchInsert(tbCopyEntityList);
        System.out.println(integer);
    }

    @Test
    public void testSelect() {
        List<TBCopyEntity> select = BraveSql.build(TBCopyEntity.class).select();
        System.out.println(select.size());
        System.out.println(select.subList(0, 1));
    }


}