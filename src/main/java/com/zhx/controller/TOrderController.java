package com.zhx.controller;

import com.zhx.pojo.TUser;
import com.zhx.service.TOrderService;
import com.zhx.vo.OrderDetailVo;
import com.zhx.vo.RespBean;
import com.zhx.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端控制器
 *
 * @author LiChao
 * @since 2022-03-03
 */
@RestController
@RequestMapping("/order")
public class TOrderController {

    @Autowired
    private TOrderService tOrderService;


    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public RespBean detail(TUser tUser, Long orderId) {
        if (tUser == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo orderDetailVo = tOrderService.detail(orderId);
        return RespBean.success(orderDetailVo);
    }
}
