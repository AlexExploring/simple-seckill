package com.zhx.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhx.mapper.TGoodsMapper;
import com.zhx.mapper.TSeckillOrderMapper;
import com.zhx.pojo.TOrder;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.rabbitmq.MQSender;
import com.zhx.service.TGoodsService;
import com.zhx.service.TOrderService;
import com.zhx.utils.JsonUtil;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.RespBeanEnum;
import com.zhx.vo.SeckillMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    private TGoodsMapper tGoodsMapper;

    @Autowired
    private TSeckillOrderMapper tSeckillOrderMapper;

    @Autowired
    private TOrderService tOrderService;

    @Autowired
    private TGoodsService tGoodsService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MQSender mqSender;

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
    @RequestMapping(value = "/doSeckill2", method = RequestMethod.POST)
    public String doSecKill2(Model model, TUser user, Long goodsId) {
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

    /**
     *
     */
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    public String doSecKill(Model model, TUser user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user",user);

        //判断是否重复抢购
        TSeckillOrder seckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            model.addAttribute("errmsg",RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }

        //内存标记，减少redis的访问
        if (EmptyStockMap.get(goodsId)) {
            model.addAttribute("errmsg",RespBeanEnum.EMPTY_STOCK);
            return "secKillFail";
        }
        //redis预减库存
        Long decrement = redisTemplate.opsForValue().decrement("seckillGoods:" + goodsId);
        if (decrement < 0) {
            redisTemplate.opsForValue().increment("seckillGoods:" + goodsId);
            model.addAttribute("errmsg",RespBeanEnum.EMPTY_STOCK);
            return "secKillFail";
        }

        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.objectToJsonStr(seckillMessage));

        return "orderDetail";
    }

    /**
     * 系统初始化时，将商品库存数量加载到redis中
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = tGoodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}