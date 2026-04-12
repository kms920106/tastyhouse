package com.tastyhouse.webapi.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * API 요청/응답 메타 정보 로깅 필터
 *
 * 로깅 항목: requestId, Method, Path, Client IP, Status Code, 처리 시간(ms)
 * requestId는 MDC에 등록되어 같은 요청에서 발생하는 모든 로그(p6spy 포함)에 자동 첨부됨
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final String MDC_REQUEST_ID = "requestId";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String REQUEST_START_TIME_ATTR = "requestStartTime";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_REQUEST_ID, requestId);
        request.setAttribute(REQUEST_START_TIME_ATTR, System.currentTimeMillis());

        try {
            log.info("[REQUEST] {} {} | IP: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    resolveClientIp(request));

            filterChain.doFilter(request, response);

        } finally {
            long elapsed = System.currentTimeMillis() - (long) request.getAttribute(REQUEST_START_TIME_ATTR);
            int status = response.getStatus();

            logResponse(request.getMethod(), request.getRequestURI(), status, elapsed);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private void logResponse(String method, String uri, int status, long elapsed) {
        String message = "[RESPONSE] {} {} | {} | {}ms";
        if (status >= 500) {
            log.error(message, method, uri, status, elapsed);
        } else if (status >= 400) {
            log.warn(message, method, uri, status, elapsed);
        } else {
            log.info(message, method, uri, status, elapsed);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
