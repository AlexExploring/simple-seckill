package com.zhx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhx.pojo.TGoods;
import com.zhx.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TGoodsMapper extends BaseMapper<TGoods> {

    public List<GoodsVo> findGoodsVo();

    public GoodsVo findGoodsVoByGoodsId(Long goodsId);
}