package com.zhx.controller;

import com.zhx.pojo.TUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 商品
 */
@Controller
@RequestMapping("goods")
public class GoodsController {

    @GetMapping(value = "/toList")
    public String toList(Model model,TUser user) {
        model.addAttribute("user",user);
        return "goodsList";
    }
}
