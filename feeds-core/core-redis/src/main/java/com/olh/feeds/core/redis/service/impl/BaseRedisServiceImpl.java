package com.olh.feeds.core.redis.service.impl;

import com.olh.feeds.core.redis.service.BaseRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Slf4j
@Service
@RequiredArgsConstructor
public class BaseRedisServiceImpl<K, V> implements BaseRedisService<K, V> {
  private final RedisTemplate<K, V> redisTemplate;

  @Override
  public void set(K key, V value) {
    redisTemplate.opsForValue().set(key, value);
  }

  @Override
  public void setTimeToLive(K key, long timeToLive, TimeUnit timeUnit) {
    redisTemplate.expire(key, timeToLive, timeUnit);
  }

  @Override
  public V get(K key) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public V get(K key, Supplier<V> valueSupplier) {
    V value = redisTemplate.opsForValue().get(key);

    if (value == null) {
      value = valueSupplier.get();
      this.set(key, value);
    }

    return value;
  }

  @Override
  public void delete(K key) {
    redisTemplate.delete(key);
  }

  @Override
  public Long increment(K key, long value) {
    return redisTemplate.opsForValue().increment(key, value);
  }
}
