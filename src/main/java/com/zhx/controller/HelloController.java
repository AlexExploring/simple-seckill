package com.zhx.controller;


import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用于测试项目是否
 */
@Controller
@Api(value = "demo", tags = "demo测试类")
public class HelloController {

    @GetMapping(value = "/hello")
    public String hello(Model model) {
        model.addAttribute("name", "seckill");
        return "hello";
    }
}

