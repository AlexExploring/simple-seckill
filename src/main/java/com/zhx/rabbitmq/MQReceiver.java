package com.zhx.rabbitmq;

import com.zhx.pojo.TSeckillOrder;
import com.zhx.pojo.TUser;
import com.zhx.service.TGoodsService;
import com.zhx.service.TOrderService;
import com.zhx.utils.JsonUtil;
import com.zhx.vo.GoodsVo;
import com.zhx.vo.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息消费者
 */
@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private TGoodsService tGoodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TOrderService tOrderService;

    /**
     * 下单操作
     **/
    @RabbitListener(queues = "seckillQueue")
    public void receive(String message) {
        log.info("接收消息：" + message);

        SeckillMessage seckillMessage = JsonUtil.jsonStrToObject(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        TUser user = seckillMessage.getTUser();
        //判断库存
        GoodsVo goodsVo = tGoodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        //判断是否重复抢购
        TSeckillOrder tSeckillOrder =
                (TSeckillOrder) redisTemplate.opsForValue()
                        .get("order:" + user.getId() + ":" + goodsId);
        if (tSeckillOrder != null) {
            return;
        }
        //下单操作
        tOrderService.secKill(user, goodsVo);
    }
}