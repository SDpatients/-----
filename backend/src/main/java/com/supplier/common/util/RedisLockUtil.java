package com.supplier.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式锁工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 尝试获取锁
     *
     * @param key       锁标识
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String key, long timeoutMs) {
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent("lock:" + key, "1", timeoutMs, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放锁
     *
     * @param key 锁标识
     */
    public void unlock(String key) {
        redisTemplate.delete("lock:" + key);
    }

    /**
     * 检查是否已锁定
     *
     * @param key 锁标识
     * @return 是否已锁定
     */
    public boolean isLocked(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("lock:" + key));
    }
}