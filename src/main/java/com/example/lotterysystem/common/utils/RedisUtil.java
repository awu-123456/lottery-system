package com.example.lotterysystem.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    public Boolean set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("RedisUtil error, set({}, {})", key, value,e);
            return false;
        }
    }

    public Boolean set(String key, String value,Long time) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.error("RedisUtil error, set({}, {}, {})", key, value,time,e);
            return false;
        }
    }

    public String get(String key) {
        try {
            return StringUtils.hasText(key) ?  stringRedisTemplate.opsForValue().get(key) : null;
        } catch (Exception e) {
            logger.error("RedisUtil error, get({})", key, e);
            return null;
        }
    }

    public Boolean del(String... key) {
        try {
            if(key != null && key.length > 0){
                if(key.length == 1){
                    stringRedisTemplate.delete(key[0]);
                } else {
                    stringRedisTemplate.delete(
                            (Collection<String>) CollectionUtils.arrayToList(key)
                    );
                }
            }
            return true;
        } catch (Exception e) {
         logger.error("RedisUtil error, del({})", key, e);
         return false;
        }
    }

    public Boolean hasKey(String key) {
        try {
            return StringUtils.hasText(key) ?  stringRedisTemplate.hasKey(key) : false;
        } catch (Exception e) {
            logger.error("RedisUtil error, hasKey({})", key, e);
            return false;
        }
    }
}