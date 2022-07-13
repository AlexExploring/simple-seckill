package com.zhx.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用于测试项目是否搭建成功
 */
@Controller
public class HelloController {

    @GetMapping(value = "/hello")
    public String hello(Model model) {
        model.addAttribute("name", "seckill");
        return "hello";
    }
}

