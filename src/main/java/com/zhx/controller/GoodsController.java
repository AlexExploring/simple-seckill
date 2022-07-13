package com.zhx.controller;

import com.zhx.pojo.TUser;
import com.zhx.service.TGoodsService;
import com.zhx.service.TUserService;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

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

    @RequestMapping(value = "/toList")
    public String toList(Model model, TUser user, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("user",user);
        model.addAttribute("goodsList", tGoodsService.findGoodsVo());
        return "goodsList";
    }

    @RequestMapping(value = "/toDetail/{goodsId}")
    public String toDetail2(Model model, TUser user, @PathVariable Long goodsId) {
        model.addAttribute("user",user);

        GoodsVo goodsVo = tGoodsService.findGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goodsVo);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();

        //秒杀状态
        int secKillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            //秒杀还未开始0
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            //秒杀已经结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀进行中
            secKillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("secKillStatus",secKillStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        return "goodsDetail";
    }
}
