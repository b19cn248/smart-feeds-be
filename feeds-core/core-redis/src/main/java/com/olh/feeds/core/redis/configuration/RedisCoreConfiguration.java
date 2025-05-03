package com.olh.feeds.core.redis.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
@ComponentScan(basePackages = {"com.olh.feeds.core.redis"})
public class RedisCoreConfiguration {
  @Value("${spring.data.redis.host}")
  private String redisHostName;
  @Value("${spring.data.redis.port}")
  private int redisPort;

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    RedisStandaloneConfiguration standaloneConfiguration = new RedisStandaloneConfiguration(redisHostName, redisPort);

    return new JedisConnectionFactory(standaloneConfiguration);
  }

  @Bean
  public <K, V> RedisTemplate<K, V> redisTemplate() {
    RedisTemplate<K, V> template = new RedisTemplate<>();

    template.setConnectionFactory(jedisConnectionFactory());
    template.setKeySerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    return template;
  }

  @Bean
  public <K, F, V> HashOperations<K, F, V> redisHashOperations(
        RedisTemplate<K, V> redisTemplate
  ) {
    return redisTemplate.opsForHash();
  }
}
