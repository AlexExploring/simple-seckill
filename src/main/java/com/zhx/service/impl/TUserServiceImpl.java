package com.zhx.service.impl;

import com.zhx.exception.GlobalException;
import com.zhx.mapper.TUserMapper;
import com.zhx.pojo.TUser;
import com.zhx.service.TUserService;
import com.zhx.utils.CookieUtil;
import com.zhx.utils.MD5Util;
import com.zhx.utils.UUIDUtil;
import com.zhx.vo.LoginVo;
import com.zhx.vo.RespBean;
import com.zhx.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class TUserServiceImpl implements TUserService {

    @Autowired
    private TUserMapper tUserMapper;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public RespBean doLongin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        //用户是否存在
        TUser user = tUserMapper.selectById(mobile);
        if (user == null) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //判断密码是否正确
        if (!MD5Util.formPassToDBPass(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(RespBeanEnum.LOGIN_ERROR);
        }

        //生成Cookie
        String userTicket = UUIDUtil.uuid();

        //使用redis存放用户信息
        redisTemplate.opsForValue().set("user:" + userTicket, user);

        CookieUtil.setCookie(request, response, "userTicket", userTicket);
        return RespBean.success(userTicket);
    }

    @Override
    public TUser getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        TUser user = (TUser) redisTemplate.opsForValue().get("user:" + userTicket);
        if (user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    @Override
    public RespBean updatePassword(String userTicket, String password, HttpServletRequest request, HttpServletResponse response) {
        return null;
    }
}
