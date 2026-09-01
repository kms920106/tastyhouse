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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.tastyhouse.apicommon.common.ClientIpResolver;

/**
 * {@link RateLimit}이 붙은 메서드의 호출 횟수를 검증하는 AOP.
 *
 * <p>키 조립(클라이언트 IP·요청 필드 해석)은 HTTP 어댑터 관심사이므로 이 표현 모듈이 소유하고,
 * 실제 카운팅만 {@link RateLimitCounterPort} 구현체(인프라)에 위임한다.
 */
@Aspect
@Component
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

    /**
     * 요청 인자 중 {@code fieldName}에 해당하는 값을 리플렉션으로 추출합니다.
     * 레코드 컴포넌트 접근자(fieldName()) → getter(getFieldName()) 순으로 시도합니다.
     */
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
                    // 해당 메서드가 없는 인자는 건너뜀
                } catch (Exception e) {
                    log.warn("keyField '{}' 추출 실패: {}", fieldName, e.getMessage());
                }
            }
        }
        return UNKNOWN_IDENTIFIER;
    }
}
