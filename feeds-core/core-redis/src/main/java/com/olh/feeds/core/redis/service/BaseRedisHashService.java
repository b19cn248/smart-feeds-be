package com.olh.feeds.core.redis.service;

import java.util.Map;

public interface BaseRedisHashService<K, F, V> {
  void hashSet(K key, F field, V value);

  V hashGet(K key, F field);

  Map<F, V> hashGetAllEntries(K key);

  void hashDelete(K key, F field);
}
