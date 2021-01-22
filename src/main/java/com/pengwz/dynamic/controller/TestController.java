package com.pengwz.dynamic.controller;

import com.pengwz.dynamic.exception.BraveException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("api/test/contro")
    @Transactional(rollbackFor=Exception.class)
    public String testContro() {
        throw new BraveException("---------------------------");
    }

}
