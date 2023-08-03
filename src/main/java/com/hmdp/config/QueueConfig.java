package com.hmdp.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fzj
 * @date 2023-08-03 18:19
 */

@Configuration
public class QueueConfig {
    @Bean
    public Queue voucherOrderQueue(){
        return new Queue("voucherOrder.queue");
    }
}
