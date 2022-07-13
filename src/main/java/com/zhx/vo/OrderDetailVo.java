package com.zhx.vo;

import com.zhx.pojo.TOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailVo {

    private TOrder tOrder;

    private GoodsVo goodsVo;
}
