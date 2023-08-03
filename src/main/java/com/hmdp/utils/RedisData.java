package com.hmdp.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RedisData {
    //逻辑过期时间
    private LocalDateTime expireTime;
    //存储在redis中的数据
    private Object data;
}
