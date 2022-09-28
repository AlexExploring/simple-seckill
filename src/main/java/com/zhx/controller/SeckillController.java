package com.zhx.controller;

import com.wf.captcha.ArithmeticCaptcha;
import com.zhx.annotation.AccessLimit;
import com.zhx.exception.GlobalException;
import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.rabbitmq.MQSender;
import com.zhx.service.TGoodsService;
import com.zhx.service.TOrderService;
import com.zhx.service.TSeckillOrderService;
import com.zhx.utils.JsonUtil;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.RespBean;
import com.zhx.vo.RespBeanEnum;
import com.zhx.vo.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    private TOrderService tOrderService;

    @Autowired
    private TGoodsService tGoodsService;

    @Autowired
    private TSeckillOrderService tSeckillOrderService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private MQSender mqSender;

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    @Autowired
    private RedisScript stockScript;

    @GetMapping(value = "/captcha")
    public void verifyCode(TUser user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //生成验证码
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }

    /**
     * 对path接口进行简单限流
     */
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @GetMapping(value = "/path")
    @ResponseBody
    public RespBean getPath(TUser user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        //限制访问次数，5秒内访问5次
        String uri = request.getRequestURI();
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null) {
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) {
            valueOperations.increment(uri + ":" + user.getId());
        } else {
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
        }

        boolean check = tOrderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = tOrderService.createPath(user, goodsId);

        return RespBean.success(str);
    }

    /**
     * 对path接口进行简单限流
     */
    @GetMapping(value = "/path1")
    @ResponseBody
    public RespBean getPath1(TUser user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();
        //限制访问次数，5秒内访问5次
        String uri = request.getRequestURI();
        Integer count = (Integer) valueOperations.get(uri + ":" + user.getId());
        if (count == null) {
            valueOperations.set(uri + ":" + user.getId(), 1, 5, TimeUnit.SECONDS);
        } else if (count < 5) {
            valueOperations.increment(uri + ":" + user.getId());
        } else {
            return RespBean.error(RespBeanEnum.ACCESS_LIMIT_REACHED);
        }

        boolean check = tOrderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }

        String str = tOrderService.createPath(user, goodsId);

        return RespBean.success(str);
    }

    @GetMapping(value = "/path2")
    @ResponseBody
    public RespBean getPath2(TUser user, Long goodsId, String captcha, HttpServletRequest request) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        boolean check = tOrderService.checkCaptcha(user, goodsId, captcha);
        if (!check) {
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = tOrderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill(@PathVariable String path,TUser user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        //校验地址是否合法
        boolean check = tOrderService.checkPath(user, goodsId, path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        //判断是否重复抢购,在高并发的情况下，这里不能拦截所有重复的订单
        TSeckillOrder seckillOrder = (TSeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //内存标记减少redis访问次数
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        if (redisTemplate.opsForSet().add("preSet",user.getId()+":"+goodsId) > 0) {
            //redis预减库存
            Long stock = (Long) redisTemplate.execute(stockScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
            if (stock < 0) {
                EmptyStockMap.put(goodsId,true);
                return RespBean.error(RespBeanEnum.EMPTY_STOCK);
            }
        }else {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        SeckillMessage message = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.objectToJsonStr(message));

        return RespBean.success(0);
    }

    @RequestMapping(value = "/doSeckill1", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSecKill1(TUser user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        //判断是否重复抢购,在高并发的情况下，这里不能拦截所有重复的订单
        TSeckillOrder seckillOrder = (TSeckillOrder) valueOperations.get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        //内存标记减少redis访问次数
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        if (redisTemplate.opsForSet().add("preSet",user.getId()+":"+goodsId) > 0) {
            //redis预减库存
            Long stock = (Long) redisTemplate.execute(stockScript, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
            if (stock < 0) {
                EmptyStockMap.put(goodsId,true);
                valueOperations.increment("seckillGoods:" + goodsId);
                return RespBean.error(RespBeanEnum.EMPTY_STOCK);
            }
        }else {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        SeckillMessage message = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.objectToJsonStr(message));

        return RespBean.success(0);
    }

    @GetMapping("/getResult")
    @ResponseBody
    public RespBean getResult(TUser tUser, Long goodsId) {
        if (tUser == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = tSeckillOrderService.getResult(tUser, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 系统初始化完毕后，把商品库存数量加载到Redis
     **/
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = tGoodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }
}