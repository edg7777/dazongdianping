package com.hmdp.lisenter;

import com.hmdp.entity.VoucherOrder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author fzj
 * @date 2023-08-03 17:48
 */
@Deprecated
@Component
public class RabbitMQListener {
    @RabbitListener(queues = "voucherOrder.queue")
    public void listenVoucherOrder(VoucherOrder voucherOrder){
        System.out.println("消费者接收到了订单消息:"+voucherOrder.toString());
    }
}
