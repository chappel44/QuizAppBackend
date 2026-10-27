package com.tasks.organizer.ratelimit;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import java.time.Duration;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  private static final String KEY_PREFIX = "rate-limit:";

  // Bucket state lives in Redis, so limits are shared across all app instances and survive restarts
  private final ProxyManager<String> proxyManager;

  public RateLimitService(@Lazy ProxyManager<String> proxyManager) {
    this.proxyManager = proxyManager;
  }

  /**
   * Attempts to consume one token from the bucket identified by {@code key}.
   * The bucket is created on first use with {@code capacity} tokens refilled every {@code period}.
   */
  public ConsumptionProbe tryConsume(String key, long capacity, Duration period) {
    return proxyManager.builder()
        .build(KEY_PREFIX + key, () -> newConfiguration(capacity, period))
        .tryConsumeAndReturnRemaining(1);
  }

  public void reset(String key) {
    proxyManager.removeProxy(KEY_PREFIX + key);
  }

  private BucketConfiguration newConfiguration(long capacity, Duration period) {
    return BucketConfiguration.builder()
        .addLimit(limit -> limit.capacity(capacity).refillIntervally(capacity, period))
        .build();
  }
}
