package com.hmdp.utils;

public class RedisConstants {
    //登录验证码以及存在时间
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 60L;
    //登录token以及存在时间
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    //商店查询缓存以及存在时间
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    //商店类型缓存
    public static final String REDIS_KEY = "cache:shopType";
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;


    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    //用户点赞
    public static final String BLOG_LIKED_KEY = "blog:liked:";

    //用户的推送
    public static final String FEED_KEY = "feed:";

    //根据距离查询商铺
    public static final String SHOP_GEO_KEY = "shop:geo:";
    //统计用户签到
    public static final String SIGN_KEY = "sign:";

    //记录用户登录时的token
    public static final String USER_LOGIN_TOKEN="user:token:";
}
