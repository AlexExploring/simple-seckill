package com.zhx.controller;

import com.zhx.pojo.TUser;
import com.zhx.service.TGoodsService;
import com.zhx.service.TUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private TGoodsService tGoodsService;

    @Autowired
    private TUserService tUserService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping(value = "/toList")
    public String toList(Model model,TUser user) {
        model.addAttribute("user",user);
        return "goodsList";
    }
}
