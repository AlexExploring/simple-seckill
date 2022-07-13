package com.zhx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhx.pojo.TSeckillOrder;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Administrator
* @description 针对表【t_seckill_order(秒杀订单表)】的数据库操作Mapper
* @createDate 2022-07-07 16:23:22
* @Entity com.zhx.pojo.TSeckillOrder
*/
@Mapper
public interface TSeckillOrderMapper extends BaseMapper<TSeckillOrder> {

}




