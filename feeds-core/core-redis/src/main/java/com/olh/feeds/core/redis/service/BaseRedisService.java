package com.olh.feeds.core.redis.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface BaseRedisService<K, V> {
  void set(K key, V value);

  void setTimeToLive(K key, long timeToLive, TimeUnit timeUnit);

  V get(K key);

  V get(K key, Supplier<V> valueSupplier);

  void delete(K key);

  Long increment(K key, long value);
}
