package com.tastyhouse.apicommon.ratelimit;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.tastyhouse.apicommon.common.ClientIpResolver;

@Aspect
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String UNKNOWN_IDENTIFIER = "unknown";

    private final RateLimitCounterPort rateLimitCounter;

    public RateLimitAspect(RateLimitCounterPort rateLimitCounter) {
        this.rateLimitCounter = rateLimitCounter;
    }

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        String key = buildKey(joinPoint, rateLimit);
        Duration window = Duration.ofSeconds(rateLimit.windowSeconds());

        if (rateLimitCounter.isLimitExceeded(key, rateLimit.limit(), window)) {
            log.warn("Rate limit exceeded - key: {}, limit: {}/{}", key, rateLimit.limit(), rateLimit.windowSeconds() + "s");
            throw new RateLimitException();
        }
    }

    private String buildKey(JoinPoint joinPoint, RateLimit rateLimit) {
        String identifier = switch (rateLimit.keyType()) {
            case IP -> resolveClientIp();
            case FIELD -> resolveFieldValue(joinPoint.getArgs(), rateLimit.keyField());
        };
        return rateLimit.keyPrefix() + ":" + identifier;
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return UNKNOWN_IDENTIFIER;
        }
        HttpServletRequest request = attributes.getRequest();
        return ClientIpResolver.resolve(request);
    }

    private String resolveFieldValue(Object[] args, String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            log.warn("keyType=FIELD 사용 시 keyField를 지정해야 합니다.");
            return UNKNOWN_IDENTIFIER;
        }

        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        List<String> candidates = List.of(fieldName, getterName);

        for (Object arg : args) {
            if (arg == null) continue;
            for (String methodName : candidates) {
                try {
                    Method method = arg.getClass().getMethod(methodName);
                    Object value = method.invoke(arg);
                    if (value instanceof String str && StringUtils.hasText(str)) {
                        return str;
                    }
                } catch (NoSuchMethodException ignored) {
                } catch (Exception e) {
                    log.warn("keyField '{}' 추출 실패: {}", fieldName, e.getMessage());
                }
            }
        }
        return UNKNOWN_IDENTIFIER;
    }
}
