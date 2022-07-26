package com.zhx.controller;

import com.zhx.mapper.TGoodsMapper;
import com.zhx.pojo.TOrder;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.rabbitmq.MQSender;
import com.zhx.service.TGoodsService;
import com.zhx.service.TOrderService;
import com.zhx.utils.JsonUtil;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.RespBean;
import com.zhx.vo.RespBeanEnum;
import com.zhx.vo.SeckillMessage;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    private TGoodsMapper tGoodsMapper;

    @Autowired
    private TOrderService tOrderService;

    @Autowired
    private TGoodsService tGoodsService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MQSender mqSender;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    /**
     * 借助redis判断是否重复抢购
     *
     * 秒杀静态化
     */
    @RequestMapping(value = "/doSeckill1", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill1(Model model, TUser user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        GoodsVo goods = tGoodsMapper.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goods.getStockCount() < 1) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //判断是否重复抢购
        TSeckillOrder seckillOrder = (TSeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //执行秒杀操作
        TOrder order = tOrderService.secKill(user, goods);

        return RespBean.success(order);
    }

    /**
     * 借助redis判断是否重复抢购
     */
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(TUser user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        //判断是否重复抢购
        TSeckillOrder seckillOrder = (TSeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //内存标记减少redis访问次数
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        //redis预减库存
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if (stock < 0) {
            EmptyStockMap.put(goodsId,true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        SeckillMessage message = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.objectToJsonStr(message));

        return RespBean.success(0);
    }

    /**
     * 系统初始化完毕后，把商品库存数量加载到Redis
     **/
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