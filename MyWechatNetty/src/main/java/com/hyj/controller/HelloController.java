package com.hyj.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Chosen1
 * @date: 2020/04/2 18:13
 */

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){

        return "hello wechat";
    }
}
