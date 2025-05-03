package com.olh.feeds.core.redis.service.impl;

import com.olh.feeds.core.redis.service.BaseRedisHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class BaseRedisHashServiceImpl<K, F, V> implements BaseRedisHashService<K, F, V> {
    private final HashOperations<K, F, V> redisHashOperations;

    @Override
    public void hashSet(K key, F field, V value) {
        redisHashOperations.put(key, field, value);
    }

    @Override
    public V hashGet(K key, F field) {
        return redisHashOperations.get(key, field);
    }

    @Override
    public Map<F, V> hashGetAllEntries(K key) {
        return redisHashOperations.entries(key);
    }

    @Override
    public void hashDelete(K key, F field) {
        redisHashOperations.delete(key, field);
    }
}
