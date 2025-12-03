package com.guenbon.jochuckhub.service.redis;

public interface RedisManager {
    String get(String key);

    void set(String key, String value, long ttlSeconds);

    void set(String key, String value);

    void delete(String key);
}
