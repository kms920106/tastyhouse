package com.tastyhouse.webapi.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller 메서드의 Request Body 및 인증 사용자 정보를 로깅하는 Aspect
 *
 * 로깅 항목: 인증 사용자(username), Request Body (민감 필드 마스킹 적용)
 * Filter 레이어에서 처리되는 401/403 등은 별도 로깅되지 않으며,
 * Controller까지 도달한 요청에 대해서만 동작합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLoggingAspect {

//    private final SensitiveFieldMasker masker;

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String caller = resolveAuthenticatedUser();
        List<Object> requestBodies = extractRequestBodies(joinPoint);

        if (!requestBodies.isEmpty()) {
//            List<String> maskedBodies = requestBodies.stream()
//                    .map(masker::mask)
//                    .toList();
//            log.info("[BODY] user={} | body={}", caller, maskedBodies.size() == 1 ? maskedBodies.get(0) : maskedBodies);
            log.info("[BODY] user={} | body={}", caller, requestBodies.size() == 1 ? requestBodies.getFirst() : requestBodies);
        } else if (!caller.equals("anonymous")) {
            log.info("[BODY] user={}", caller);
        }

        return joinPoint.proceed();
    }

    /**
     * SecurityContext에서 인증된 사용자의 username을 추출합니다.
     * 인증되지 않은 요청은 "anonymous"를 반환합니다.
     */
    private String resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }

    /**
     * Controller 메서드 파라미터 중 @RequestBody 어노테이션이 붙은 인자를 추출합니다.
     */
    private List<Object> extractRequestBodies(ProceedingJoinPoint joinPoint) {
        List<Object> bodies = new ArrayList<>();
        try {
            Method method = resolveMethod(joinPoint);
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < paramAnnotations.length; i++) {
                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation instanceof RequestBody && args[i] != null) {
                        bodies.add(args[i]);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("RequestBody 추출 실패: {}", e.getMessage());
        }
        return bodies;
    }

    private Method resolveMethod(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        String methodName = joinPoint.getSignature().getName();
        Class<?>[] paramTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes();
        return joinPoint.getTarget().getClass().getMethod(methodName, paramTypes);
    }
}
