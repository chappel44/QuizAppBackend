package com.tasks.organizer.ratelimit;

import io.github.bucket4j.ConsumptionProbe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import com.tasks.organizer.authorization.OrgAuthorization;
import com.tasks.organizer.organization.context.OrgContext;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

  private final RateLimitService rateLimitService;
  private final OrgAuthorization orgAuthorization;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (!(handler instanceof HandlerMethod handlerMethod)) {
      return true;
    }

    RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
    if (rateLimit == null) {
      return true;
    }

    Map<String, String> pathVariables =
        (Map<String, String>) request.getAttribute(
                HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

    String organizationId = pathVariables.get("organizationId");
    String orgKey = (organizationId == null && rateLimit.keyType() == KeyType.USER) ? "" : (":" + organizationId);

    String endpoint = handlerMethod.getBeanType().getName() + "#" + handlerMethod.getMethod().getName();
    
    String key = endpoint + ":" + resolveClientKey(request, rateLimit.keyType()) + orgKey;
    Duration period = Duration.of(rateLimit.period(), rateLimit.unit());

    ConsumptionProbe probe;
    try {
      probe = rateLimitService.tryConsume(key, rateLimit.capacity(), period);
    } catch (RuntimeException e) {
      // Fail open: a Redis outage shouldn't take down the endpoints being protected
      log.warn("Rate limiting unavailable, allowing request to {}", endpoint, e);
      return true;
    }

    if (probe.isConsumed()) {
      response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
      return true;
    }

    long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(
      "{\"status\":429,\"message\":\"Too many requests. Try again in " + retryAfterSeconds + " seconds.\",\"data\":null}"
    );
    return false;
  }

  private String resolveClientKey(HttpServletRequest request, KeyType keyType) {
    if (keyType == KeyType.USER) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
        return "user:" + auth.getName();
      }
    }
    else if (keyType == KeyType.USER_ORGANIZATION_ID) {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
        OrgContext org = orgAuthorization.currentOrgContext();
        if(org != null){
          UUID organizationId = org.organizationId();
          return "user:" + auth.getName() + ":organizationId:" + organizationId;
        }

        return "user:" + auth.getName();
      }
    }
    return "ip:" + request.getRemoteAddr();
  }
}
