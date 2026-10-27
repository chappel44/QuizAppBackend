package com.tasks.organizer.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

import java.time.Duration;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;

@Configuration
public class RedisRateLimitConfiguration {

  // Reuses the standard spring.data.redis.* properties
  @Bean(destroyMethod = "shutdown")
  public RedisClient rateLimitRedisClient(RedisProperties properties) {
    if (properties.getUrl() != null) {
      return RedisClient.create(properties.getUrl());
    }

    RedisURI uri = RedisURI.builder()
        .withHost(properties.getHost())
        .withPort(properties.getPort())
        .withDatabase(properties.getDatabase())
        .build();
    if (StringUtils.hasText(properties.getPassword())) {
      if (StringUtils.hasText(properties.getUsername())) {
        uri.setUsername(properties.getUsername());
      }
      uri.setPassword(properties.getPassword().toCharArray());
    }
    return RedisClient.create(uri);
  }

  // Lazy so the app (and tests) can start without Redis; the connection opens on the first rate-limited request
  @Lazy
  @Bean(destroyMethod = "close")
  public StatefulRedisConnection<String, byte[]> rateLimitRedisConnection(RedisClient rateLimitRedisClient) {
    return rateLimitRedisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
  }

  @Lazy
  @Bean
  public ProxyManager<String> rateLimitProxyManager(StatefulRedisConnection<String, byte[]> rateLimitRedisConnection) {
    return Bucket4jLettuce.casBasedBuilder(rateLimitRedisConnection)
        // Redis deletes a bucket's key once it would have fully refilled, so idle clients don't pile up
        .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofSeconds(10)))
        .build();
  }
}
