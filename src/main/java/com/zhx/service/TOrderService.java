package com.zhx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhx.pojo.TOrder;
import com.zhx.pojo.TUser;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.OrderDetailVo;

/**
 * 服务类
 *
 * @author LiChao
 * @since 2022-03-03
 */
public interface TOrderService extends IService<TOrder> {

    /**
     * 秒杀
     *
     * @param user    用户对象
     * @param goodsVo 商品对象
     * @return com.example.seckilldemo.entity.TOrder
     * @author LC
     * @operation add
     * @date 1:44 下午 2022/3/4
     **/
    TOrder secKill1(TUser user, GoodsVo goodsVo);

    TOrder secKill(TUser user, GoodsVo goodsVo);

    OrderDetailVo detail(Long orderId);

    String createPath(TUser tuser, Long goodsId);

    boolean checkCaptcha(TUser tuser, Long goodsId, String captcha);

    boolean checkPath(TUser user, Long goodsId, String path);
}
