package com.dorm.server.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 封装 RedisTemplate 的常用操作，业务层统一通过此工具类操作 Redis
 *
 * @author dorm-server
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 存储键值对并设置过期时间
     *
     * @param key     缓存 Key
     * @param value   缓存值
     * @param timeout 过期时长
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取缓存值
     *
     * @param key 缓存 Key
     * @return 缓存值，不存在则返回 null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定 Key
     *
     * @param key 缓存 Key
     * @return true=删除成功，false=Key不存在
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断 Key 是否存在
     *
     * @param key 缓存 Key
     * @return true=存在，false=不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置 Key 的过期时间
     *
     * @param key     缓存 Key
     * @param timeout 过期时长
     * @param unit    时间单位
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 存储键值对（不设置过期时间，永不过期）
     *
     * @param key   缓存 Key
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
}
