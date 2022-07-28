package com.zhx.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhx.mapper.TSeckillOrderMapper;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TSeckillOrderServiceImpl extends ServiceImpl<TSeckillOrderMapper, TSeckillOrder> implements com.zhx.service.TSeckillOrderService {

    @Autowired
    private TSeckillOrderMapper tSeckillOrderMapper;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public Long getResult(TUser tUser, Long goodsId) {

        TSeckillOrder tSeckillOrder = tSeckillOrderMapper.selectOne(new QueryWrapper<TSeckillOrder>().eq("user_id", tUser.getId()).eq("goods_id", goodsId));
        if (null != tSeckillOrder) {
            return tSeckillOrder.getOrderId();
        } else if (redisTemplate.hasKey("isStockEmpty:" + goodsId)) {
            return -1L;
        } else {
            return 0L;
        }
    }
}
