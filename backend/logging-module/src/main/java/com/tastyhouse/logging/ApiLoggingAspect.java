package com.tastyhouse.logging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

@Aspect
@Component
public class ApiLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingAspect.class);

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logControllerExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String caller = resolveAuthenticatedUser();
        List<Object> requestBodies = extractRequestBodies(joinPoint);

        if (!requestBodies.isEmpty()) {
            log.info("[BODY] user={} | body={}", caller, requestBodies.size() == 1 ? requestBodies.getFirst() : requestBodies);
        } else if (!caller.equals("anonymous")) {
            log.info("[BODY] user={}", caller);
        }

        return joinPoint.proceed();
    }

    private String resolveAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }

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
