package com.zhx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhx.mapper.TOrderMapper;
import com.zhx.pojo.TOrder;
import com.zhx.pojo.TSeckillGoods;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.service.TOrderService;
import com.zhx.service.TSeckillGoodsService;
import com.zhx.service.TSeckillOrderService;
import com.zhx.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 服务实现类
 */
@Service
public class TOrderServiceImpl extends ServiceImpl<TOrderMapper, TOrder> implements TOrderService {

    @Autowired
    private TSeckillGoodsService tSeckillGoodsService;

    @Autowired
    private TSeckillOrderService tSeckillOrderService;

    @Autowired
    private  TOrderMapper tOrderMapper;

    public TOrder secKill(TUser user, GoodsVo goodsVo) {
        //秒杀商品扣减库存
        TSeckillGoods seckillGoods = tSeckillGoodsService.getOne(new QueryWrapper<TSeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        boolean seckillGoodsResult = tSeckillGoodsService.update(new UpdateWrapper<TSeckillGoods>()
                .setSql("stock_count = " + "stock_count - 1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0)
        );

        //扣减库存失败
        if (!seckillGoodsResult) {
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
        tSeckillOrderService.save(tSeckillOrder);

        return order;
    }
}