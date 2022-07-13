package com.zhx.service.impl;

import com.zhx.mapper.TGoodsMapper;
import com.zhx.service.TGoodsService;
import com.zhx.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TGoodsServiceImpl implements TGoodsService {

    @Autowired
    private TGoodsMapper tGoodsMapper;

    @Override
    public List<GoodsVo> findGoodsVo() {
        return tGoodsMapper.findGoodsVo();
    }

    @Override
    public GoodsVo findGoodsVoByGoodsId(Long goodsId) {
        return tGoodsMapper.findGoodsVoByGoodsId(goodsId);
    }
}