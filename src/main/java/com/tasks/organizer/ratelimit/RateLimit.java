package com.tasks.organizer.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Limits how often a single client can call the annotated endpoint.
 * Example: @RateLimit(capacity = 5, period = 1, unit = ChronoUnit.HOURS) allows 5 requests per hour per client.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

  /** Maximum number of requests allowed within the period. */
  long capacity();

  /** Length of the window in which {@link #capacity()} tokens are refilled. */
  long period() default 1;

  ChronoUnit unit() default ChronoUnit.MINUTES;

  /** How clients are identified: by IP address or by authenticated user (falls back to IP when anonymous). */
  KeyType keyType() default KeyType.IP;
}
