package com.example.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PACKAGE     : com.example.demo
 * CREATE DATE : 2018-05-07
 * AUTHOR      : shouhan
 * 文件描述     :
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping("test")
    public String test() {
        System.out.println("jenkins启动成功。。。");
        return "jenkins启动成功。。。";
    }

}
