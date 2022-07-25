package com.zhx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhx.exception.GlobalException;
import com.zhx.mapper.TOrderMapper;
import com.zhx.mapper.TSeckillGoodsMapper;
import com.zhx.mapper.TSeckillOrderMapper;
import com.zhx.pojo.TOrder;
import com.zhx.pojo.TSeckillGoods;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.service.TGoodsService;
import com.zhx.service.TOrderService;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.OrderDetailVo;
import com.zhx.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 服务实现类
 */
@Service
public class TOrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder> implements TOrderService {

    @Autowired
    private TSeckillGoodsMapper tSeckillGoodsMapper;

    @Autowired
    private TSeckillOrderMapper tSeckillOrderMapper;

    @Autowired
    private  TOrderMapper tOrderMapper;

    @Autowired
    private TGoodsService tGoodsService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public TOrder secKill1(TUser user, GoodsVo goodsVo) {
        //秒杀商品扣减库存
        TSeckillGoods seckillGoods = tSeckillGoodsMapper.selectOne(new QueryWrapper<TSeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        int seckillGoodsResult = tSeckillGoodsMapper.update(null,new UpdateWrapper<TSeckillGoods>()
                .setSql("stock_count = " + "stock_count - 1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));

        //扣减库存失败
        if (seckillGoodsResult <= 0) {
            return null;
        }

        //生成订单
        TOrder order = new TOrder();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        tOrderMapper.insert(order);

        //生成秒杀订单
        TSeckillOrder tSeckillOrder = new TSeckillOrder();
        tSeckillOrder.setUserId(user.getId());
        tSeckillOrder.setOrderId(order.getId());
        tSeckillOrder.setGoodsId(goodsVo.getId());
        tSeckillOrderMapper.insert(tSeckillOrder);

        return order;
    }

    public TOrder secKill(TUser user, GoodsVo goodsVo) {
        //秒杀商品扣减库存
        TSeckillGoods seckillGoods = tSeckillGoodsMapper.selectOne(new QueryWrapper<TSeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        int seckillGoodsResult = tSeckillGoodsMapper.update(null,new UpdateWrapper<TSeckillGoods>()
                .setSql("stock_count = " + "stock_count - 1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));

        //扣减库存失败
        if (seckillGoodsResult <= 0) {
            return null;
        }

        //生成订单
        TOrder order = new TOrder();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        tOrderMapper.insert(order);

        //生成秒杀订单
        TSeckillOrder tSeckillOrder = new TSeckillOrder();
        tSeckillOrder.setUserId(user.getId());
        tSeckillOrder.setOrderId(order.getId());
        tSeckillOrder.setGoodsId(goodsVo.getId());
        tSeckillOrderMapper.insert(tSeckillOrder);

        redisTemplate.opsForValue().set("order:" + user.getId() + ":" + goodsVo.getId(), tSeckillOrder);

        return order;
    }

    public OrderDetailVo detail(Long orderId) {
        if (orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        TOrder tOrder = tOrderMapper.selectById(orderId);
        GoodsVo goodsVoByGoodsId = tGoodsService.findGoodsVoByGoodsId(tOrder.getGoodsId());
        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setTOrder(tOrder);
        orderDetailVo.setGoodsVo(goodsVoByGoodsId);
        return orderDetailVo;
    }
}