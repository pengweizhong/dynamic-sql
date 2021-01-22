package com.pengwz.dynamic.controller;

import com.pengwz.dynamic.entity.DynamicEntity;
import com.pengwz.dynamic.exception.BraveException;
import com.pengwz.dynamic.sql.BraveSql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class TestController {

    @GetMapping("api/test/contro")
    @Transactional(rollbackFor=Exception.class)
    public String testContro() {
        DynamicEntity entity = BraveSql.build(DynamicEntity.class).selectByPrimaryKey(381);
        entity.setMoney(BigDecimal.valueOf(100));
        BraveSql.build(DynamicEntity.class).updateByPrimaryKey(entity);
        DynamicEntity entity2 = BraveSql.build(DynamicEntity.class).selectByPrimaryKey(385);
        entity2.setMoney(BigDecimal.valueOf(100));
        BraveSql.build(DynamicEntity.class).updateByPrimaryKey(entity2);
        throw new BraveException("---------------------------");
    }

}
