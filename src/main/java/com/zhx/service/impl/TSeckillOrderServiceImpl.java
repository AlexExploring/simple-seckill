package com.zhx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhx.mapper.TSeckillOrderMapper;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import org.springframework.stereotype.Service;

@Service
public class TSeckillOrderServiceImpl extends ServiceImpl<TSeckillOrderMapper, TSeckillOrder> implements com.zhx.service.TSeckillOrderService {

    @Override
    public Long getResult(TUser tUser, Long goodsId) {
        return null;
    }
}
