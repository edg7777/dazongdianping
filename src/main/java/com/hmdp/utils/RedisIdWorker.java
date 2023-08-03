package com.hmdp.utils;

import lombok.AllArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
public class RedisIdWorker {
    //开始的时间戳
    private static final long BEGIN_TIMESTAMP=1640995200L;
    //序列号的位数
    private static final long COUNT_BITS=32;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    public long nextId(String keyPrefix){
        //1.生成时间戳
        LocalDateTime currentTime = LocalDateTime.now();
        long currentSeconds = currentTime.toEpochSecond(ZoneOffset.UTC);
        long timestamp=currentSeconds-BEGIN_TIMESTAMP;

        //2.生成序列号
        //2.1获取当前日期
        String date = currentTime.format(DateTimeFormatter.ofPattern("yyy:MM:dd"));
        //2.2将日期拼接到key中做自增
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        //3.拼接返回
        return timestamp<<COUNT_BITS | count;
    }

}
