package com.zhx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;

/**
 * 秒杀订单表 服务类
 */
public interface TSeckillOrderService extends IService<TSeckillOrder> {

    /**
     * 获取秒杀结果
     *
     * @param tUser
     * @param goodsId
     * @return orderId 成功 ；-1 秒杀失败 ；0 排队中
     * @author LiChao
     * @operation add
     * @date 7:07 下午 2022/3/8
     **/
    Long getResult(TUser tUser, Long goodsId);
}
