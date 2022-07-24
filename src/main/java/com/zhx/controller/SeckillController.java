package com.zhx.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhx.mapper.TGoodsMapper;
import com.zhx.mapper.TSeckillOrderMapper;
import com.zhx.pojo.TOrder;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.service.TOrderService;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController{

    @Autowired
    private TGoodsMapper tGoodsMapper;

    @Autowired
    private TSeckillOrderMapper tSeckillOrderMapper;

    @Autowired
    private TOrderService tOrderService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private Map<Long,Boolean> EmptyStockMap = new HashMap<>();

    @RequestMapping(value = "/doSeckill1", method = RequestMethod.POST)
    public String doSecKill1(Model model, TUser user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user",user);

        GoodsVo goods = tGoodsMapper.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }

        //判断是否重复抢购
        TSeckillOrder secKillOrder = tSeckillOrderMapper.selectOne(new QueryWrapper<TSeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsId));

        if (secKillOrder != null) {
            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }

        //执行秒杀操作
        TOrder order = tOrderService.secKill1(user, goods);

        model.addAttribute("order",order);
        model.addAttribute("goods",goods);

        return "orderDetail";
    }

    /**
     * 借助redis判断是否重复抢购
     */
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    public String doSecKill(Model model, TUser user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user",user);

        GoodsVo goods = tGoodsMapper.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }

        //判断是否重复抢购
        TSeckillOrder seckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);

        if (seckillOrder != null) {
            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }

        //执行秒杀操作
        TOrder order = tOrderService.secKill(user, goods);

        model.addAttribute("order",order);
        model.addAttribute("goods",goods);

        return "orderDetail";
    }
}