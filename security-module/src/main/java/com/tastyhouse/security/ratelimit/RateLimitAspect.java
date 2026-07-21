package com.tastyhouse.security.ratelimit;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final RateLimiterService rateLimiterService;

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        String key = buildKey(joinPoint, rateLimit);
        Duration window = Duration.ofSeconds(rateLimit.windowSeconds());

        if (rateLimiterService.isLimitExceeded(key, rateLimit.limit(), window)) {
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
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 요청 인자 중 {@code fieldName}에 해당하는 값을 리플렉션으로 추출합니다.
     * 레코드 컴포넌트 접근자(fieldName()) → getter(getFieldName()) 순으로 시도합니다.
     */
    private String resolveFieldValue(Object[] args, String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            log.warn("keyType=FIELD 사용 시 keyField를 지정해야 합니다.");
            return "unknown";
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
                    // 해당 메서드가 없는 인자는 건너뜀
                } catch (Exception e) {
                    log.warn("keyField '{}' 추출 실패: {}", fieldName, e.getMessage());
                }
            }
        }
        return "unknown";
    }
}
