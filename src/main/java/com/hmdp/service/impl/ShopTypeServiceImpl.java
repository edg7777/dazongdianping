package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;

import static com.hmdp.utils.RedisConstants.REDIS_KEY;


@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public List<ShopType> queryByRedis() {
        String typeListJson = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        if(typeListJson!=null){
            List<ShopType> shopTypeList = JSONUtil.toList(typeListJson, ShopType.class);
            return shopTypeList;
        }
        // 缓存中没有数据，从数据库查询
        List<ShopType> typeList = query().orderByAsc("sort").list();
        // 将查询结果存入Redis缓存中
        stringRedisTemplate.opsForValue().set(REDIS_KEY, JSONUtil.toJsonStr(typeList));
        return typeList;
    }
}
