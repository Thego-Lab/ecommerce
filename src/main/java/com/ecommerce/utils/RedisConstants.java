package com.ecommerce.utils;

/**
 * Redis Key 前缀常量
 */
public class RedisConstants {
    /** 登录用户 key */
    public static final String LOGIN_USER_KEY = "login:user:";
    /** 登录用户 TTL（分钟） */
    public static final Long LOGIN_USER_TTL = 30L;

    /** 商品缓存 key */
    public static final String CACHE_PRODUCT_KEY = "cache:product:";
    /** 商品列表缓存 key */
    public static final String CACHE_PRODUCT_PAGE_KEY = "cache:product:page:";
    /** 商品缓存 TTL（分钟） */
    public static final Long CACHE_PRODUCT_TTL = 30L;
    /** 商品列表缓存 TTL（分钟） */
    public static final Long CACHE_PRODUCT_PAGE_TTL = 10L;
    /** 空值缓存 TTL（分钟），防穿透 */
    public static final Long CACHE_NULL_TTL = 2L;

    /** 分类树缓存 key */
    public static final String CACHE_CATEGORY_TREE_KEY = "cache:category:tree";
    /** 分类树缓存 TTL（分钟） */
    public static final Long CACHE_CATEGORY_TTL = 60L;
}
