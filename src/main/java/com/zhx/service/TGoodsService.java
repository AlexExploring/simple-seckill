package com.zhx.service;

import com.zhx.vo.GoodsVo;

import java.util.List;

public interface TGoodsService {


    /**
     * 获取商品列表
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 获取商品详情
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
