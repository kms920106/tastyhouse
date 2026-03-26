package com.tastyhouse.webapi.ratelimit;

import com.tastyhouse.webapi.exception.RateLimitException;
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

import java.lang.reflect.Method;
import java.time.Duration;

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
            case PHONE -> resolvePhoneNumber(joinPoint);
            case EMAIL -> resolveEmail(joinPoint);
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

    private String resolvePhoneNumber(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) {
                continue;
            }
            for (String methodName : new String[]{"phoneNumber", "getPhoneNumber"}) {
                try {
                    Method method = arg.getClass().getMethod(methodName);
                    Object phoneNumber = method.invoke(arg);
                    if (phoneNumber instanceof String phone && StringUtils.hasText(phone)) {
                        return phone;
                    }
                } catch (NoSuchMethodException ignored) {
                    // 해당 메서드가 없는 인자는 건너뜀
                } catch (Exception e) {
                    log.warn("phoneNumber 추출 실패: {}", e.getMessage());
                }
            }
        }
        return "unknown";
    }

    private String resolveEmail(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) {
                continue;
            }
            try {
                Method getEmail = arg.getClass().getMethod("email");
                Object email = getEmail.invoke(arg);
                if (email instanceof String emailValue && StringUtils.hasText(emailValue)) {
                    return emailValue;
                }
            } catch (NoSuchMethodException ignored) {
                // email 메서드가 없는 인자는 건너뜀
            } catch (Exception e) {
                log.warn("email 추출 실패: {}", e.getMessage());
            }
        }
        return "unknown";
    }
}
