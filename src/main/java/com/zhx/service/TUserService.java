package com.zhx.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhx.pojo.TUser;
import com.zhx.vo.LoginVo;
import com.zhx.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TUserService extends IService<TUser> {

    /**
     * 登录方法
     **/
    RespBean doLongin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    /**
     * 根据cookie获取用户
     **/
    TUser getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);


    /**
     * 更新密码
     **/
    RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response);
}
